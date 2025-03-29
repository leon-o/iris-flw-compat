package top.leonx.irisflw.mixin.flw.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;

@Mixin(value = BufferBuilder.class, priority = 1010)
public class MixinBufferBuilder implements BufferBuilderAccessor {

    @SuppressWarnings("unused")
    @Unique
    private boolean irisFlw$forceBlockFormat;

    // This field is from Iris's mixin.
    private boolean extending;

    @Override
    public void irisflw$setForceBlockFormat(boolean isFlyWheel) {
        this.irisFlw$forceBlockFormat = isFlyWheel;
    }

    /*@ModifyVariable(method = "begin", at = @At("HEAD"), argsOnly = true)
    private VertexFormat irisflw$begin(VertexFormat format) {
        // If forceBlockFormat is true, we set the format to BLOCK.
        // Notice that we only handle the format when shader pack is in use.
        if (irisFlw$forceBlockFormat && IrisFlw.isShaderPackInUse())
        {
            extending = false;
            return DefaultVertexFormat.BLOCK;
        }

        // If we are using extended vertex format, we should leave the format as it is.
        // Since the Iris's mixin will handle the format.
        return format;
    }*/
}
