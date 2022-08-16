package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.AlphaTestFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.*;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.compiler.AutoInsertProgramCompiler;
import top.leonx.irisflw.compiler.IrisProgramCompilerBase;
import top.leonx.irisflw.transformer.TemplatePreprocessor;
import top.leonx.irisflw.accessors.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(value = ProgramCompiler.class, remap = false)
public abstract class MixinProgramCompiler<P extends WorldProgram> {

    private IrisProgramCompilerBase<P> irisProgramCompiler;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void injectInit(GlProgram.Factory<P> factory, VertexCompiler vertexCompiler, FragmentCompiler fragmentCompiler, CallbackInfo ci) {
        VertexCompilerAccessor vertexCompilerAccessor = (VertexCompilerAccessor) vertexCompiler;
        Template<? extends VertexData> template = vertexCompilerAccessor.getTemplate();
        irisProgramCompiler = new AutoInsertProgramCompiler<>(factory,template,vertexCompilerAccessor.getHeader());
    }

    @Inject(method = "getProgram", at = @At("HEAD"), remap = false, cancellable = true)
    public void getProgram(ProgramContext ctx, CallbackInfoReturnable<P> cir) {

        if (IrisApi.getInstance().isShaderPackInUse()) {
            //Optional<ShaderPack> currentPackOptional = Iris.getCurrentPack();
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            boolean isShadow = IrisApi.getInstance().isRenderingShadowPass();

            P program = irisProgramCompiler.getProgram(ctx,isShadow);
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

    @Inject(method = "invalidate", remap = false, at = @At("TAIL"))
    private void injectInvalidate(CallbackInfo ci) {
        irisProgramCompiler.clear();
        // todo remove cache when shader pack reloaded.
    }
}
