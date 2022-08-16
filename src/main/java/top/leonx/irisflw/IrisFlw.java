package top.leonx.irisflw;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod("irisflw")
public class IrisFlw {
    public static final Logger LOGGER = LogUtils.getLogger();
    public IrisFlw()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(FMLClientSetupEvent t) {
        LOGGER.info("Oculus Flywheel Compat Initialized");
    }
}
