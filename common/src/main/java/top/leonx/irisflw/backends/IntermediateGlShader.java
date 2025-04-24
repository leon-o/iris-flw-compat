package top.leonx.irisflw.backends;

import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;

public class IntermediateGlShader extends GlShader {
    private final String source;
    private final String name;

    public IntermediateGlShader(String source, ShaderType type, String name) {
        super(0, type, name);
        this.source = source;
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public String getName()
    {
        return name;
    }
}
