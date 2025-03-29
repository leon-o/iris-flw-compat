package top.leonx.irisflw.mixin.flw;

//import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
//import com.jozufozu.flywheel.core.shader.WorldProgram;

/*@Mixin(WorldProgram.class)
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
}*/
