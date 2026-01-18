package top.leonx.irisflw.mixin.flw.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.leonx.irisflw.IrisFlw;

@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.MeshEmitter", remap = false)
public abstract class MixinMeshEmitter implements VertexConsumer, BlockSensitiveBufferBuilder {

    @Shadow
    private BufferBuilder @UnknownNullability [] bufferBuilders = new BufferBuilder[1];
    @Shadow
    private int currentIndex = 0;

    @Unique
    private int currentBlock = -1;
    @Unique
    private byte currentRenderType = -1;
    @Unique
    private byte blockEmission = 0;
    @Unique
    private int currentLocalPosX;
    @Unique
    private int currentLocalPosY;
    @Unique
    private int currentLocalPosZ;

    @Unique
    private BlockSensitiveBufferBuilder currentBlockSensitiveBufferBuilder;

//    @Inject(method = "prepareForGeometry(Z)V", at = @At("TAIL"), require = 0)
//    public void irisflw$prepareForGeometry(boolean shade, CallbackInfo ci)
//    {
//        if(this.bufferBuilder instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
//        {
//            blockSensitiveBufferBuilder.beginBlock(this.currentBlock, this.currentRenderType, (byte) 0, this.currentLocalPosX, this.currentLocalPosY, this.currentLocalPosZ);
//            currentBlockSensitiveBufferBuilder = blockSensitiveBufferBuilder;
//        }
//    }

    @Inject(method = "getBuffer(Ldev/engine_room/flywheel/api/material/Material;)Lcom/mojang/blaze3d/vertex/BufferBuilder;", at = @At("TAIL"), require = 0)
    public void irisflw$getBuffer(Material material, CallbackInfoReturnable<BufferBuilder> cir)
    {
        if(cir.getReturnValue() instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
        {
            // new created buffer builder, need to beginBlock
            blockSensitiveBufferBuilder.beginBlock(this.currentBlock, this.currentRenderType, this.blockEmission, this.currentLocalPosX, this.currentLocalPosY, this.currentLocalPosZ);
            currentBlockSensitiveBufferBuilder = blockSensitiveBufferBuilder;
        }
    }

//    @Inject(method = "prepareForBlock()V", at = @At("TAIL"))
//    public void prepareForBlock(CallbackInfo ci)
//    {
//        for(int i = 0; i <= this.currentIndex; i++)
//        {
//            if(this.bufferBuilders[i] instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
//            {
//                blockSensitiveBufferBuilder.endBlock();
//            }
//        }
//    }

    @Override
    public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
        this.currentBlock = block;
        this.currentRenderType = renderType;
        this.blockEmission = blockEmission;
        this.currentLocalPosX = localPosX;
        this.currentLocalPosY = localPosY;
        this.currentLocalPosZ = localPosZ;

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
        this.currentBlock = -1;
        this.currentRenderType = -1;
        this.currentLocalPosX = 0;
        this.currentLocalPosY = 0;
        this.currentLocalPosZ = 0;

        for(int i = 0; i <= this.currentIndex; i++)
        {
            if(this.bufferBuilders[i] instanceof BlockSensitiveBufferBuilder blockSensitiveBufferBuilder)
            {
                blockSensitiveBufferBuilder.endBlock();
            }
        }
    }
}
