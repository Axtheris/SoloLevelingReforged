package net.xelpha.sololevelingreforged.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.network.ModNetworkRegistry;
import net.xelpha.sololevelingreforged.network.SyncCapabilityPacket;

/**
 * Commands for the Solo Leveling System
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class SoloLevelingCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // /system info - Show player system info
        LiteralArgumentBuilder<CommandSourceStack> infoCommand = Commands.literal("system")
            .then(Commands.literal("info")
                .executes(context -> showSystemInfo(context.getSource())));

        // /system givexp <amount> - Give experience (admin/debug command)
        LiteralArgumentBuilder<CommandSourceStack> giveXpCommand = Commands.literal("system")
            .then(Commands.literal("givexp")
                .requires(source -> source.hasPermission(2)) // OP only
                .then(Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                    .executes(context -> giveExperience(
                        context.getSource(),
                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount")
                    ))));

        // /system giveap <amount> - Give ability points (admin command)
        LiteralArgumentBuilder<CommandSourceStack> giveApCommand = Commands.literal("system")
            .then(Commands.literal("giveap")
                .requires(source -> source.hasPermission(2)) // OP only
                .then(Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                    .executes(context -> giveAbilityPoints(
                        context.getSource(),
                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount")
                    ))));

        // /system setlevel <level> - Set player level (admin command)
        LiteralArgumentBuilder<CommandSourceStack> setLevelCommand = Commands.literal("system")
            .then(Commands.literal("setlevel")
                .requires(source -> source.hasPermission(2)) // OP only
                .then(Commands.argument("level", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                    .executes(context -> setPlayerLevel(
                        context.getSource(),
                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "level")
                    ))));

        // /system resetstats - Reset all stats and refund AP (admin command)
        LiteralArgumentBuilder<CommandSourceStack> resetStatsCommand = Commands.literal("system")
            .then(Commands.literal("resetstats")
                .requires(source -> source.hasPermission(2)) // OP only
                .executes(context -> resetPlayerStats(context.getSource())));

        // /system resetlevel - Reset level to 1 and refund XP (admin command)
        LiteralArgumentBuilder<CommandSourceStack> resetLevelCommand = Commands.literal("system")
            .then(Commands.literal("resetlevel")
                .requires(source -> source.hasPermission(2)) // OP only
                .executes(context -> resetPlayerLevel(context.getSource())));

        dispatcher.register(infoCommand);
        dispatcher.register(giveXpCommand);
        dispatcher.register(giveApCommand);
        dispatcher.register(setLevelCommand);
        dispatcher.register(resetStatsCommand);
        dispatcher.register(resetLevelCommand);
    }


    private static int showSystemInfo(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            player.sendSystemMessage(Component.literal("=== SYSTEM STATUS ==="));
            player.sendSystemMessage(Component.literal("Level: " + cap.getLevel()));
            player.sendSystemMessage(Component.literal("Experience: " + cap.getExperience() + "/" + cap.getExperienceToNext()));
            player.sendSystemMessage(Component.literal("Available AP: " + cap.getAvailableAP()));
            player.sendSystemMessage(Component.literal("STR: " + cap.getStrength() + " | AGI: " + cap.getAgility() +
                                                     " | SEN: " + cap.getSense()));
            player.sendSystemMessage(Component.literal("VIT: " + cap.getVitality() + " | INT: " + cap.getIntelligence()));
            player.sendSystemMessage(Component.literal("Mana: " + String.format("%.1f/%.1f", cap.getCurrentMana(), cap.getMaxMana())));
        });

        return 1;
    }

    private static int giveExperience(CommandSourceStack source, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            cap.addExperience(amount);
            source.sendSuccess(() -> Component.literal("Gave " + amount + " experience to " + player.getName().getString()), true);
        });

        return 1;
    }

    private static int giveAbilityPoints(CommandSourceStack source, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            cap.addAvailableAP(amount);
            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCapabilityPacket(cap.serializeNBT()));
            source.sendSuccess(() -> Component.literal("Gave " + amount + " ability points to " + player.getName().getString()), true);
        });

        return 1;
    }

    private static int setPlayerLevel(CommandSourceStack source, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            // Calculate experience needed for the target level
            int targetExp = 0;
            for (int i = 1; i < level; i++) {
                targetExp += (int)(100 * Math.pow(i, 1.5));
            }

            // Set level and experience
            cap.setLevel(level);
            cap.setExperience(targetExp);
            cap.setExperienceToNext((int)(100 * Math.pow(level, 1.5)));
            cap.setAvailableAP((level - 1) * 5); // 5 AP per level

            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCapabilityPacket(cap.serializeNBT()));
            source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + " to level " + level), true);
        });

        return 1;
    }

    private static int resetPlayerStats(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            // Calculate total AP used
            int refundedAP = cap.getStrength() + cap.getAgility() + cap.getSense() +
                           cap.getVitality() + cap.getIntelligence();

            // Reset all stats to 0
            cap.setStrength(0);
            cap.setAgility(0);
            cap.setSense(0);
            cap.setVitality(0);
            cap.setIntelligence(0);

            // Refund AP
            cap.addAvailableAP(refundedAP);

            // Update max health and mana
            cap.updateMaxHealth();
            cap.updateMaxMana();

            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCapabilityPacket(cap.serializeNBT()));
            source.sendSuccess(() -> Component.literal("Reset all stats for " + player.getName().getString() + " and refunded " + refundedAP + " AP"), true);
        });

        return 1;
    }

    private static int resetPlayerLevel(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players!"));
            return 0;
        }

        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            int oldLevel = cap.getLevel();
            int refundedAP = (oldLevel - 1) * 5; // Remove AP gained from levels

            // Reset to level 1
            cap.setLevel(1);
            cap.setExperience(0);
            cap.setExperienceToNext(100);
            cap.setAvailableAP(0); // Start with 0 AP, can earn through leveling

            ModNetworkRegistry.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncCapabilityPacket(cap.serializeNBT()));
            source.sendSuccess(() -> Component.literal("Reset " + player.getName().getString() + " to level 1 (was level " + oldLevel + ")"), true);
        });

        return 1;
    }
}