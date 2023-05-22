package adris.altoclef.brainWIP;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.brainWIP.gui.BrainTTS;
import adris.altoclef.brainWIP.tasks.BrainTask;
import adris.altoclef.brainWIP.utils.ChatGPT;
import adris.altoclef.brainWIP.utils.WorldState;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.tasks.movement.IdleTask;
import adris.altoclef.tasksystem.Task;
import net.fabricmc.loader.api.FabricLoader;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BaritoneBrain {

    // AltoClef Mod
    private final AltoClef mod;

    // Memory store
    public List<MemoryItem> memory;
    // Minecraft world state
    public WorldState worldState;
    // ChatGPT
    public ChatGPT chatGPT;
    public BrainTTS brainTTS;
    private boolean taskInProgress = false;
    private final Subscription<ChatMessageEvent> _commandResponsePrompt;

    public BaritoneBrain(AltoClef mod) {
        this.mod = mod;
        this.memory = new ArrayList<>();
        this.chatGPT = ChatGPT.build();

        var args = List.of(FabricLoader.getInstance().getLaunchArguments(true));

        if (args.contains("--tts")) {
            SwingUtilities.invokeLater(() -> {
                this.brainTTS = new BrainTTS(mod);
                Debug.logInternal("TTS Enabled.");
            });
        } else {
            Debug.logInternal("TTS Disabled.");
        }

        _commandResponsePrompt = EventBus.subscribe(ChatMessageEvent.class, evt -> {
            // TODO Prompt it self from the chat response
        });
    }

    public BrainTTS getTTS() {
        return brainTTS;
    }

    public void updateWorldState() {
        this.worldState = new WorldState(mod);
    }

    public Task process(BrainTask task) {
        // Update World State
        updateWorldState();

        // Decay memory
        decayMemory();

        if (!taskInProgress) {
            new Thread(() -> {
                try {
                    taskInProgress = true;  // Mark that a task is in progress

                    var nextTask = chatGPT.generateTask(worldState);

                    task.setDebugState(nextTask);

                    Debug.logMessageBrain(nextTask);

                    var chat = mod.mc().getNetworkHandler();
                    if (nextTask.startsWith("@") && chat != null) {
                        chat.sendChatMessage(nextTask);
                    } else {
                        mod.runUserTask(Task.fromString(nextTask), () -> {
                            taskInProgress = false;
                            mod.runUserTask(task);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    task.setDebugState(e.getMessage());
                    taskInProgress = false;  // If there was an error, allow a new task to be generated
//                    task.stop(mod);
                }
            }).start();
        }

        // Learn from outcomes
        learnFromOutcomes();

        // Return Task
        return new IdleTask();
    }

    private void decayMemory() {
        LocalDateTime now = LocalDateTime.now();
        memory.removeIf(memoryItem -> {
            // Remove memories older than one day with low importance and low access frequency
            return memoryItem.getTimestamp().isBefore(now.minusDays(1))
                    && memoryItem.getImportance() < 5
                    && memoryItem.getAccessCount() < 3;
        });
    }

    private void learnFromOutcomes() {
        // Code to incorporate feedback from completed goals
    }

    public WorldState getWorldState() {
        return worldState;
    }

    @Override
    public String toString() {
        return "Brain{" +
                "memory=" + memory +
                ", worldState=" + worldState +
                '}';
    }

    public void onStop(AltoClef mod, Task interruptTask) {
        taskInProgress = false;
    }

    public static class MemoryItem {
        private final String content;
        private final LocalDateTime timestamp;
        private final int importance;
        private int accessCount;

        public MemoryItem(String content, LocalDateTime timestamp, int importance) {
            this.content = content;
            this.timestamp = timestamp;
            this.importance = importance;
            this.accessCount = 0;
        }

        public String getContent() {
            return content;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getImportance() {
            return importance;
        }

        public int getAccessCount() {
            return accessCount;
        }

        public void incrementAccessCount() {
            this.accessCount++;
        }

        @Override
        public String toString() {
            return "MemoryItem{" +
                    "content='" + content + '\'' +
                    ", timestamp=" + timestamp +
                    ", importance=" + importance +
                    ", accessCount=" + accessCount +
                    '}';
        }
    }
}
