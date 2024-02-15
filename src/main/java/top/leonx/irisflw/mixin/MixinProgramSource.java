package top.leonx.irisflw.mixin;

import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;

@Mixin(ProgramSource.class)
public class MixinProgramSource implements ProgramSourceAccessor {
    @Unique
    ShaderProperties shaderProperties;

    @Unique
    BlendModeOverride blendMode;

    public ShaderProperties getShaderProperties() {
        return shaderProperties;
    }

    @Override
    public BlendModeOverride getBlendModeOverride() {
        return blendMode;
    }

    @Inject(remap = false, method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lnet/coderbot/iris/shaderpack/ProgramSet;Lnet/coderbot/iris/shaderpack/ShaderProperties;Lnet/coderbot/iris/gl/blending/BlendModeOverride;)V",at = @At("TAIL"))
    private void injectInit(String name, String vertexSource, String geometrySource, String tessControlSource, String tessEvalSource, String fragmentSource, ProgramSet parent, ShaderProperties properties, BlendModeOverride blendModeOverride, CallbackInfo ci){
        shaderProperties = properties;

        blendMode = blendModeOverride;
    }


}
