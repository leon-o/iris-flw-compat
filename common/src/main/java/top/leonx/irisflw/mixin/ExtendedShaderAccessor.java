package top.leonx.irisflw.mixin;

import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExtendedShader.class,remap = false)
public interface ExtendedShaderAccessor {
    @Accessor
    GlFramebuffer getWritingToBeforeTranslucent();
}
