package top.leonx.irisflw.neoforge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import com.simibubi.create.content.contraptions.minecart.CouplingRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler;
import com.simibubi.create.content.trains.entity.CarriageCouplingRenderer;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.foundation.events.ClientEvents;
import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.backend.BackendManager;
import dev.engine_room.flywheel.impl.FlywheelNeoForge;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.leonx.irisflw.backend.IrisFlwBackends;

@Mixin(value = ClientEvents.class, remap = false)
public class MixinCreateClientEvents {
    @Inject(method = "onRenderWorld", at = @At("HEAD"), require = 0, cancellable = true)
    private static void irisflw$OnRenderWorld(RenderLevelStageEvent event, CallbackInfo ci)
    {
        if (BackendManager.currentBackend() == IrisFlwBackends.INSTANCING) {
            if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
            {
                PoseStack ms = event.getPoseStack();
                ms.pushPose();
                SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                TrackBlockOutline.drawCurveSelection(ms, buffer, camera);
                TrackTargetingClient.render(ms, buffer, camera);
                CouplingRenderer.renderAll(ms, buffer, camera);
                CarriageCouplingRenderer.renderAll(ms, buffer, camera);
                CreateClient.SCHEMATIC_HANDLER.render(ms, buffer, camera);
                ChainConveyorInteractionHandler.drawCustomBlockSelection(ms, buffer, camera);
                buffer.draw();
                RenderSystem.enableCull();
                ms.popPose();
                ContraptionPlayerPassengerRotation.frame();
                ci.cancel();
            }else if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
                ci.cancel();
            }
        }
    }
}
