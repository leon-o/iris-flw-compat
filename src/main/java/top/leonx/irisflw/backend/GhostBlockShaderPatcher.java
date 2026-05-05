package top.leonx.irisflw.backend;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.minecraft.client.renderer.ShaderInstance;
import org.slf4j.Logger;
import top.leonx.irisflw.IrisFlw;
import top.leonx.irisflw.accessors.IrisRenderingPipelineAccessor;
import top.leonx.irisflw.accessors.ProgramSourceAccessor;
import top.leonx.irisflw.transformer.BayerFragmentPatcher;

import java.util.Optional;

/**
 * Lazily creates a patched ShaderInstance for ghost block rendering.
 * <p>
 * Takes the shaderpack's gbuffers_block program, injects Bayer ordered dithering
 * discard into the fragment shader, and compiles it through Iris's shader pipeline.
 * Caches the result for the lifetime of the shaderpack.
 * <p>
 * Automatically detects shaderpack changes by tracking the IrisRenderingPipeline
 * reference — when the pipeline changes (shaderpack reload), the cached shader is
 * invalidated and re-created on the next render frame.
 */
public final class GhostBlockShaderPatcher {
    private static final Logger LOGGER = IrisFlw.LOGGER;
    private static ShaderInstance cachedShader;
    private static boolean attemptMade;
    private static WorldRenderingPipeline pipelineAtCreation;

    private GhostBlockShaderPatcher() {}

    /**
     * Returns a patched ShaderInstance with Bayer dithering, or null if
     * no shaderpack is loaded or shader creation fails.
     * Automatically detects shaderpack changes and re-creates the patched shader.
     */
    public static ShaderInstance getShader() {
        WorldRenderingPipeline currentPipeline = Iris.getPipelineManager().getPipelineNullable();

        // Detect shaderpack reload: pipeline reference changed → stale cache
        if (attemptMade && pipelineAtCreation != null && currentPipeline != pipelineAtCreation) {
            LOGGER.info("Shaderpack changed, resetting Bayer ghost block shader");
            cachedShader = null;
            attemptMade = false;
            pipelineAtCreation = null;
        }

        if (attemptMade) return cachedShader;
        attemptMade = true;
        pipelineAtCreation = currentPipeline;

        if (currentPipeline == null) return null;

        try {
            if (!(currentPipeline instanceof IrisRenderingPipelineAccessor accessor)) {
                LOGGER.debug("Iris rendering pipeline not accessible, ghost block Bayer patching skipped");
                return null;
            }

            ProgramSet programSet = accessor.getProgramSet();
            ProgramFallbackResolver resolver = new ProgramFallbackResolver(programSet);
            Optional<ProgramSource> refOpt = resolver.resolve(ProgramId.Block);
            if (refOpt.isEmpty()) {
                LOGGER.warn("No gbuffers_block program in shaderpack, ghost block Bayer patching skipped");
                return null;
            }

            ProgramSource ref = refOpt.get();
            String vertexSource = ref.getVertexSource().orElseThrow();
            String fragSource = ref.getFragmentSource().orElseThrow();

            String patchedFrag = BayerFragmentPatcher.patch(fragSource);

            ShaderProperties properties = ((ProgramSourceAccessor) ref).getShaderProperties();
            BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) ref).getBlendModeOverride();

            ProgramSource patchedSource = new ProgramSource(
                    "gbuffers_flw_ghost_block",
                    vertexSource,
                    ref.getGeometrySource().orElse(null),
                    null,
                    null,
                    patchedFrag,
                    programSet,
                    properties,
                    blendModeOverride
            );

            cachedShader = accessor.callCreateShader(
                    "gbuffers_flw_ghost_block",
                    patchedSource,
                    ProgramId.Block,
                    AlphaTest.ALWAYS,
                    DefaultVertexFormat.BLOCK,
                    FogMode.OFF,
                    false, false, false, false, false
            );

            LOGGER.info("Created Bayer-dithered ghost block shader from shaderpack's gbuffers_block");
            return cachedShader;
        } catch (Exception e) {
            LOGGER.error("Failed to create Bayer-dithered ghost block shader", e);
            return null;
        }
    }

    /** Reset for shaderpack reload. */
    public static void reset() {
        cachedShader = null;
        attemptMade = false;
        pipelineAtCreation = null;
    }
}
