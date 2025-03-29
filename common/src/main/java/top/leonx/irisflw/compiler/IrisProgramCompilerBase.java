/*

package top.leonx.irisflw.compiler;


import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;

import java.util.HashMap;

public abstract class IrisProgramCompilerBase<P extends WorldProgram> {
    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> programCache = new HashMap<>();

    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> shadowProgramCache = new HashMap<>();

    protected final GlProgram.Factory<P> factory;
    private static int programCounter = 0;

    public IrisProgramCompilerBase(GlProgram.Factory<P> factory, Template<? extends VertexData> ignoredTemplate, FileResolution ignoredHeader) {
        this.factory = factory;
    }

    public P getProgram(String shaderName, boolean isShadow) {

        if (IrisFlw.isShaderPackInUse()) {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            HashMap<ProgramContext, P> cache;
            if (isShadow) {
                cache = shadowProgramCache.computeIfAbsent(pipeline, key -> new HashMap<>());
            } else {
                cache = programCache.computeIfAbsent(pipeline, key -> new HashMap<>());
            }
            if (!cache.containsKey(ctx)) {
                P created = createIrisShaderProgram(ctx, isShadow);
                cache.put(ctx, created);
                if (created == null) {
                    if (Minecraft.getInstance().player != null) {
                        if (isShadow) Minecraft.getInstance().player.displayClientMessage(Component.literal(
                                String.format("Fail to compile %s_%s", "Shadow", shaderName)), false);
                        else Minecraft.getInstance().player.displayClientMessage(Component.literal(
                                String.format("Fail to compile %s_%s", "Gbuffers_flw", shaderName)), false);
                    }
                }
            }
            return cache.get(ctx);
        }
        return null;
    }

    private String getFlwShaderName(ResourceLocation location, boolean isShadow) {
        String randomId = String.valueOf(programCounter);
        programCounter++;
        if (isShadow)
            return String.format("shadow_flw_%s_%s_%s", location.getNamespace(),
                    location.getPath(), randomId);
        else
            return String.format("gbuffers_flw_%s_%s_%s", location.getNamespace(),
                    location.getPath(), randomId);
    }

    abstract P createIrisShaderProgram(ProgramContext ctx, boolean isShadow);

    protected P createWorldProgramBySource(ProgramContext ctx, boolean isShadow, IrisRenderingPipelineAccessor pipeline, ProgramSource processedSource) {
        ShaderInstance override = null;
        try {
            if (isShadow) {
                override = pipeline.callCreateShadowShader(
                        getFlwShaderName(ctx.spec.name, true), processedSource, ProgramId.Block, AlphaTest.ALWAYS,
                        IrisVertexFormats.TERRAIN, false, false, false);
            } else {
                override = pipeline.callCreateShader(
                        getFlwShaderName(ctx.spec.name, false), processedSource, ProgramId.Block, AlphaTest.ALWAYS,
                        IrisVertexFormats.TERRAIN, FogMode.OFF, false, false, false, false);
            }

        } catch (Exception exception) {
            IrisFlw.LOGGER.error("Fail to compile shader", exception);
        }

        if (override != null) {
            P program = factory.create(ctx.spec.name, override.getId());
            ((WorldProgramAccessor) program).setShader(new IrisFlwCompatShaderWarp(override));
            return program;
        }
        return null;
    }

    @NotNull
    protected ProgramSource programSourceOverrideVertexSource(String shaderName, ProgramSet programSet, ProgramSource source, String vertexSource) {
        ShaderProperties properties = ((ProgramSourceAccessor) source).getShaderProperties();
        BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) source).getBlendModeOverride();
        //Get a copy of program
        return new ProgramSource(source.getName() + "_" + shaderName, vertexSource,
                source.getGeometrySource().orElse(null),
                source.getTessControlSource().orElse(null),
                source.getTessEvalSource().orElse(null),
                source.getFragmentSource().orElse(null), programSet, properties, blendModeOverride);
    }

    public void clear() {
        programCache.clear();
        shadowProgramCache.clear();
    }
}
*/
