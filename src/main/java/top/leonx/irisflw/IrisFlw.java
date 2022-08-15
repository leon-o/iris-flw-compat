package top.leonx.irisflw;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IrisFlw implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("iris-flw");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Iris-Flywheel-Compat initialized!");
    }
}
