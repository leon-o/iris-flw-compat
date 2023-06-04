package top.leonx.irisflw.accessors;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

public interface NewWorldRenderingPipelineAccessor {
    ProgramSet getProgramSet();

    ShaderInstance callCreateShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                    VertexFormat vertexFormat, FogMode fogMode,
                                    boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText) throws IOException;


    ShaderInstance callCreateShadowShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                          VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText) throws IOException;

    //IrisShaderProgram getFlwShaderProgram(ProgramContext context);
}
