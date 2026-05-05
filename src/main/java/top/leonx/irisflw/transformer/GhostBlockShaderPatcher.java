package top.leonx.irisflw.transformer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.douira.glsl_transformer.ast.query.RootSupplier;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates a patched ShaderInstance for ghost block rendering with screen-door dithering.
 * <p>
 * Takes the shaderpack's gbuffers_block program, injects a custom alpha varying
 * ({@code _if_alpha}) into both vertex and fragment shaders, and compiles through
 * Iris's shader pipeline. The alpha varying carries the ghost block's vertex color
 * alpha for ordered dithering. Supported methods: IGN (Interleaved Gradient Noise),
 * BAYER (8×8 matrix).
 */
public final class GhostBlockShaderPatcher {
    private static final Logger LOGGER = IrisFlw.LOGGER;
    private static ShaderInstance cachedShader;
    private static boolean attemptMade;
    private static WorldRenderingPipeline pipelineAtCreation;

    private GhostBlockShaderPatcher() {}

    // ── Public API ───────────────────────────────────────────────────────

    public static ShaderInstance getShader() {
        WorldRenderingPipeline currentPipeline = Iris.getPipelineManager().getPipelineNullable();

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
                LOGGER.debug("Iris rendering pipeline not accessible");
                return null;
            }

            ProgramSet programSet = accessor.getProgramSet();
            var resolver = new ProgramFallbackResolver(programSet);
            Optional<ProgramSource> refOpt = resolver.resolve(ProgramId.Block);
            if (refOpt.isEmpty()) {
                LOGGER.warn("No gbuffers_block program in shaderpack");
                return null;
            }

            ProgramSource ref = refOpt.get();
            String vertexSource = ref.getVertexSource().orElseThrow();
            String fragSource = ref.getFragmentSource().orElseThrow();

            String patchedVert = VertexPatcher.patch(vertexSource);
            String patchedFrag = FragmentPatcher.patch(fragSource, IrisFlw.ditheringMethod());

            ShaderProperties properties = ((ProgramSourceAccessor) ref).getShaderProperties();
            BlendModeOverride blendModeOverride = ((ProgramSourceAccessor) ref).getBlendModeOverride();

            ProgramSource patchedSource = new ProgramSource(
                    "gbuffers_flw_ghost_block",
                    patchedVert,
                    ref.getGeometrySource().orElse(null),
                    null, null,
                    patchedFrag,
                    programSet, properties, blendModeOverride)
                    .withDirectiveOverride(ref.getDirectives());

            cachedShader = accessor.callCreateShader(
                    "gbuffers_flw_ghost_block", patchedSource,
                    ProgramId.Block, AlphaTest.ALWAYS, DefaultVertexFormat.BLOCK,
                    FogMode.OFF, false, false, false, false, false);

            LOGGER.info("Created dithered ghost block shader");
            return cachedShader;
        } catch (Exception e) {
            LOGGER.error("Failed to create Bayer-dithered ghost block shader", e);
            return null;
        }
    }

    public static void reset() {
        cachedShader = null;
        attemptMade = false;
        pipelineAtCreation = null;
    }

    // ── Shared types ─────────────────────────────────────────────────────

    private static final class ParseParams implements JobParameters {}

    private static final class VertexParams implements JobParameters {
        final String colorAttr;
        VertexParams(String colorAttr) { this.colorAttr = colorAttr; }
    }

    // ══════════════════════════════════════════════════════════════════════
    // VertexPatcher
    // ══════════════════════════════════════════════════════════════════════

    public static final class VertexPatcher {
        private static final Pattern IN_VEC4 = Pattern.compile("in\\s+vec4\\s+(\\w+)\\s*;");

        private VertexPatcher() {}

        public static String patch(String vertexSource) {
            String colorAttr = findColorAttribute(vertexSource);
            if (colorAttr == null) {
                LOGGER.warn("VertexPatcher: no vec4 vertex attribute found, not patching");
                return vertexSource;
            }

            try {
                final SingleASTTransformer<VertexParams> t;
                t = new SingleASTTransformer<>() {{
                    setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
                }};
                t.setTransformation((tree, root, params) -> {
                    tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
                            "out float _if_alpha;");
                    tree.appendMainFunctionBody(t,
                            "_if_alpha = " + params.colorAttr + ".a;");
                });
                LOGGER.info("VertexPatcher: injected _if_alpha = {}.a", colorAttr);
                return t.transform(vertexSource, new VertexParams(colorAttr));
            } catch (Exception e) {
                LOGGER.warn("VertexPatcher: AST injection failed", e);
                return vertexSource;
            }
        }

        private static String findColorAttribute(String source) {
            // Compatibility profile: gl_Color is a built-in available in all
            // compatibility-mode shaders (the vast majority of shaderpacks).
            if (source.contains("gl_Color")) return "gl_Color";

            // Core profile: find explicit "in vec4" vertex color declaration.
            Matcher m = IN_VEC4.matcher(source);
            while (m.find()) {
                String name = m.group(1);
                if (name.startsWith("at_") || name.startsWith("mc_") || name.startsWith("irs_tex")) continue;
                return name;
            }
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FragmentPatcher
    // ══════════════════════════════════════════════════════════════════════

    public static final class FragmentPatcher {
        private FragmentPatcher() {}

        /** Bayer 8×8 matrix. */
        private static final String BAYER_MATRIX = """
            const float _if_bayer[64] = float[64](
                0.0/64.0, 32.0/64.0,  8.0/64.0, 40.0/64.0,  2.0/64.0, 34.0/64.0, 10.0/64.0, 42.0/64.0,
                48.0/64.0, 16.0/64.0, 56.0/64.0, 24.0/64.0, 50.0/64.0, 18.0/64.0, 58.0/64.0, 26.0/64.0,
                12.0/64.0, 44.0/64.0,  4.0/64.0, 36.0/64.0, 14.0/64.0, 46.0/64.0,  6.0/64.0, 38.0/64.0,
                60.0/64.0, 28.0/64.0, 52.0/64.0, 20.0/64.0, 62.0/64.0, 30.0/64.0, 54.0/64.0, 22.0/64.0,
                3.0/64.0, 35.0/64.0, 11.0/64.0, 43.0/64.0,  1.0/64.0, 33.0/64.0,  9.0/64.0, 41.0/64.0,
                51.0/64.0, 19.0/64.0, 59.0/64.0, 27.0/64.0, 49.0/64.0, 17.0/64.0, 57.0/64.0, 25.0/64.0,
                15.0/64.0, 47.0/64.0,  7.0/64.0, 39.0/64.0, 13.0/64.0, 45.0/64.0,  5.0/64.0, 37.0/64.0,
                63.0/64.0, 31.0/64.0, 55.0/64.0, 23.0/64.0, 61.0/64.0, 29.0/64.0, 53.0/64.0, 21.0/64.0
            );""";

        /** Interleaved Gradient Noise — no visible pattern, no texture needed. */
        private static final String IGN_FUNC = """
            float _if_noise(vec2 p) {
                return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
            }""";

        private static final String BAYER_DISCARD = """
            int _if_bx = int(mod(gl_FragCoord.x, 8.0));
            int _if_by = int(mod(gl_FragCoord.y, 8.0));
            if (_if_alpha <= _if_bayer[_if_by * 8 + _if_bx]) discard;""";

        private static final String IGN_DISCARD = """
            if (_if_alpha <= _if_noise(gl_FragCoord.xy)) discard;""";

        /** @param method "IGN" or "BAYER" */
        public static String patch(String fragmentSource, String method) {
            boolean isBayer = "BAYER".equalsIgnoreCase(method);
            try {
                final SingleASTTransformer<ParseParams> t;
                t = new SingleASTTransformer<>() {{
                    setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
                }};
                if (isBayer) {
                    t.setTransformation((tree, root, params) -> {
                        tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
                                "in float _if_alpha;", BAYER_MATRIX);
                        tree.appendMainFunctionBody(t, BAYER_DISCARD);
                    });
                } else {
                    t.setTransformation((tree, root, params) -> {
                        tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
                                "in float _if_alpha;", IGN_FUNC);
                        tree.appendMainFunctionBody(t, IGN_DISCARD);
                    });
                }
                return t.transform(fragmentSource, new ParseParams());
            } catch (Exception e) {
                LOGGER.warn("FragmentPatcher: AST injection failed for method={}", method, e);
                return fragmentSource;
            }
        }
    }
}
