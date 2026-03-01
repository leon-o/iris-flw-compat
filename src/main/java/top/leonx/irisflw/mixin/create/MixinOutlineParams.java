package top.leonx.irisflw.mixin.create;

import com.simibubi.create.AllSpecialTextures;
import dev.engine_room.flywheel.api.backend.BackendManager;
import net.createmod.catnip.outliner.Outline;
import net.createmod.catnip.render.BindableTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.backend.IrisFlwBackends;
import top.leonx.irisflw.AllIrisFlwSpecialTextures;
import top.leonx.irisflw.config.IrisFlwConfig;

@Mixin(value = Outline.OutlineParams.class, remap = false)
public class MixinOutlineParams {

    @Shadow
    protected BindableTexture faceTexture;

    @Shadow
    protected BindableTexture highlightedFaceTexture;

    @Inject(method = "withFaceTexture", at = @At("TAIL"), require = 0)
    private void irisflw$withFaceTexture(BindableTexture texture, CallbackInfoReturnable<Outline.OutlineParams> cir){
        if(BackendManager.currentBackend() == IrisFlwBackends.INSTANCING){
            if(IrisFlwConfig.CLIENT.replaceCheckerboardTexture.getAsBoolean() && texture == AllSpecialTextures.CHECKERED){
                this.faceTexture = AllIrisFlwSpecialTextures.CHECKERED;
            }
        }
    }

    @Inject(method = "withFaceTextures", at = @At("TAIL"), require = 0)
    private void irisflw$withFaceTextures(BindableTexture texture, BindableTexture highlightTexture, CallbackInfoReturnable<Outline.OutlineParams> cir){
        if(BackendManager.currentBackend() == IrisFlwBackends.INSTANCING){
            if(IrisFlwConfig.CLIENT.replaceCheckerboardTexture.getAsBoolean() && texture == AllSpecialTextures.CHECKERED){
                this.faceTexture = AllIrisFlwSpecialTextures.CHECKERED;
            }
        }
    }
}
