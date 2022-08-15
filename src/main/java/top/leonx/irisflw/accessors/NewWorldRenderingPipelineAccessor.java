package top.leonx.irisflw.accessors;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;

public interface NewWorldRenderingPipelineAccessor {
    ProgramSet getProgramSet();

    Shader callCreateShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
                                   VertexFormat vertexFormat, FogMode fogMode,
                                   boolean isIntensity, boolean isFullbright);


    Shader callCreateShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
                                                  VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright);

    //IrisShaderProgram getFlwShaderProgram(ProgramContext context);
}
