package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;
import top.leonx.irisflw.iris.BufferBuilderStateManager;

@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder implements BufferBuilderAccessor {

    @Unique
    private boolean isFlyWheelBufferBuilder;

    // There will be an error when building, never mind.
    // This shadow field provided by Iris.

    private boolean extending;

    @Override
    public void setIsFlyWheelBufferBuilder(boolean isFlyWheel) {
        this.isFlyWheelBufferBuilder = isFlyWheel;
    }

    /*@Inject(method = "begin", at = @At(value = "FIELD",target = "com/mojang/blaze3d/vertex/BufferBuilder.building:Z"))
    private void iris$onBegin(VertexFormat.Mode drawMode, VertexFormat format, CallbackInfo ci) {
        extending = extending && BufferBuilderStateManager.isAllowExtend();
    }*/

    @ModifyVariable(method = "begin", at = @At("HEAD"), argsOnly = true)
    private VertexFormat iris$extendFormat(VertexFormat format) {
        if (!BufferBuilderStateManager.isAllowExtend()) {
            extending = false;
            if (IrisApi.getInstance().isShaderPackInUse())
                return DefaultVertexFormat.BLOCK;
        }
        return format;
    }
}
