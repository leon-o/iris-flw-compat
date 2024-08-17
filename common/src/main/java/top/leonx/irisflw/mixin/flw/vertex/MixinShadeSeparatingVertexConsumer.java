package top.leonx.irisflw.mixin.flw.vertex;

import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.BufferBuilderAccessor;

@Mixin(ShadeSeparatingVertexConsumer.class)
public class MixinShadeSeparatingVertexConsumer implements BlockSensitiveBufferBuilder {

    // Mixin this class to implement BlockSensitiveBufferBuilder interface
    // which allows us to pass the block id and render type to the buffer builder.

    @Shadow
    protected VertexConsumer shadedConsumer;

    @Shadow
    protected VertexConsumer unshadedConsumer;

    @Inject(method = "prepare", at = @At("TAIL"))
    public void irisflw$prepare(VertexConsumer shadedConsumer, VertexConsumer unshadedConsumer, CallbackInfo ci) {
        if(!IrisFlw.isUsingExtendedVertexFormat())
        {
            if (shadedConsumer instanceof BufferBuilderAccessor bufferBuilderAccessor) {
                bufferBuilderAccessor.irisflw$setForceBlockFormat(true);
            }
            if (unshadedConsumer instanceof BufferBuilderAccessor bufferBuilderAccessor) {
                bufferBuilderAccessor.irisflw$setForceBlockFormat(true);
            }
        }
    }

    @Override
    public void beginBlock(short blockId, short renderType, int blockX, int blockY, int blockZ) {
        if (shadedConsumer instanceof BlockSensitiveBufferBuilder sensitiveBufferBuilder) {
            sensitiveBufferBuilder.beginBlock(blockId, renderType, blockX, blockY, blockZ);
        }
        if (unshadedConsumer instanceof BlockSensitiveBufferBuilder sensitiveBufferBuilder) {
            sensitiveBufferBuilder.beginBlock(blockId, renderType, blockX, blockY, blockZ);
        }
    }

    @Override
    public void endBlock() {
        if (shadedConsumer instanceof BlockSensitiveBufferBuilder sensitiveBufferBuilder) {
            sensitiveBufferBuilder.endBlock();
        }
        if (unshadedConsumer instanceof BlockSensitiveBufferBuilder sensitiveBufferBuilder) {
            sensitiveBufferBuilder.endBlock();
        }
    }
}
