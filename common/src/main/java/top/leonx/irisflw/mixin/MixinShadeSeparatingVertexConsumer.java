package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShadeSeparatingVertexConsumer.class)
public class MixinShadeSeparatingVertexConsumer implements BlockSensitiveBufferBuilder {

    // Mixin this class to implement BlockSensitiveBufferBuilder interface
    // which allows us to pass the block id and render type to the buffer builder.

    @Shadow
    protected VertexConsumer shadedConsumer;

    @Shadow
    protected VertexConsumer unshadedConsumer;

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
