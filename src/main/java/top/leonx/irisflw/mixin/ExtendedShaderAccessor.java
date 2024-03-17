package top.leonx.irisflw.mixin;

import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExtendedShader.class,remap = false)
public interface ExtendedShaderAccessor {
    @Accessor
    GlFramebuffer getWritingToBeforeTranslucent();
}
