package top.leonx.irisflw.mixin.flw.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.MeshEmitter", remap = false)
public abstract class MixinMeshEmitter implements VertexConsumer, BlockSensitiveBufferBuilder {
    @Shadow private @UnknownNullability BufferBuilder bufferBuilder;
    @Unique
    private int currentBlock = -1;
    @Unique
    private byte currentRenderType = -1;
    @Unique
    private int currentLocalPosX;
    @Unique
    private int currentLocalPosY;
    @Unique
    private int currentLocalPosZ;

    @Inject(method = "prepareForGeometry(Z)V", at = @At("TAIL"))
    public void irisflw$prepareForGeometry(boolean shade, CallbackInfo ci)
    {
        if(this.bufferBuilder instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
        {
            blockSensitiveBufferBuilder.beginBlock(this.currentBlock, this.currentRenderType, (byte) 0, this.currentLocalPosX, this.currentLocalPosY, this.currentLocalPosZ);
        }
    }



    @Override
    public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
        this.currentBlock = block;
        this.currentRenderType = renderType;
        this.currentLocalPosX = localPosX;
        this.currentLocalPosY = localPosY;
        this.currentLocalPosZ = localPosZ;
    }

    @Override
    public void endBlock() {
        this.currentBlock = -1;
        this.currentRenderType = -1;
        this.currentLocalPosX = 0;
        this.currentLocalPosY = 0;
        this.currentLocalPosZ = 0;

        if(this.bufferBuilder instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
        {
            blockSensitiveBufferBuilder.endBlock();
        }
    }
}
