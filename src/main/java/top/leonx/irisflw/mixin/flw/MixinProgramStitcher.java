package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.compile.core.Compile;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.Map;

@Mixin(value = Compile.ProgramStitcher.class, remap = false)
public class MixinProgramStitcher<K> {

    @Shadow
    private final Map<ShaderType, Compile.ShaderCompiler<K>> compilers = new EnumMap(ShaderType.class);

    @Inject(method = "link", at = @At("HEAD"), remap = false, cancellable = true)
    public void irisflw$link(Compile.ShaderCompiler<K> compilerBuilder, CallbackInfoReturnable<Compile.ProgramStitcher<K>> cir){
        var shaderType = ((ShaderCompilerAccessor)compilerBuilder).getShaderType();
        if (this.compilers.containsKey(shaderType)) {
            throw new IllegalArgumentException("Duplicate shader type: " + shaderType);
        } else {
            if(shaderType==ShaderType.VERTEX)
                compilerBuilder.onCompile(this::PatchShader);
            this.compilers.put(shaderType, compilerBuilder);
            cir.setReturnValue((Compile.ProgramStitcher<K>)(Object)this);
            cir.cancel();
        }
    }

    private void PatchShader(K k, Compilation compilation) {
        if(k == ShaderType.VERTEX){
            System.out.println("Patching vertex shader");
        }
    }
}
