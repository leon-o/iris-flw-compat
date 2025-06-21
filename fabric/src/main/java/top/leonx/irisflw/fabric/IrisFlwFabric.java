package top.leonx.irisflw.fabric;

import net.fabricmc.api.ClientModInitializer;
import top.leonx.irisflw.IrisFlw;

public final class IrisFlwFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Run our common setup.
        IrisFlw.init();
    }
}
