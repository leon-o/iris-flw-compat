package top.leonx.irisflw.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class IrisFlwConfig {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;


    public static class Client {
        public final BooleanValue replaceCheckerboardTexture;
        public final BooleanValue useDithering;
        public final ConfigValue<String> ditheringMethod;

        Client(ModConfigSpec.Builder builder) {
            replaceCheckerboardTexture = builder
                    .comment("Use IrisFlw's custom checkerboard texture instead of the default Create one. It is highly recommended to turn it on, as Create's own checkerboard maps are completely opaque in some shaderpacks.")
                    .define("replaceCheckerboardTexture", true);

            useDithering = builder
                    .comment("Use screen-door translucency (ordered dithering) for ghost block preview instead of alpha blending. Dithering works with both forward and deferred shaderpacks. Disable to fall back to alpha-blended terrain shader.")
                    .define("useDithering", true);

            ditheringMethod = builder
                    .comment("Dithering noise method:" +
                            "\n  IGN  — Interleaved Gradient Noise (default). No visible pattern, no texture needed." +
                            "\n  BAYER — 8×8 Bayer matrix. Deterministic, but shows cross-hatch pattern." +
                            "\nOnly effective when useDithering=true.")
                    .define("ditheringMethod", "IGN");
        }
    }

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
}
