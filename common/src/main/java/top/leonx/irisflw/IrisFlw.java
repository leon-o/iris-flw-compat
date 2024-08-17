package top.leonx.irisflw;

import net.irisshaders.iris.api.v0.IrisApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrisFlw {
    public static final String MOD_ID = "irisflw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static void init() {
        // Write common init code here.
    }

    public static boolean isShaderPackInUse() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static boolean isUsingExtendedVertexFormat() {
        // Currently, there is no other choice but to extend the vertex format when shaders are enabled.
        return isShaderPackInUse();
    }
}
