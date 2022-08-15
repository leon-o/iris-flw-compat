package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.*;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.IrisVertexPreprocessor;
import top.leonx.irisflw.accessors.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(value = ProgramCompiler.class, remap = false)
public abstract class MixinProgramCompiler<P extends WorldProgram> {

    @Shadow
    @Final
    private GlProgram.Factory<P> factory;

    @Unique
    IrisVertexPreprocessor preprocessor;

    @Unique
    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> programCache = new HashMap<>();

    @Unique
    Map<WorldRenderingPipeline, HashMap<ProgramContext, P>> shadowProgramCache = new HashMap<>();


    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void injectInit(GlProgram.Factory<P> factory, VertexCompiler vertexCompiler, FragmentCompiler fragmentCompiler, CallbackInfo ci) {
        VertexCompilerAccessor vertexCompilerAccessor = (VertexCompilerAccessor) vertexCompiler;
        Template<? extends VertexData> template = vertexCompilerAccessor.getTemplate();
        preprocessor = new IrisVertexPreprocessor(template, vertexCompilerAccessor.getHeader());
    }

    @Inject(method = "getProgram", at = @At("HEAD"), remap = false, cancellable = true)
    public void getProgram(ProgramContext ctx, CallbackInfoReturnable<P> cir) {

        if (IrisApi.getInstance().isShaderPackInUse()) {
            //Optional<ShaderPack> currentPackOptional = Iris.getCurrentPack();
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            boolean isShadow = IrisApi.getInstance().isRenderingShadowPass();
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
            P program = cache.get(ctx);
            if (program != null) cir.setReturnValue(program);
            else {
                if (pipeline instanceof NewWorldRenderingPipeline newPipeline) {
                    Shader shader = newPipeline.getShaderMap().getShader(ShaderKey.TEXTURED_COLOR);
                    if (shader instanceof ExtendedShader extendedShader) {
                        ((ExtendedShaderAccessor) extendedShader).getWritingToBeforeTranslucent().bind();
                        //Use the same render target with Gbuffers_textured.
                    }
                }
            }
        }
    }

    @Nullable
    private P createIrisShaderProgram(ProgramContext ctx, boolean isShadow) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof NewWorldRenderingPipeline newPipeline) {
            ProgramSet programSet = ((NewWorldRenderingPipelineAccessor) newPipeline).getProgramSet();
            Optional<ProgramSource> sourceOptional;

            if (isShadow) {
                sourceOptional = ((ProgramSetAccessor) programSet).getShadowFlw();
            } else {
                sourceOptional = ((ProgramSetAccessor) programSet).getGbuffersFlw();
            }

            if (sourceOptional.isPresent()) {
                ProgramSource source = sourceOptional.get();
                if (source.getVertexSource().isPresent()) {
                    String vertexSource = source.getVertexSource().get();
                    vertexSource = preprocessor.preprocess(vertexSource,
                                                           new IrisVertexPreprocessor.Context(ctx.spec.getVertexFile(),
                                                                                              ctx.ctx, ctx.vertexType));

                    ShaderProperties properties = ((ProgramSourceAccessor) source).getShaderProperties();
                    BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) source).getBlendModeOverride();
                    //Get a copy of program source.
                    ProgramSource processedSource = new ProgramSource(source.getName() + "_" + ctx.spec.name,
                                                                      vertexSource,
                                                                      source.getGeometrySource().orElse(null),
                                                                      source.getFragmentSource().orElse(null),
                                                                      programSet, properties, blendModeOverride);
                    ((ProgramDirectivesAccessor) processedSource.getDirectives()).setFlwAlphaTestOverride(
                            new AlphaTest(AlphaTestFunction.GREATER, ctx.alphaDiscard));

                    Shader override = null;
                    try {
                        String randomId = MathHelper.randomUuid().toString();
                        if (isShadow) {
                            override = ((NewWorldRenderingPipelineAccessor) pipeline).callCreateShadowShader(
                                    String.format("shadow_flw_%s_%s_%s", ctx.spec.name.getNamespace(),
                                                  ctx.spec.name.getPath(), randomId), processedSource, AlphaTest.ALWAYS,
                                    VertexFormats.POSITION_TEXTURE, false, false);
                        } else {
                            override = ((NewWorldRenderingPipelineAccessor) pipeline).callCreateShader(
                                    String.format("gbuffers_flw_%s_%s_%s", ctx.spec.name.getNamespace(),
                                                  ctx.spec.name.getPath(), randomId), processedSource, AlphaTest.ALWAYS,
                                    VertexFormats.POSITION_TEXTURE, FogMode.OFF, false, false);
                        }

                    } catch (Exception exception) {
                        IrisFlw.LOGGER.error("Fail to compile shader", exception);
                    }

                    if (override != null) {
                        P program = factory.create(ctx.spec.name, override.getProgramRef());
                        ((WorldProgramAccessor) program).setShader(override);
                        return program;
                    }
                }

                //programCache.put(ctx,new IrisShaderProgram(ctx.spec.name, override));
                //return new IrisShaderProgram(ctx.spec.name, override);
            }
        }
        return null;
    }

    @Inject(method = "invalidate", remap = false, at = @At("TAIL"))
    private void injectInvalidate(CallbackInfo ci) {
        programCache.clear();
        shadowProgramCache.clear();
        // todo remove cache when shader pack reloaded.
    }
}
