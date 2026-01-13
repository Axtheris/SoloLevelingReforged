package net.xelpha.sololevelingreforged.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.KeyBindings;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ui.SystemConsoleScreen;

/**
 * Client-side event handlers for the Solo Leveling System
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean keyWasPressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) return;

        // Handle system console keybind (K key) - toggle behavior
        boolean keyIsDown = KeyBindings.OPEN_STATS.isDown();
        
        if (keyIsDown && !keyWasPressed) {
            // Key was just pressed (edge detection)
            if (minecraft.screen instanceof SystemConsoleScreen) {
                // Close if already open
                minecraft.setScreen(null);
            } else if (minecraft.screen == null) {
                // Open if no screen is open
                player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                    minecraft.setScreen(new SystemConsoleScreen());
                });
            }
        }
        
        keyWasPressed = keyIsDown;
    }
}