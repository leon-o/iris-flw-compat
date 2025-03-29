package top.leonx.irisflw.fabric.mixin;

/*import com.jozufozu.flywheel.core.model.WorldModelBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WorldModelBuilder.class)
public class MixinWorldModelBuilder {

    @Unique
    private final Object2IntMap<BlockState> blockStateIds = irisflw$getBlockStateIds();

    @Unique
    private Object2IntMap<BlockState> irisflw$getBlockStateIds() {
        return WorldRenderingSettings.INSTANCE.getBlockStateIds();
    }

    @Unique
    private short irisflw$resolveBlockId(BlockState state) {
        if (blockStateIds == null) {
            return -1;
        }

        return (short) blockStateIds.getOrDefault(state, -1);
    }
    @Redirect(method = "bufferInto(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/block/ModelBlockRenderer;Lnet/minecraft/util/RandomSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;" +
                    "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;" +
                    "Lnet/minecraft/client/resources/model/BakedModel;" +
                    "Lnet/minecraft/world/level/block/state/BlockState;" +
                    "Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;" +
                    "Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"), require = 0)
    public void irisflw$bufferInto(ModelBlockRenderer instance, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay) {

        // The WorldModelBuilder is used to buffer the Contraption's block models vertex data.

        // When the bufferInto method is called, we need to check if the VertexConsumer is an instance of BlockSensitiveBufferBuilder that added by Iris.
        // If it is, we need to call the beginBlock to pass the block id and render type to the buffer builder.
        // So the shader of flywheel can get the block id and render type.


        if(consumer instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder){
            var blockId = irisflw$resolveBlockId(state);
            blockSensitiveBufferBuilder.beginBlock(blockId, ExtendedDataHelper.BLOCK_RENDER_TYPE, pos.getX(), pos.getY(), pos.getZ());
            instance.tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
            blockSensitiveBufferBuilder.endBlock();
        }else{
            instance.tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        }
    }
}*/
