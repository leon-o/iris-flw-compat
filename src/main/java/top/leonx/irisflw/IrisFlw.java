package top.leonx.irisflw;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.leonx.irisflw.backend.IrisFlwBackends;
import top.leonx.irisflw.config.IrisFlwConfig;

public final class IrisFlw {
    public static final String MOD_ID = "irisflw";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static void init() {
        IrisFlwBackends.init();
    }

    public static boolean isShaderPackInUse() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static boolean isUsingExtendedVertexFormat() {
        return isShaderPackInUse();
    }

    public static boolean isSableLoaded() {
        return ModList.get().isLoaded("sable");
    }

    /** If true, ghost block preview uses screen-door dithering with patched
     *  gbuffers_block shader. Dithering method is controlled by
     *  {@link IrisFlwConfig.Client#ditheringMethod}.
     *  If false, uses alpha-blended terrain shader at AFTER_BLOCK_ENTITIES. */
    public static boolean useDithering() {
        return IrisFlwConfig.CLIENT.useDithering.get();
    }

    /** Returns the configured dithering method: "IGN" or "BAYER". */
    public static String ditheringMethod() {
        return IrisFlwConfig.CLIENT.ditheringMethod.get();
    }
}
