package top.leonx.irisflw.accessors;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

public interface NewWorldRenderingPipelineAccessor {
    ProgramSet getProgramSet();

    ShaderInstance callCreateShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode, boolean isFullbright) throws IOException;


    ShaderInstance callCreateShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, boolean isFullbright) throws IOException;

    //IrisShaderProgram getFlwShaderProgram(ProgramContext context);
}
