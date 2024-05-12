package top.leonx.irisflw.mixin;


import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;

import java.io.IOException;

@Mixin(IrisRenderingPipeline.class)
public abstract class MixinIrisRenderingPipeline implements IrisRenderingPipelineAccessor {

    @Unique
    private ProgramSet programSet;

    @Override
    public ProgramSet getProgramSet(){
        return programSet;
    }

    @Inject(method = "<init>",at = @At("TAIL"),remap = false)
    public void initSet(ProgramSet set, CallbackInfo callbackInfo){
        programSet = set;
    }


    @Invoker(remap = false)
    @Override
    public abstract ShaderInstance callCreateShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                                    VertexFormat vertexFormat, FogMode fogMode,
                                                    boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText) throws IOException;

    @Invoker(remap = false)
    @Override
    public abstract ShaderInstance callCreateShadowShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha,
                                                          VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText) throws IOException;
}
