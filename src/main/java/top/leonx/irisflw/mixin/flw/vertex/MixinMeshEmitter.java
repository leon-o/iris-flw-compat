package top.leonx.irisflw.mixin.flw.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.material.Material;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.MeshEmitter", remap = false)
public abstract class MixinMeshEmitter implements VertexConsumer, BlockSensitiveBufferBuilder {

    @Shadow
    private BufferBuilder @UnknownNullability [] bufferBuilders = new BufferBuilder[1];
    @Shadow
    private int currentIndex = 0;

    @Unique
    private int irisflw$currentBlock = -1;
    @Unique
    private byte irisflw$currentRenderType = -1;
    @Unique
    private byte irisflw$blockEmission = 0;
    @Unique
    private int irisflw$currentLocalPosX;
    @Unique
    private int irisflw$currentLocalPosY;
    @Unique
    private int irisflw$currentLocalPosZ;

    @Inject(method = "getBuffer(Ldev/engine_room/flywheel/api/material/Material;)Lcom/mojang/blaze3d/vertex/BufferBuilder;", at = @At("TAIL"), require = 0)
    public void irisflw$getBuffer(Material material, CallbackInfoReturnable<BufferBuilder> cir)
    {
        if(cir.getReturnValue() instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
        {
            // new created buffer builder, need to beginBlock
            blockSensitiveBufferBuilder.beginBlock(this.irisflw$currentBlock, this.irisflw$currentRenderType, this.irisflw$blockEmission, this.irisflw$currentLocalPosX, this.irisflw$currentLocalPosY, this.irisflw$currentLocalPosZ);
        }
    }

    @Override
    public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
        this.irisflw$currentBlock = block;
        this.irisflw$currentRenderType = renderType;
        this.irisflw$blockEmission = blockEmission;
        this.irisflw$currentLocalPosX = localPosX;
        this.irisflw$currentLocalPosY = localPosY;
        this.irisflw$currentLocalPosZ = localPosZ;

        for(int i = 0; i <= this.currentIndex; i++)
        {
            if(this.bufferBuilders[i] instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
            {
                blockSensitiveBufferBuilder.beginBlock(block, renderType, blockEmission, localPosX, localPosY, localPosZ);
            }
        }
    }

    @Override
    public void endBlock() {
        this.irisflw$currentBlock = -1;
        this.irisflw$currentRenderType = -1;
        this.irisflw$currentLocalPosX = 0;
        this.irisflw$currentLocalPosY = 0;
        this.irisflw$currentLocalPosZ = 0;

        for(int i = 0; i <= this.currentIndex; i++)
        {
            if(this.bufferBuilders[i] instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
            {
                blockSensitiveBufferBuilder.endBlock();
            }
        }
    }
}
