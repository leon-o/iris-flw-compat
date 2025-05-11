package top.leonx.irisflw.mixin.iris;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.event.RenderContextImpl;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;

@Mixin(value = ShadowRenderer.class,remap = false)
public abstract class MixinShadowRenderer {

    @Final
    @Shadow
    private boolean shouldRenderBlockEntities;

    @Final
    @Shadow
    private RenderBuffers buffers;

    @Inject(method = "renderShadows",at = @At("HEAD"))
    private void injectRenderShadow(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci){
        if (shouldRenderBlockEntities){
            RenderLayerEventStateManager.setRenderingShadow(true);
        }
    }


    @Inject(method = "renderShadows",at = @At("TAIL"))
    private void injectRenderShadowTail(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci){
        RenderLayerEventStateManager.setRenderingShadow(false);
    }

    @Inject(method = "renderShadows",
            at = {@At(
            value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
            args = {"ldc=draw entities"}
    )})
    private void injectRenderShadowBeforeDrawEntities(LevelRendererAccessor levelRenderer, Camera playerCamera, CallbackInfo ci){
        if(shouldRenderBlockEntities)
        {
            var modelMatrix = ShadowRenderer.MODELVIEW;
            var projectionMatrix = ShadowRenderer.PROJECTION;
            var deltaTracker = Minecraft.getInstance().getTimer();
            var flywheel$renderContext = RenderContextImpl.create((LevelRenderer) levelRenderer, levelRenderer.getLevel(), this.buffers, modelMatrix, projectionMatrix, playerCamera, deltaTracker.getGameTimeDeltaPartialTick(false));
            VisualizationManager manager = VisualizationManager.get(levelRenderer.getLevel());
            if (manager != null) {
//                manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
                manager.renderDispatcher().afterEntities(flywheel$renderContext);
            }
        }
    }
}
