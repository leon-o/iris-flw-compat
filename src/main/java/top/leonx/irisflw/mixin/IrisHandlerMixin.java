package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.ShadersModHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShadersModHandler.class)
public class IrisHandlerMixin {
    @Inject(at = @At("HEAD"), method = "isShaderPackInUse()Z", cancellable = true,remap = false)
    private static void isShaderPackInUse(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(false);
    }}
