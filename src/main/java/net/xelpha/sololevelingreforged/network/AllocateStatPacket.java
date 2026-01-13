package net.xelpha.sololevelingreforged.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.core.PlayerCapability;

import java.util.function.Supplier;

/**
 * Packet for allocating ability points to stats
 */
public class AllocateStatPacket {

    private final String statName;

    public AllocateStatPacket(String statName) {
        this.statName = statName;
    }

    public AllocateStatPacket(FriendlyByteBuf buf) {
        this.statName = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(statName);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Server-side processing
            if (context.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer sender = context.get().getSender();
                if (sender != null) {
                    sender.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                        // Verify player has available AP
                        if (cap.getAvailableAP() > 0) {
                            // Attempt to allocate the stat point
                            if (cap.allocateStatPoint(statName)) {
                                // Success - sync back to client
                                ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                                    new SyncCapabilityPacket(cap.serializeNBT()));
                            } else {
                                // Invalid stat name
                                sender.sendSystemMessage(Component.literal("Invalid stat allocation request."));
                            }
                        } else {
                            // No AP available
                            sender.sendSystemMessage(Component.literal("No Ability Points available!"));
                        }
                    });
                }
            } else {
                // Client-side: refresh the UI if needed
                // The capability sync packet will handle UI updates
            }
        });
        context.get().setPacketHandled(true);
    }
}