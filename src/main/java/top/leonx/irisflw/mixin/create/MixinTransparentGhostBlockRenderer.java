package top.leonx.irisflw.mixin.create;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import top.leonx.irisflw.render.IrisFlwRenderTypes;
import dev.engine_room.flywheel.api.backend.BackendManager;
import top.leonx.irisflw.backend.IrisFlwBackends;

/**
 * Redirects RenderType.translucent() to IrisFlwRenderTypes.ghostTranslucent()
 * when the IrisFlw instancing backend is active. The replacement uses
 * RENDERTYPE_SOLID_SHADER (Iris → gbuffers_terrain) with translucent blend,
 * avoiding BSL's gbuffers_water discard and Photon's deferred issues.
 */
@Mixin(targets = "net.createmod.catnip.ghostblock.GhostBlockRenderer$TransparentGhostBlockRenderer", remap = false)
public class MixinTransparentGhostBlockRenderer {
  @Redirect(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;translucent()Lnet/minecraft/client/renderer/RenderType;")
  )
  private static RenderType irisflw$RenderTypeTranslucent() {
    try {
      if (BackendManager.currentBackend() == IrisFlwBackends.INSTANCING) {
        return IrisFlwRenderTypes.ghostTranslucent();
      }
    } catch (Throwable t) {
      // Fall back to original translucent on any runtime issue
    }
    return RenderType.translucent();
  }
}
