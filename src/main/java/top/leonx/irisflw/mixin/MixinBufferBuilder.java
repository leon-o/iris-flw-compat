package top.leonx.irisflw.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;

@Mixin(value = BufferBuilder.class,priority = 1010)
public class MixinBufferBuilder implements BufferBuilderAccessor {

    @Unique
    private boolean isFlyWheelBufferBuilder;

    // There will be an error when building, never mind.
    // This shadow field provided by Iris.
    @SuppressWarnings("MixinAnnotationTarget")
    @Shadow
    private boolean extending;

    @Override
    public void setIsFlyWheelBufferBuilder(boolean isFlyWheel) {
        this.isFlyWheelBufferBuilder = isFlyWheel;
    }

    @Inject(method = "begin", at = @At(value = "FIELD",target = "Lnet/minecraft/client/render/BufferBuilder;building:Z"))
    private void iris$onBegin(VertexFormat.DrawMode drawMode, VertexFormat format, CallbackInfo ci) {
        extending = extending && !isFlyWheelBufferBuilder;
    }
}
