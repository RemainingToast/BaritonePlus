package adris.altoclef.ui;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

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
    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.from(ZoneOffset.of("+00:00"))); // The date formatter

    public void render(AltoClef mod, DrawContext context) {
        if (mod.getModSettings().shouldShowTaskChain()) {
            List<Task> tasks = Collections.emptyList();
            if (mod.getTaskRunner().getCurrentTaskChain() != null) {
                tasks = mod.getTaskRunner().getCurrentTaskChain().getTasks();
            }

            int color = 0xFFFFFFFF;
            drawTaskChain(MinecraftClient.getInstance().textRenderer, context, 5, 5, color, 10, tasks, mod);
        }
    }

    private void drawTaskChain(TextRenderer renderer, DrawContext context, int dx, int dy, int color, int maxLines, List<Task> tasks, AltoClef mod) {
        if (tasks.size() == 0) {
            context.drawText(renderer, " (no task running) ", dx, dy, color, true);
            if (_lastTime + 10000 < Instant.now().toEpochMilli() && mod.getModSettings().shouldShowTimer()) {
                _timeRunning = Instant.now().toEpochMilli(); // reset the timer if it's been more than 10 seconds
            }
        } else {
            float fontHeight = renderer.fontHeight;
            // Draw the timer
            if (mod.getModSettings().shouldShowTimer()) {
                _lastTime = Instant.now().toEpochMilli(); // keep the last time for the timer reset
                String _realTime = DATE_TIME_FORMATTER.format(Instant.now().minusMillis(_timeRunning)); // Format the running time to string
                context.drawText(renderer,_realTime, dx, dy, color, true); // Draw the timer before drawing tasks list
                dy += fontHeight + 2;
            }
            // Draw the item chain
            List<String> itemChain = new ArrayList<>();
            for (Task task : tasks) {
                if (task instanceof ResourceTask resourceTask && !resourceTask.getItemName().isBlank()) {
                    itemChain.add(resourceTask.getItemName());
                }
            }
            context.drawText(renderer,  String.join(" ← ", itemChain), dx, dy, color, true);
            dy += fontHeight + 2;
            // Draw the tasks list
            if (tasks.size() > maxLines) {
                for (int i = 0; i < tasks.size(); ++i) {
                    // Skip over the next tasks
                    if (i == 0 || i > tasks.size() - maxLines) {
                        var text = renderer.trimToWidth(tasks.get(i).toString(), MinecraftClient.getInstance().getWindow().getWidth() / 2);
                        context.drawText(renderer, text, dx, dy, color, true);
                    } else if (i == 1) {
                        context.drawText(renderer, " ... ", dx, dy, color, true);
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
                        context.drawText(renderer, text, dx, dy, color, true);
                        dx += 8;
                        dy += fontHeight + 2;
                    }
                }
            }
        }
    }
}
