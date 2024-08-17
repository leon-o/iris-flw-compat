package top.leonx.irisflw.mixin.iris;

import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;

@Mixin(value = ShadowRenderer.class,remap = false)
public class MixinShadowRenderer {

    @Final
    @Shadow
    private boolean shouldRenderBlockEntities;

    @Inject(method = "renderShadows",at = @At("HEAD"))
    private void injectRenderShadow(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci){
        if (shouldRenderBlockEntities){
            RenderLayerEventStateManager.setRenderingShadow(true);
            RenderLayerEventStateManager.setSkip(false);
        }
    }


    @Inject(method = "renderShadows",at = @At("TAIL"))
    private void injectRenderShadowTail(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci){
        RenderLayerEventStateManager.setRenderingShadow(false);
        RenderLayerEventStateManager.setSkip(true);
    }
}
