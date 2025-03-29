package top.leonx.irisflw.mixin.flw;

import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.compile.core.FailedCompilation;
import dev.engine_room.flywheel.backend.compile.core.ShaderResult;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.SourceFile;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = Compilation.class)
public class MixinCompilation {

    @Final
    @Shadow()
    private StringBuffer fullSource;

    @Final
    @Shadow(remap = false)
    private StringBuffer generatedSource;

    @Final
    @Shadow(remap = false)
    private List<SourceFile> files;

    @Invoker
    private static void callDumpSource(String source, String fileName)
    {

    }

    @Inject(method = "compile",
            at = @At(value = "HEAD"),
            remap = false, cancellable = true)
    public void irisflw$compile(ShaderType shaderType, String name, CallbackInfoReturnable<ShaderResult> cir)
    {
        String source = this.fullSource.toString();
        try{
//            if (IrisApi.getInstance().isShaderPackInUse()) {
//                //Optional<ShaderPack> currentPackOptional = Iris.getCurrentPack();
//                WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
//                boolean isShadow = IrisApi.getInstance().isRenderingShadowPass();
//
//                P program = irisProgramCompiler.getProgram(ctx,isShadow);
//                if (program != null)
//                    cir.setReturnValue(program);
//            }
        }catch (Exception e){
//            IrisFlw.LOGGER.error("Fail to create iris shader with "+ctx.toString(),e);
        }

        int handle = GL20.glCreateShader(shaderType.glEnum);
        GlCompat.safeShaderSource(handle, source);
        GL20.glCompileShader(handle);
        String shaderName = name + "." + shaderType.extension;
        callDumpSource(source, shaderName);
        String infoLog = GL20.glGetShaderInfoLog(handle);
        if (Compilation.compiledSuccessfully(handle)) {
            cir.setReturnValue(ShaderResult.success(new GlShader(handle, shaderType, shaderName), infoLog));
        } else {
            GL20.glDeleteShader(handle);
            cir.setReturnValue(ShaderResult.failure(new FailedCompilation(shaderName, this.files, this.generatedSource.toString(), source, infoLog)));
        }
        cir.cancel();
    }
}
