package top.leonx.irisflw.transformer;

import org.slf4j.Logger;
import top.leonx.irisflw.IrisFlw;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Injects Bayer ordered dithering discard logic into a GLSL fragment shader source.
 * <p>
 * The patcher finds main() and injects an 8×8 Bayer matrix + discard at the end of
 * the function body. Fragments whose output alpha falls below the Bayer threshold
 * are discarded, creating a screen-door translucency effect that works with both
 * forward (BSL) and deferred (Photon) rendering.
 * <p>
 * Alpha source priority:
 * <ol>
 *   <li>Vertex color varying (from vertex shader analysis): correct for ALL shaderpacks
 *       because {@code ColoringVertexConsumer} sets it directly. Detected by finding
 *       the {@code out vec4} in the vertex shader that receives {@code Color}.</li>
 *   <li>Fragment output variable (from fragment source): works for forward shaderpacks
 *       but fails for deferred (gbuffer .a channels are repurposed for PBR data).</li>
 * </ol>
 */
public final class BayerFragmentPatcher {
    private static final Logger LOGGER = IrisFlw.LOGGER;

    private BayerFragmentPatcher() {}

    /** Finds {@code layout(...) out vec4 name;} or {@code out vec4 name;}. */
    private static final Pattern OUT_DECL = Pattern.compile(
            "(?:layout\\s*\\([^)]*\\)\\s*)?out\\s+vec4\\s+(\\w+)\\s*;");

    /**
     * Finds the vertex color varying by analyzing the vertex shader.
     * Returns the varying name with {@code .a} suffix, e.g. {@code "tint.a"}.
     * <p>
     * Strategy: find all {@code out vec4 <name>;} declarations, then check which
     * one is assigned from the {@code Color} vertex attribute within main().
     */
    private static String findVertexColorVarying(String vertexSource) {
        Matcher m = OUT_DECL.matcher(vertexSource);
        while (m.find()) {
            String name = m.group(1);
            // Check if this varying is assigned from the Color attribute
            // Pattern: name = Color[.rgba] or name = Color * ...
            if (Pattern.compile("\\b" + Pattern.quote(name) + "\\s*=\\s*Color\\b").matcher(vertexSource).find()) {
                LOGGER.info("BayerFragmentPatcher: detected vertex color varying '{}' from vertex shader", name);
                return name + ".a";
            }
        }
        // Fallback: return the first out vec4 name (works for many shaderpacks)
        m = OUT_DECL.matcher(vertexSource);
        if (m.find()) {
            String name = m.group(1);
            LOGGER.info("BayerFragmentPatcher: using first out vec4 '{}' as vertex color varying (fallback)", name);
            return name + ".a";
        }
        return null;
    }

    /** Finds the fragment output alpha, e.g. {@code "fragColor.a"} or {@code "gl_FragData[0].a"}. */
    private static String findFragmentOutputAlpha(String fragmentSource) {
        Matcher m = OUT_DECL.matcher(fragmentSource);
        if (m.find()) return m.group(1) + ".a";
        if (fragmentSource.contains("gl_FragData[0]")) return "gl_FragData[0].a";
        if (fragmentSource.contains("gl_FragColor")) return "gl_FragColor.a";
        return null;
    }

    private static String buildBayerCode(String alphaExpr) {
        return String.format("""
            // IrisFlw: Bayer 8x8 ordered dithering for ghost block translucency
            const float _if_bayer[64] = float[64](
                0.0/64.0, 32.0/64.0,  8.0/64.0, 40.0/64.0,  2.0/64.0, 34.0/64.0, 10.0/64.0, 42.0/64.0,
                48.0/64.0, 16.0/64.0, 56.0/64.0, 24.0/64.0, 50.0/64.0, 18.0/64.0, 58.0/64.0, 26.0/64.0,
                12.0/64.0, 44.0/64.0,  4.0/64.0, 36.0/64.0, 14.0/64.0, 46.0/64.0,  6.0/64.0, 38.0/64.0,
                60.0/64.0, 28.0/64.0, 52.0/64.0, 20.0/64.0, 62.0/64.0, 30.0/64.0, 54.0/64.0, 22.0/64.0,
                3.0/64.0, 35.0/64.0, 11.0/64.0, 43.0/64.0,  1.0/64.0, 33.0/64.0,  9.0/64.0, 41.0/64.0,
                51.0/64.0, 19.0/64.0, 59.0/64.0, 27.0/64.0, 49.0/64.0, 17.0/64.0, 57.0/64.0, 25.0/64.0,
                15.0/64.0, 47.0/64.0,  7.0/64.0, 39.0/64.0, 13.0/64.0, 45.0/64.0,  5.0/64.0, 37.0/64.0,
                63.0/64.0, 31.0/64.0, 55.0/64.0, 23.0/64.0, 61.0/64.0, 29.0/64.0, 53.0/64.0, 21.0/64.0
            );
            int _if_bx = int(mod(gl_FragCoord.x, 8.0));
            int _if_by = int(mod(gl_FragCoord.y, 8.0));
            if (%s <= _if_bayer[_if_by * 8 + _if_bx]) discard;
            """, alphaExpr);
    }

    /**
     * Patches the fragment shader source by injecting Bayer dithering discard
     * at the end of main().
     *
     * @param vertexSource   original vertex shader source (from shaderpack)
     * @param fragmentSource original fragment shader source (from shaderpack)
     * @return patched fragment source, or original if injection fails
     */
    public static String patch(String vertexSource, String fragmentSource) {
        // Priority: vertex color varying (works for ALL shaderpacks)
        String alphaExpr = findVertexColorVarying(vertexSource);
        if (alphaExpr == null) {
            // Fallback: fragment output (works for forward shaderpacks only)
            alphaExpr = findFragmentOutputAlpha(fragmentSource);
        }
        if (alphaExpr == null) {
            LOGGER.warn("BayerFragmentPatcher: could not find alpha source, dithering not applied");
            return fragmentSource;
        }

        // GLSL fragment shaders always end with main()'s closing brace —
        // no struct/fn definitions follow it. lastIndexOf is O(1) from tail.
        int closePos = fragmentSource.lastIndexOf('}');
        if (closePos < 0) {
            LOGGER.warn("BayerFragmentPatcher: no closing brace found in fragment source");
            return fragmentSource;
        }

        LOGGER.info("BayerFragmentPatcher: injecting Bayer dithering using alpha '{}'", alphaExpr);

        return fragmentSource.substring(0, closePos)
                + '\n' + buildBayerCode(alphaExpr) + '\n'
                + fragmentSource.substring(closePos);
    }
}
