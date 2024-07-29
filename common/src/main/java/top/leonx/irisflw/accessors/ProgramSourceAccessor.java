package top.leonx.irisflw.accessors;

import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.ShaderProperties;

public interface ProgramSourceAccessor {
    ShaderProperties getShaderProperties();

    BlendModeOverride getBlendModeOverride();
}
