package net.xelpha.sololevelingreforged.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;

import java.util.function.Supplier;

/**
 * Packet for syncing PlayerCapability data between client and server
 */
public class SyncCapabilityPacket {

    private final CompoundTag capabilityData;

    public SyncCapabilityPacket(CompoundTag capabilityData) {
        this.capabilityData = capabilityData;
    }

    public SyncCapabilityPacket(FriendlyByteBuf buf) {
        this.capabilityData = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(capabilityData);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Check if this is coming from client to server
            if (context.get().getDirection().getReceptionSide().isServer()) {
                // Handle on server side - update server capability
                ServerPlayer sender = context.get().getSender();
                if (sender != null) {
                    sender.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                        cap.deserializeNBT(capabilityData);
                        // Sync back to all clients
                        ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                                                       new SyncCapabilityPacket(capabilityData));
                    });
                }
            } else {
                // Handle on client side
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                        cap.deserializeNBT(capabilityData);
                    });
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}