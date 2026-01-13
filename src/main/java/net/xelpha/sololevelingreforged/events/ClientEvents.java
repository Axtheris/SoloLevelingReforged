package net.xelpha.sololevelingreforged.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.KeyBindings;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.StoreItemPacket;
import net.xelpha.sololevelingreforged.ui.SystemConsoleScreen;

/**
 * Client-side event handlers for the Solo Leveling System
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean systemKeyWasPressed = false;
    private static boolean storeKeyWasPressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) return;

        // Handle system console keybind (K key) - toggle behavior
        handleSystemKey(minecraft, player);
        
        // Handle store item keybind (V key)
        handleStoreKey(minecraft, player);
    }
    
    private static void handleSystemKey(Minecraft minecraft, LocalPlayer player) {
        boolean keyIsDown = KeyBindings.OPEN_STATS.isDown();
        
        if (keyIsDown && !systemKeyWasPressed) {
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
        
        systemKeyWasPressed = keyIsDown;
    }
    
    private static void handleStoreKey(Minecraft minecraft, LocalPlayer player) {
        boolean keyIsDown = KeyBindings.STORE_ITEM.isDown();
        
        if (keyIsDown && !storeKeyWasPressed) {
            // Key was just pressed - store held item
            if (minecraft.screen == null) {
                // Only when no screen is open (in-game)
                ItemStack heldItem = player.getMainHandItem();
                
                if (!heldItem.isEmpty()) {
                    // Send packet to store the item
                    ModNetworkRegistry.CHANNEL.sendToServer(new StoreItemPacket());
                    
                    // Show feedback message (the actual storage happens server-side)
                    // The system message will be sent from server after successful storage
                } else {
                    // No item held - show hint
                    player.displayClientMessage(
                        Component.literal("§7[System] §fHold an item to store it in your System Inventory"),
                        true
                    );
                }
            }
        }
        
        storeKeyWasPressed = keyIsDown;
    }
}
