package top.leonx.irisflw.mixin;


import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.NewWorldRenderingPipelineAccessor;

import java.io.IOException;

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
    }


    @Invoker(remap = false)
    @Override
    public abstract ShaderInstance callCreateShader(String name, ProgramSource source, ProgramId programId, AlphaTest fallbackAlpha, VertexFormat vertexFormat, FogMode fogMode, boolean isIntensity, boolean isFullbright) throws IOException;

    @Invoker(remap = false)
    @Override
    public abstract ShaderInstance callCreateShadowShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat, boolean isIntensity, boolean isFullbright) throws IOException;
}
