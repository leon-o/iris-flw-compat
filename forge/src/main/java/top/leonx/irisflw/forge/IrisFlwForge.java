package top.leonx.irisflw.forge;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.leonx.irisflw.IrisFlw;

@Mod(IrisFlw.MOD_ID)
public final class IrisFlwForge {
    public IrisFlwForge() {
        // Run our common setup.
        IrisFlw.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(FMLClientSetupEvent t) {
        IrisFlw.LOGGER.info("Oculus Flywheel Compat Initialized");
    }
}
