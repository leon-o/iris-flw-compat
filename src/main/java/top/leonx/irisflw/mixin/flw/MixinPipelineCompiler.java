package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.backend.compile.PipelineCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.backend.IrisPipelineCompiler;

@Mixin(value = PipelineCompiler.class, remap = false)
public class MixinPipelineCompiler {

    @Inject(method = "deleteAll", at = @At("TAIL"))
    private static void irisflw$deleteAll(CallbackInfo ci) {
        IrisPipelineCompiler.deleteAll();
    }
}
