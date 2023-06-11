package top.leonx.irisflw.mixin;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.flywheel.RenderLayerEventStateManager;

@Mixin(value = LevelRenderer.class,priority = 900)
public class MixinLevelRender {

    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    private ClientLevel level;

    @Inject(
            method = {"renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V"},
            at = @At(value = "FIELD",target = "Lnet/minecraft/client/renderer/LevelRenderer;renderedEntities:I",ordinal = 0)
    )
    private void irisflw$renderLayerTail(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo callbackInfo) {
        // Render flywheel block entities in the last.
        // Otherwise, it may cause rendering issue with some shaderpacks.

        RenderLayerEventStateManager.setSkip(false);
        if (!RenderLayerEventStateManager.isRenderingShadow()){
            RenderBuffers renderBuffers = this.renderBuffers;
            Vec3 camPos = camera.getPosition();
            double camX = camPos.x();
            double camY = camPos.y();
            double camZ = camPos.z();
            GlStateTracker.State restoreState = GlStateTracker.getRestoreState();
            MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(level, RenderType.solid(), poseStack, renderBuffers, camX, camY, camZ));
            MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(level, RenderType.cutoutMipped(), poseStack, renderBuffers, camX, camY, camZ));
            MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(level, RenderType.cutout(), poseStack, renderBuffers, camX, camY, camZ));
            restoreState.restore();
        }
    }

    @Inject(
            method = {"renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V"},
            at = @At("HEAD")
    )
    private void irisflw$renderLayerHead(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo callbackInfo) {
        RenderLayerEventStateManager.setSkip(true);
    }
}
