package adris.altoclef.eventbus.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ClientRenderEvent {
    public DrawContext context;
    public float tickDelta;

    public ClientRenderEvent(DrawContext context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }
}
