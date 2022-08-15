package top.leonx.irisflw.iris;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.AlphaTests;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.Locale;

public enum FlwShaderKey {
    // if you auto-format this and destroy all the manual indentation, I'll steal your kneecaps

    BASIC                  (FlwProgramId.Basic, AlphaTests.OFF, VertexFormats.POSITION, FogMode.PER_VERTEX, LightingModel.LIGHTMAP  )
    ;

    private final FlwProgramId program;
    private final AlphaTest alphaTest;
    private final VertexFormat vertexFormat;
    private final FogMode fogMode;
    private final LightingModel lightingModel;

    FlwShaderKey(FlwProgramId program, AlphaTest alphaTest, VertexFormat vertexFormat, FogMode fogMode, LightingModel lightingModel) {
        this.program = program;
        this.alphaTest = alphaTest;
        this.vertexFormat = vertexFormat;
        this.fogMode = fogMode;
        this.lightingModel = lightingModel;
    }

    public FlwProgramId getProgram() {
        return program;
    }

    public AlphaTest getAlphaTest() {
        return alphaTest;
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    public FogMode getFogMode() {
        return fogMode;
    }

    public String getName() {
        return toString().toLowerCase(Locale.ROOT);
    }

    public boolean isShadow() {
        return this.getProgram() == FlwProgramId.Shadow;
    }

    public boolean hasDiffuseLighting() {
        return lightingModel == LightingModel.DIFFUSE || lightingModel == LightingModel.DIFFUSE_LM;
    }

    public boolean shouldIgnoreLightmap() {
        return lightingModel == LightingModel.FULLBRIGHT || lightingModel == LightingModel.DIFFUSE;
    }

    public enum LightingModel {
        FULLBRIGHT,
        LIGHTMAP,
        DIFFUSE,
        DIFFUSE_LM
    }
}
