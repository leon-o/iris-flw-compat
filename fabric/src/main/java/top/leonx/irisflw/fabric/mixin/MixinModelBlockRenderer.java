package top.leonx.irisflw.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModelBlockRenderer.class)
public class MixinModelBlockRenderer {


    @Unique
    private short irisflw$resolveBlockId(BlockState state) {
        var blockStateIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        if (blockStateIds == null) {
            return -1;
        }

        return (short) blockStateIds.getOrDefault(state, -1);
    }

    @Inject(method = "tesselateBlock", at = @At("HEAD"))
    private void irisflw$tesselateBlock$head(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, CallbackInfo ci) {
        if(consumer instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder) {
            blockSensitiveBufferBuilder.beginBlock(irisflw$resolveBlockId(state), (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Inject(method = "tesselateBlock", at = @At("RETURN"))
    private void irisflw$tesselateBlock$tail(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, CallbackInfo ci) {
        if(consumer instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder) {
            blockSensitiveBufferBuilder.endBlock();
        }
    }
}
