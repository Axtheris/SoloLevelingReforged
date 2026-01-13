package net.xelpha.sololevelingreforged.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for the Solo Leveling System
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    // Open System Console (K key)
    public static final KeyMapping OPEN_STATS = new KeyMapping(
        "key.sololevelingreforged.open_stats",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        "key.categories.sololevelingreforged"
    );
    
    // Store held item to System Inventory (V key - like "vault")
    public static final KeyMapping STORE_ITEM = new KeyMapping(
        "key.sololevelingreforged.store_item",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.categories.sololevelingreforged"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
        event.register(STORE_ITEM);
    }
}