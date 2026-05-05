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
 * The alpha is read from the shaderpack's original fragment output variable
 * (e.g. {@code fragColor.a} or {@code gl_FragData[0].a}), detected from the source.
 */
public final class BayerFragmentPatcher {
    private static final Logger LOGGER = IrisFlw.LOGGER;

    private BayerFragmentPatcher() {}

    /**
     * Finds the fragment output variable name from the source.
     * Handles: {@code out vec4 name;}, {@code layout(...) out vec4 name;},
     * and {@code gl_FragData[0]}.
     */
    private static final Pattern OUTPUT_DECL = Pattern.compile(
            "(?:layout\\s*\\([^)]*\\)\\s*)?out\\s+vec4\\s+(\\w+)\\s*;");

    private static String findOutputAlpha(String source) {
        Matcher m = OUTPUT_DECL.matcher(source);
        if (m.find()) return m.group(1) + ".a";
        // Check for gl_FragData[0]
        if (source.contains("gl_FragData[0]")) return "gl_FragData[0].a";
        // Last resort: try gl_FragColor (legacy)
        if (source.contains("gl_FragColor")) return "gl_FragColor.a";
        return null;
    }

    private static String buildBayerCode(String alphaVar) {
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
            """, alphaVar);
    }

    /**
     * Patches the fragment shader source by injecting Bayer dithering discard
     * at the end of main().
     *
     * @param fragmentSource original fragment shader source (from shaderpack)
     * @return patched source, or original if injection point or alpha variable not found
     */
    public static String patch(String fragmentSource) {
        String alphaVar = findOutputAlpha(fragmentSource);
        if (alphaVar == null) {
            LOGGER.warn("BayerFragmentPatcher: could not find fragment output variable, dithering not applied");
            return fragmentSource;
        }

        int closePos = findMainClosingBrace(fragmentSource);
        if (closePos < 0) {
            LOGGER.warn("BayerFragmentPatcher: could not find main() closing brace, dithering not applied");
            return fragmentSource;
        }

        LOGGER.info("BayerFragmentPatcher: injecting Bayer dithering using alpha variable '{}'", alphaVar);

        return fragmentSource.substring(0, closePos)
                + '\n' + buildBayerCode(alphaVar) + '\n'
                + fragmentSource.substring(closePos);
    }

    /**
     * Finds the position of the closing brace of main().
     * Handles nested braces within the function body.
     */
    private static int findMainClosingBrace(String source) {
        int mainPos = source.indexOf("void main()");
        if (mainPos < 0) {
            mainPos = source.indexOf("void main (");
        }
        if (mainPos < 0) return -1;

        int openPos = source.indexOf('{', mainPos);
        if (openPos < 0) return -1;

        int depth = 1;
        for (int i = openPos + 1; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
}
