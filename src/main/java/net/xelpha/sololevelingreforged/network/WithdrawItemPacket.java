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
 * Packet for withdrawing items from the System Inventory
 */
public class WithdrawItemPacket {
    
    private final int slotIndex;

    public WithdrawItemPacket(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public WithdrawItemPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.get().getSender();
                if (player != null) {
                    player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                        ItemStack withdrawn = cap.removeItemFromInventory(slotIndex);
                        
                        if (!withdrawn.isEmpty()) {
                            // Give item to player
                            if (!player.getInventory().add(withdrawn)) {
                                // Inventory full - drop at feet
                                player.drop(withdrawn, false);
                                player.sendSystemMessage(Component.literal(
                                    "§b[System] §f" + withdrawn.getHoverName().getString() + " §7dropped (inventory full)"
                                ));
                            } else {
                                player.sendSystemMessage(Component.literal(
                                    "§b[System] §f" + withdrawn.getHoverName().getString() + " §7withdrawn"
                                ));
                            }
                            
                            // Sync capability to client
                            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                new SyncCapabilityPacket(cap.serializeNBT()));
                        }
                    });
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
