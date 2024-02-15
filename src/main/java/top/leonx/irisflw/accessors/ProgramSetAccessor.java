package top.leonx.irisflw.accessors;

import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.function.Function;

public interface ProgramSetAccessor {

    ProgramSource callReadProgramSource(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties, boolean readTesselation);

    ProgramSource callReadProgramSource(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties, BlendModeOverride var5, boolean readTesselation);

    Optional<ProgramSource> getGbuffersFlw();

    Optional<ProgramSource> getShadowFlw();
}
