package top.leonx.irisflw.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import top.leonx.irisflw.IrisFlw;

public final class IrisFlwFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Run our common setup.
        IrisFlw.init();

        // Listen for the player joining the world
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (FabricLoader.getInstance().isModLoaded("scalablelux")) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(
                            Component.literal(ChatFormatting.DARK_RED+"IrisFlwCompat is not compatible with ScalableLux!")
                    );
                }
            }
        });
    }
}
