package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import java.util.ArrayList;

public class CommandStatusOverlay {

    //For the ingame timer
    private long _timeRunning;
    private long _lastTime = 0;
    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    public void render(AltoClef mod, MatrixStack matrixstack) {
        if (mod.getModSettings().shouldShowTaskChain()) {
            List<Task> tasks = Collections.emptyList();
            if (mod.getTaskRunner().getCurrentTaskChain() != null) {
                tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
            }

            int color = 0xFFFFFFFF;
            drawTaskChain(MinecraftClient.getInstance().textRenderer, matrixstack, 5, 5, color, 10, tasks, mod);
        }
    }

    private void drawTaskChain(TextRenderer renderer, MatrixStack stack, float dx, float dy, int color, int maxLines, List<Task> tasks, AltoClef mod) {
        if (tasks.size() == 0) {
            renderer.draw(stack, " (no task running) ", dx, dy, color);
            if (_lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {
                _timeRunning = Instant.now().toEpochMilli(); // reset the timer if it's been more than 10 seconds
            }
        } else {
            float fontHeight = renderer.fontHeight;
            // Draw the timer
            if (mod.getModSettings().shouldShowTimer()) {
                _lastTime = Instant.now().toEpochMilli(); // keep the last time for the timer reset
                String _realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(_timeRunning)); // Format the running time to string
                renderer.draw(stack, _realTime, dx, dy, color); // Draw the timer before drawing tasks list
                dy += fontHeight + 2;
            }
            // Draw the item chain
            List<String> itemChain = new ArrayList<>();
            for (Task task : tasks) {
                if (task instanceof ResourceTask resourceTask && !resourceTask.getItemName().isBlank()) {
                    itemChain.add(resourceTask.getItemName());
                }
            }
            renderer.draw(stack, String.join(" ← ", itemChain), dx, dy, color);
            dy += fontHeight + 2;
            // Draw the tasks list
            if (tasks.size() > maxLines) {
                for (int i = 0; i < tasks.size(); ++i) {
                    // Skip over the next tasks
                    if (i == 0 || i > tasks.size() - maxLines) {
                        var text = renderer.trimToWidth(tasks.get(i).toString(), MinecraftClient.getInstance().getWindow().getWidth() / 2);
                        renderer.draw(stack, text, dx, dy, color);
//                        renderer.draw(stack, tasks.get(i).toString(), dx, dy, color);
                    } else if (i == 1) {
                        renderer.draw(stack, " ... ", dx, dy, color);
                    } else {
                        continue;
                    }
                    dx += 8;
                    dy += fontHeight + 2;
                }
            } else {
                if (!tasks.isEmpty()) {
                    for (Task task : tasks) {
                        var text = renderer.trimToWidth(task.toString(), MinecraftClient.getInstance().getWindow().getWidth() / 2);
                        renderer.draw(stack, text, dx, dy, color);
                        dx += 8;
                        dy += fontHeight + 2;
                    }
                }
            }
        }
    }
}
