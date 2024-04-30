package top.leonx.irisflw.accessors;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

public interface IrisRenderingPipelineAccessor {
    ProgramSet getProgramSet();

    ShaderInstance callCreateShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                    VertexFormat vertexFormat, FogMode fogMode,
                                    boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText) throws IOException;


    ShaderInstance callCreateShadowShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                          VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText) throws IOException;

    //IrisShaderProgram getFlwShaderProgram(ProgramContext context);
}
