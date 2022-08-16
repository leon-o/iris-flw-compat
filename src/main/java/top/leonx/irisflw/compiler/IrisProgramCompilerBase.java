package top.leonx.irisflw.compiler;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.compile.Template;
import com.jozufozu.flywheel.core.compile.VertexData;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.*;
import top.leonx.irisflw.flywheel.IrisFlwCompatShaderWarp;

import java.util.HashMap;
import java.util.Map;

public abstract class IrisProgramCompilerBase<P extends WorldProgram> {
    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> programCache = new HashMap<>();

    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> shadowProgramCache = new HashMap<>();

    protected final GlProgram.Factory<P> factory;

    public IrisProgramCompilerBase(GlProgram.Factory<P> factory,Template<? extends VertexData> template, FileResolution header) {
        this.factory = factory;
    }

    public P getProgram(ProgramContext ctx,boolean isShadow) {

        if (IrisApi.getInstance().isShaderPackInUse()) {
            //Optional<ShaderPack> currentPackOptional = Iris.getCurrentPack();
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
                    if (isShadow) MinecraftClient.getInstance().player.sendMessage(new LiteralText(
                            String.format("Fail to compile %s_%s_%s", "Shadow", ctx.spec.name.getNamespace(),
                                          ctx.spec.name.getPath())), false);
                    else MinecraftClient.getInstance().player.sendMessage(new LiteralText(
                            String.format("Fail to compile %s_%s_%s", "Gbuffers_flw", ctx.spec.name.getNamespace(),
                                          ctx.spec.name.getPath())), false);
                }
            }
            return cache.get(ctx);
        }
        return null;
    }

    abstract P createIrisShaderProgram(ProgramContext ctx, boolean isShadow);

    protected P createWorldProgramBySource(ProgramContext ctx, boolean isShadow, NewWorldRenderingPipelineAccessor pipeline, ProgramSource processedSource) {
        Shader override = null;
        try {
            String randomId = MathHelper.randomUuid().toString();
            if (isShadow) {
                override = pipeline.callCreateShadowShader(
                        String.format("shadow_flw_%s_%s_%s", ctx.spec.name.getNamespace(),
                                      ctx.spec.name.getPath(), randomId), processedSource, AlphaTest.ALWAYS,
                        VertexFormats.POSITION_TEXTURE, false, false);
            } else {
                override = pipeline.callCreateShader(
                        String.format("gbuffers_flw_%s_%s_%s", ctx.spec.name.getNamespace(),
                                      ctx.spec.name.getPath(), randomId), processedSource, AlphaTest.ALWAYS,
                        VertexFormats.POSITION_TEXTURE, FogMode.OFF, false, false);
            }

        } catch (Exception exception) {
            IrisFlw.LOGGER.error("Fail to compile shader", exception);
        }

        if (override != null) {
            P program = factory.create(ctx.spec.name, override.getProgramRef());
            ((WorldProgramAccessor) program).setShader(new IrisFlwCompatShaderWarp(override));
            return program;
        }
        return null;
    }

    @NotNull
    protected ProgramSource programSourceOverrideVertexSource(ProgramContext ctx, ProgramSet programSet, ProgramSource source, String vertexSource) {
        ShaderProperties properties = ((ProgramSourceAccessor) source).getShaderProperties();
        BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) source).getBlendModeOverride();
        //Get a copy of program
        ProgramSource processedSource = new ProgramSource(source.getName() + "_" + ctx.spec.name, vertexSource,
                                                          source.getGeometrySource().orElse(null),
                                                          source.getFragmentSource().orElse(null), programSet, properties, blendModeOverride);
        return processedSource;
    }

    public void clear(){
        programCache.clear();
        shadowProgramCache.clear();
    }
}
