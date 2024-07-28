package top.leonx.irisflw.fabric;

import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;

public class FlywheelEventsInvokerImpl {
    public static void InvokeRenderLayer(RenderLayerEvent renderLayerEvent){
        FlywheelEvents.RENDER_LAYER.invoker().handleEvent(renderLayerEvent);
    }
}
