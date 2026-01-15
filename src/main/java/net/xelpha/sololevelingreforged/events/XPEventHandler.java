package net.xelpha.sololevelingreforged.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.skills.Skill;

/**
 * XP Event Handler - Listens for Entity Death events and grants XP to players
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class XPEventHandler {

    /**
     * Grant XP when player kills a mob
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Check if the damage source was a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        LivingEntity target = event.getEntity();

        // Calculate XP based on target's max health and difficulty
        int xpGain = calculateXP(target);

        // Add XP to player's System Capability (with Predator skill multiplier)
        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            // Apply Predator skill multiplier if available
            float predatorMultiplier = 1.0f;
            if (cap.hasSkill(Sololevelingreforged.loc("predator"))) {
                Skill predatorSkill = cap.getSkill(Sololevelingreforged.loc("predator"));
                if (predatorSkill instanceof net.xelpha.sololevelingreforged.skills.PredatorSkill predator) {
                    predatorMultiplier = predator.getXpMultiplier(target);
                }
            }

            int finalXpGain = Math.round(xpGain * predatorMultiplier);
            cap.addExperience(finalXpGain);

            // Show predator bonus message if applicable
            if (predatorMultiplier > 1.0f && finalXpGain > xpGain) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    String.format("Predator bonus: +%.0f%% XP!", (predatorMultiplier - 1.0f) * 100)));
            }
        });
    }

    /**
     * Calculate XP based on entity's max health and difficulty
     */
    private static int calculateXP(LivingEntity entity) {
        float maxHealth = entity.getMaxHealth();

        // Base XP calculation: health-based with some multipliers
        int baseXP = Math.max(1, (int) (maxHealth * 2.0f));

        // Difficulty multipliers
        String entityType = entity.getType().getDescriptionId().toLowerCase();

        // Boss mobs get bonus XP
        if (entityType.contains("wither") || entityType.contains("ender_dragon") ||
            entityType.contains("warden") || maxHealth >= 200) {
            baseXP *= 3;
        }
        // Elite mobs get bonus
        else if (entityType.contains("boss") || maxHealth >= 100) {
            baseXP *= 2;
        }
        // Hostile mobs get slight bonus
        else if (entityType.contains("zombie") || entityType.contains("skeleton") ||
                 entityType.contains("spider") || entityType.contains("creeper")) {
            baseXP = (int) (baseXP * 1.5f);
        }

        return Math.max(1, baseXP);
    }
}