package top.leonx.irisflw.neoforge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.leonx.irisflw.IrisFlw;

@Mod(IrisFlw.MOD_ID)
public final class IrisFlwNeoForge {
    public IrisFlwNeoForge() {
        // Run our common setup.
        IrisFlw.init();
        CompatibilityCheck.register();
    }

    @EventBusSubscriber
    public static class CompatibilityCheck {

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (ModList.get().isLoaded("scalablelux")) {
                if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
                    player.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED+"IrisFlwCompat is not compatible with ScalableLux!"));
                }
            }
        }

        public static void register() {
            NeoForge.EVENT_BUS.register(CompatibilityCheck.class);
        }
    }
}
