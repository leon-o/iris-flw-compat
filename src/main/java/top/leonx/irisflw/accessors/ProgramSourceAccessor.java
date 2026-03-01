package top.leonx.irisflw.accessors;

import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;

public interface ProgramSourceAccessor {
    ShaderProperties getShaderProperties();

    BlendModeOverride getBlendModeOverride();
}
