package top.leonx.irisflw.mixin;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.NewWorldRenderingPipelineAccessor;

@Mixin(NewWorldRenderingPipeline.class)
public abstract class MixinNewWorldRenderingPipeline implements NewWorldRenderingPipelineAccessor {

    @Unique
    private ProgramSet programSet;

    @Override
    public ProgramSet getProgramSet(){
        return programSet;
    }

    @Inject(method = "<init>",at = @At("TAIL"),remap = false)
    public void initSet(ProgramSet set, CallbackInfo callbackInfo){
        programSet = set;
//        addIrisShaderProgram(ProgramContext.create(((Instanced) Materials.TRANSFORMED).getProgramSpec(),
//                                                   Formats.POS_TEX_NORMAL, RenderLayer.SOLID));
    }


    @Invoker(remap = false)
    @Override
    public abstract Shader callCreateShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
                            VertexFormat vertexFormat, FogMode fogMode,
                            boolean isIntensity, boolean isFullbright);

    @Invoker(remap = false)
    @Override
    public abstract Shader callCreateShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha,
                                  VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright);

//    @Override
//    public IrisShaderProgram getFlwShaderProgram(ProgramContext context) {
//        return programCache.get(context);
//    }
}
