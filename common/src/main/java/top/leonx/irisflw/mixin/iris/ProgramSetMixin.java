package top.leonx.irisflw.mixin.iris;

import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.features.FeatureFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.ProgramSetAccessor;

import java.util.Optional;
import java.util.function.Function;

@Mixin(ProgramSet.class)
public abstract class ProgramSetMixin implements ProgramSetAccessor {
    @Invoker(remap = false)
    @Override
    public abstract ProgramSource callReadProgramSource(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties);

    @Invoker(remap = false)
    @Override
    public abstract ProgramSource callReadProgramSource(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties, BlendModeOverride var5);

    private ProgramSource gbuffersFlw;
    private ProgramSource shadowFlw;

    @Inject(method = "<init>",remap = false,at = @At(value="RETURN"))
    private void initGBufferFlw(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider,
                                ShaderProperties shaderProperties, ShaderPack pack, CallbackInfo ci){
        gbuffersFlw = callReadProgramSource(directory, sourceProvider, "gbuffers_flw", (ProgramSet) (Object)this, shaderProperties);
        shadowFlw = callReadProgramSource(directory, sourceProvider, "shadow_flw", (ProgramSet) (Object)this,shaderProperties, BlendModeOverride.OFF);
    }

    @Override
    public Optional<ProgramSource> getGbuffersFlw() {
        return gbuffersFlw.requireValid();
    }

    @Override
    public Optional<ProgramSource> getShadowFlw() {
        return shadowFlw.requireValid();
    }
}
