package top.leonx.irisflw.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
import org.apache.commons.lang3.tuple.Pair;

public class IrisFlwConfig {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    // 客户端专用配置（仅本地生效）
    public static class Client {
        public final BooleanValue replaceCheckerboardTexture;

        Client(ModConfigSpec.Builder builder) {
            replaceCheckerboardTexture = builder
                    .comment("Use IrisFlw's custom checkerboard texture instead of the default Create one. It is highly recommended to turn it on, as Create's own checkerboard maps are completely opaque in some shaderpacks.")
                    .define("replaceCheckerboardTexture", true);
        }
    }

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
}
