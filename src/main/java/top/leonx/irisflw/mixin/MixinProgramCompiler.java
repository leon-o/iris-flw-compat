package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.compile.*;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.compiler.IrisProgramCompilerBase;
import top.leonx.irisflw.compiler.NewProgramCompiler;
import top.leonx.irisflw.transformer.GlslTransformerShaderPatcher;

import java.lang.reflect.InvocationTargetException;

@Mixin(value = ProgramCompiler.class, remap = false)
public abstract class MixinProgramCompiler<P extends WorldProgram> {

    private IrisProgramCompilerBase<P> irisProgramCompiler;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void injectInit(GlProgram.Factory<P> factory, VertexCompiler vertexCompiler, FragmentCompiler fragmentCompiler, CallbackInfo ci) {
        VertexCompilerAccessor vertexCompilerAccessor = (VertexCompilerAccessor) vertexCompiler;
        Template<? extends VertexData> template = vertexCompilerAccessor.getTemplate();
        try {
            irisProgramCompiler = new NewProgramCompiler<>(factory, template, vertexCompilerAccessor.getHeader(), GlslTransformerShaderPatcher.class);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            IrisFlw.LOGGER.error("Fail to create program compiler",e);
        }
    }

    @Inject(method = "getProgram", at = @At("HEAD"), remap = false, cancellable = true)
    public void getProgram(ProgramContext ctx, CallbackInfoReturnable<P> cir) {

        try{
            if (IrisApi.getInstance().isShaderPackInUse()) {
                //Optional<ShaderPack> currentPackOptional = Iris.getCurrentPack();
                WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
                boolean isShadow = IrisApi.getInstance().isRenderingShadowPass();

                P program = irisProgramCompiler.getProgram(ctx,isShadow);
                if (program != null) cir.setReturnValue(program);
                else {
                    if (pipeline instanceof IrisRenderingPipeline newPipeline) {
                        ShaderInstance shader = newPipeline.getShaderMap().getShader(ShaderKey.TEXTURED_COLOR);
                        if (shader instanceof ExtendedShader extendedShader) {
                            ((ExtendedShaderAccessor) extendedShader).getWritingToBeforeTranslucent().bind();
                            //Use the same render target with Gbuffers_textured.
                        }
                    }
                }
            }
        }catch (Exception e){
            IrisFlw.LOGGER.error("Fail to create iris shader with "+ctx.toString(),e);
        }

    }

    @Inject(method = "invalidate", remap = false, at = @At("TAIL"))
    private void injectInvalidate(CallbackInfo ci) {
        irisProgramCompiler.clear();
        // todo remove cache when shader pack reloaded.
    }
}
