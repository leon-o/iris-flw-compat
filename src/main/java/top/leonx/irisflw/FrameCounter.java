package top.leonx.irisflw;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber
public class FrameCounter {
    public static long RENDER_FRAME_COUNT = 0;

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Post event) {
        RENDER_FRAME_COUNT++;
    }
}