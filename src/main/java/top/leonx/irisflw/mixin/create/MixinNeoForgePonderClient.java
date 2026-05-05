package top.leonx.irisflw.mixin.create;

import dev.engine_room.flywheel.api.backend.BackendManager;
import net.createmod.ponder.NeoForgePonderClient;
import net.createmod.ponder.PonderClient;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.backend.IrisFlwBackends;

@Mixin(value = NeoForgePonderClient.ClientEvents.class, remap = false)
public class MixinNeoForgePonderClient {

    @Inject(method = "onRenderWorld(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void irisflw$OnRenderWorld(RenderLevelStageEvent event, CallbackInfo ci) {
        if (BackendManager.currentBackend() != IrisFlwBackends.INSTANCING) return;

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            PonderClient.onRenderWorld(event.getPoseStack());
            ci.cancel();
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            ci.cancel();
        }
    }
}
