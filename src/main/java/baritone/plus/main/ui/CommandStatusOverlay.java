package baritone.plus.main.ui;

import baritone.plus.api.tasks.Task;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandStatusOverlay {

    //For the ingame timer
    private long _timeRunning;
    private long _lastTime = 0;
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    public void render(BaritonePlus mod/*, MatrixStack matrixstack*/) {
        if (mod.getModSettings().shouldShowTaskChain()) {
            List<Task> tasks = Collections.emptyList();
            if (mod.getTaskRunner().getCurrentTaskChain() != null) {
                tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
            }

            int color = 0xFFFFFFFF;
            drawTaskChain(Minecraft.getMinecraft().fontRenderer, 5, 5, color, 10, tasks, mod);
        }
    }

    private void drawTaskChain(FontRenderer renderer, int dx, int dy, int color, int maxLines, List<Task> tasks, BaritonePlus mod) {
        if (tasks.size() == 0) {
            renderer.drawString(" (no task running) ", dx, dy, color);
            if (_lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {
                _timeRunning = Instant.now().toEpochMilli(); // reset the timer if it's been more than 10 seconds
            }
        } else {
            float fontHeight = renderer.FONT_HEIGHT;
            // Draw the timer
            if (mod.getModSettings().shouldShowTimer()) {
                _lastTime = Instant.now().toEpochMilli(); // keep the last time for the timer reset
                String _realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(_timeRunning)); // Format the running time to string
                renderer.drawString(_realTime, dx, dy, color); // Draw the timer before drawing tasks list
                dy += fontHeight + 2;
            }
            // Draw the item chain
            List<String> itemChain = new ArrayList<>();
            for (Task task : tasks) {
                if (task instanceof ResourceTask resourceTask && !resourceTask.getItemName().isBlank()) {
                    itemChain.add(resourceTask.getItemName());
                }
            }
            renderer.drawString(String.join(" ← ", itemChain), dx, dy, color);
            dy += fontHeight + 2;
            // Draw the tasks list
            if (tasks.size() > maxLines) {
                for (int i = 0; i < tasks.size(); ++i) {
                    // Skip over the next tasks
                    if (i == 0 || i > tasks.size() - maxLines) {
                        var text = renderer.trimStringToWidth(tasks.get(i).toString(), Minecraft.getMinecraft().displayWidth / 2);
                        renderer.drawString(text, dx, dy, color);
//                        renderer.drawString(  tasks.get(i).toString(), dx, dy, color);
                    } else if (i == 1) {
                        renderer.drawString(  " ... ", dx, dy, color);
                    } else {
                        continue;
                    }
                    dx += 8;
                    dy += fontHeight + 2;
                }
            } else {
                if (!tasks.isEmpty()) {
                    for (Task task : tasks) {
                        var text = renderer.trimStringToWidth(task.toString(), Minecraft.getMinecraft().displayWidth / 2);
                        renderer.drawString(text, dx, dy, color);
                        dx += 8;
                        dy += fontHeight + 2;
                    }
                }
            }
        }
    }
}
