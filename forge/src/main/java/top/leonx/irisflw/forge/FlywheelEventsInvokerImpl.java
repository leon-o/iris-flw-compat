package top.leonx.irisflw.forge;

import com.jozufozu.flywheel.event.RenderLayerEvent;
import net.minecraftforge.common.MinecraftForge;

public class FlywheelEventsInvokerImpl {
    public static void InvokeRenderLayer(RenderLayerEvent renderLayerEvent){
        MinecraftForge.EVENT_BUS.post(renderLayerEvent);
    }
}
