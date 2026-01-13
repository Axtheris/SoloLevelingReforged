package net.xelpha.sololevelingreforged.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.core.PlayerCapability;

import java.util.function.Supplier;

/**
 * Packet for storing held item into the System Inventory
 * Solo Leveling style - items go into the system's dimensional storage
 */
public class StoreItemPacket {

    public StoreItemPacket() {
        // Empty constructor for receiving
    }

    public StoreItemPacket(FriendlyByteBuf buf) {
        // No data needed - we store whatever the player is holding
    }

    public void encode(FriendlyByteBuf buf) {
        // No data needed
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.get().getSender();
                if (player != null) {
                    ItemStack heldItem = player.getMainHandItem();
                    
                    if (!heldItem.isEmpty()) {
                        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                            // Store the item in system inventory
                            boolean stored = cap.addItemToInventory(heldItem.copy());
                            
                            if (stored) {
                                // Remove item from player's hand
                                player.getMainHandItem().setCount(0);
                                
                                // Send system message
                                player.sendSystemMessage(Component.literal(
                                    "§b[System] §f" + heldItem.getHoverName().getString() + " §7stored in System Inventory"
                                ));
                                
                                // Sync capability to client
                                ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                    new SyncCapabilityPacket(cap.serializeNBT()));
                            } else {
                                // Inventory full (shouldn't happen with unlimited storage, but just in case)
                                player.sendSystemMessage(Component.literal(
                                    "§c[System] §fFailed to store item"
                                ));
                            }
                        });
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
