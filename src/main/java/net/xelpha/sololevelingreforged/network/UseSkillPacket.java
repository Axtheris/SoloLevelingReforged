package net.xelpha.sololevelingreforged.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for using skills from client to server
 */
public class UseSkillPacket {

    private final String skillId;

    public UseSkillPacket(String skillId) {
        this.skillId = skillId;
    }

    public UseSkillPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(skillId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
                    .ifPresent(cap -> {
                        net.minecraft.resources.ResourceLocation skillKey =
                            new net.minecraft.resources.ResourceLocation(
                                net.xelpha.sololevelingreforged.Sololevelingreforged.MODID, skillId);

                        if (cap.hasSkill(skillKey)) {
                            boolean success = cap.useSkill(skillKey, player);
                            if (!success) {
                                // Send failure message
                                player.sendSystemMessage(Component.literal("Failed to use skill: " + skillId));
                            }
                        } else {
                            // Player doesn't have this skill
                            player.sendSystemMessage(Component.literal("You haven't learned this skill yet!"));
                        }
                    });
            }
        });
        return true;
    }
}