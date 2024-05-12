package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.blending.BlendModeStorage;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.WorldProgramAccessor;
import top.leonx.irisflw.flywheel.IrisFlwCompatShaderWarp;

@Mixin(WorldProgram.class)
public abstract class MixinWorldProgram extends GlProgram implements WorldProgramAccessor {
    @Unique
    private IrisFlwCompatShaderWarp shader;

    protected MixinWorldProgram(ResourceLocation name, int handle) {
        super(name, handle);
    }

    public void setShader(IrisFlwCompatShaderWarp shader){
        this.shader = shader;
        setHandle(shader.getProgramHandle());
    }

    @Inject(method = "bind",remap = false,at = @At("HEAD"))
    public void bind(CallbackInfo ci){
        //To ensure the program has been activated.
        //If the Shader.activeShaderId is equal to the programId, the GlProgramManager.useProgram won't be called.
        //Because nobody calls the unbind method of the shader.
        GlStateManager._glUseProgram(this.handle());
        if(this.shader!=null){
            shader.bind();
            BlendModeStorage.restoreBlend();
        }
    }

    @Unique
    public void instanceUnbind(){
        //Currently, this method has never been called.
        //So, the shader never been unbounded.
        //That's why I invoke the BlendModeStorage.restoreBlend in bind method,
        // otherwise the blend won't be enabled when Render Entity Outline, witch resulting in a completely black screen.
        if(this.shader!=null)
            this.shader.unbind();
    }
}
