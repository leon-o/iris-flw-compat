package top.leonx.irisflw.forge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.leonx.irisflw.IrisFlw;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

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
            EVENT_BUS.register(CompatibilityCheck.class);
        }
    }
}
