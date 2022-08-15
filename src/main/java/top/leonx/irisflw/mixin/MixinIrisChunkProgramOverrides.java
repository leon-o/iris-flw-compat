package top.leonx.irisflw.mixin;

import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.IrisChunkProgramOverridesHolder;

@Mixin(IrisChunkProgramOverrides.class)
public class MixinIrisChunkProgramOverrides {

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(CallbackInfo ci) {
        IrisChunkProgramOverridesHolder.instance = (IrisChunkProgramOverrides) (Object)this;
    }
}
