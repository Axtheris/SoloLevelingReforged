package net.xelpha.sololevelingreforged.core;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Registry for custom capabilities
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistry {

    /**
     * Register all mod capabilities
     */
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register PlayerCapability
        event.register(PlayerCapability.class);
    }
}