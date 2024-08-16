package top.leonx.irisflw.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;
import top.leonx.irisflw.iris.BufferBuilderStateManager;

@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder implements BufferBuilderAccessor {

    @SuppressWarnings("unused")
    @Unique
    private boolean irisFlw$isFlywheelBufferBuilder;

    // This field is from Iris's mixin.
    private boolean extending;

    @Override
    public void irisflw$setIsFlyWheelBufferBuilder(boolean isFlyWheel) {
        this.irisFlw$isFlywheelBufferBuilder = isFlyWheel;
    }

    @ModifyVariable(method = "begin", at = @At("HEAD"), argsOnly = true)
    private VertexFormat irisflw$begin(VertexFormat format) {
        // IrisFlw.isUsingExtendedVertexFormat() equals true means that we are using the extended vertex format.
        // BufferBuilderStateManager.isAllowExtend() equals true means that this BufferBuilder is not a Flywheel BufferBuilder, we don't need to handle it.
        // In these two cases, we should return the original format.
        // The Iris will then handle it and set to the correct format.
        if (IrisFlw.isUsingExtendedVertexFormat() || BufferBuilderStateManager.isAllowExtend()) {
            return format;
        }

        // Otherwise, we are using DefaultVertexFormat.BLOCK.
        // Notice that we only handle the format when shader pack is in use.
        extending = false;
        if (IrisFlw.isShaderPackInUse())
            return DefaultVertexFormat.BLOCK;
        return format;
    }
}
