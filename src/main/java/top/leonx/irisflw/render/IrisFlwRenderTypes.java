package top.leonx.irisflw.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.transformer.GhostBlockShaderPatcher;

/**
 * Custom RenderType for ghost translucent rendering when IrisFlw INSTANCING is active.
 * <p>
 * Primary path: uses a Bayer-dithered gbuffers_block shader (patched via Iris pipeline).
 * Bayer ordered dithering creates a screen-door translucency effect using discard instead
 * of alpha blend — works with both forward (BSL) and deferred (Photon) rendering.
 * <p>
 * Fallback path: uses RENDERTYPE_SOLID_SHADER (Iris → gbuffers_terrain) with
 * TRANSLUCENT_TRANSPARENCY for alpha blend at the OpenGL level.
 */
public final class IrisFlwRenderTypes {
  private IrisFlwRenderTypes() {}

  /** Bayer-dithered ghost translucent (primary path). */
  private static final RenderType GHOST_BAYER = RenderType.create(
      "irisflw:ghost_translucent",
      DefaultVertexFormat.BLOCK,
      VertexFormat.Mode.QUADS,
      256,
      false,
      true,
      RenderType.CompositeState.builder()
          .setShaderState(new RenderStateShard.ShaderStateShard(GhostBlockShaderPatcher::getShader))
          .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
          .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
          .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
          .setLightmapState(RenderStateShard.LIGHTMAP)
          .setOverlayState(RenderStateShard.OVERLAY)
          .createCompositeState(false)
  );

  /** Fallback: RENDERTYPE_SOLID_SHADER with alpha blend (Plan 2). */
  private static final RenderType GHOST_SOLID_FALLBACK = RenderType.create(
      "irisflw:ghost_translucent",
      DefaultVertexFormat.BLOCK,
      VertexFormat.Mode.QUADS,
      256,
      false,
      true,
      RenderType.CompositeState.builder()
          .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
          .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
          .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
          .setWriteMaskState(RenderStateShard.COLOR_WRITE)
          .setLightmapState(RenderStateShard.LIGHTMAP)
          .setOverlayState(RenderStateShard.OVERLAY)
          .createCompositeState(false)
  );

  public static RenderType ghostTranslucent() {
    if (IrisFlw.useBayerDithering) {
      ShaderInstance patched = GhostBlockShaderPatcher.getShader();
      if (patched != null) {
        return GHOST_BAYER;
      }
    }
    return GHOST_SOLID_FALLBACK;
  }
}
