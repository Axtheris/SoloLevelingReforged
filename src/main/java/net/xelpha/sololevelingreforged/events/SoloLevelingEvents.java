package net.xelpha.sololevelingreforged.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.CapabilityStorage;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.SyncCapabilityPacket;

/**
 * Main event handler for Solo Leveling System
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class SoloLevelingEvents {

    /**
     * Attach the PlayerCapability to all players
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            // Attach the player capability with storage
            CapabilityStorage storage = new CapabilityStorage();
            storage.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> cap.setPlayer(player));

            event.addCapability(
                Sololevelingreforged.loc("player_system_capability"),
                storage
            );
        }
    }

    /**
     * Copy capability data when player respawns or changes dimensions
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        // Copy capability data
        original.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(oldCap -> {
            player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(newCap -> {
                // Copy all data from old capability to new one
                newCap.deserializeNBT(oldCap.serializeNBT());
                newCap.setPlayer(player);
            });
        });
    }

    /**
     * Sync capability data to client when player logs in
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            // Sync capability data to client
            serverPlayer.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncCapabilityPacket(cap.serializeNBT()));
            });
        }
    }

    /**
     * Handle player death (potential penalty zone logic)
     */
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            // TODO: Implement penalty zone teleportation logic
            // This will be part of the daily quest system
        });
    }
}