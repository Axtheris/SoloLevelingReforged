package net.xelpha.sololevelingreforged.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Network registry for handling packets between server and client
 */
public class ModNetworkRegistry {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        Sololevelingreforged.loc("main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        // Register sync capability packet
        CHANNEL.registerMessage(
            packetId++,
            SyncCapabilityPacket.class,
            SyncCapabilityPacket::encode,
            SyncCapabilityPacket::new,
            SyncCapabilityPacket::handle
        );

        // Register allocate stat packet
        CHANNEL.registerMessage(
            packetId++,
            AllocateStatPacket.class,
            AllocateStatPacket::encode,
            AllocateStatPacket::new,
            AllocateStatPacket::handle
        );
    }
}