package top.leonx.irisflw.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * Custom RenderType for ghost translucent rendering when IrisFlw INSTANCING is active.
 * Uses RENDERTYPE_SOLID_SHADER (Iris maps to gbuffers_terrain) with TRANSLUCENT_TRANSPARENCY
 * for alpha blending at the OpenGL level. Block vertex format matches what GhostBlockRenderer
 * outputs via BakedModelBufferer.
 * <p>
 * Strategy: gbuffers_terrain is the most universally stable shaderpack program — ALL
 * shaderpacks handle terrain rendering correctly. The solid shader multiplies vertex color
 * alpha into the output, and TRANSLUCENT_TRANSPARENCY handles the alpha blend in GL.
 * No custom shader files needed — uses built-in RENDERTYPE_SOLID_SHADER.
 */
public final class IrisFlwRenderTypes {
  private IrisFlwRenderTypes() {}

  private static final RenderType GHOST_TRANSLUCENT = RenderType.create(
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
    return GHOST_TRANSLUCENT;
  }
}
