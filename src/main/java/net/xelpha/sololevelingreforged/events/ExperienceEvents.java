package net.xelpha.sololevelingreforged.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.core.PlayerCapability;

/**
 * Event handlers for granting experience to players
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class ExperienceEvents {

    /**
     * Grant experience when player kills a mob
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        LivingEntity target = event.getEntity();

        // Calculate experience based on mob type
        int expGain = calculateMobExperience(target);

        // Grant experience to player
        player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
            cap.addExperience(expGain);
        });
    }

    /**
     * Grant minimal experience when player breaks blocks
     * Mining should not be the primary way to level up
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        // Much reduced experience for mining - focus should be on combat
        float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
        int expGain = Math.max(1, (int) (hardness * 0.3f)); // 0.3-3 exp based on hardness (much reduced)

        // Only give experience for harder blocks (stone level and above)
        if (hardness >= 1.5f) {
            player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).ifPresent(cap -> {
                cap.addExperience(expGain);
            });
        }
    }

    /**
     * Calculate experience gained from killing mobs based on their max health
     * This makes the system modular and scales with mob health for compatibility with other mods
     *
     * Formula: XP = maxHealth * 0.8 * bossMultiplier * healthScaling
     * - Base: 0.8 XP per heart
     * - Boss multiplier: 2x for mini-bosses, 3x for major bosses
     * - Health scaling: Additional multiplier for very high HP mobs
     * - Capped at 500 XP maximum per mob
     */
    private static int calculateMobExperience(LivingEntity entity) {
        // Get the mob's maximum health
        float maxHealth = entity.getMaxHealth();

        // Base experience scaling with health
        // Formula: baseXP = maxHealth * multiplier, with minimum and maximum caps
        float baseXP = maxHealth * 0.8f; // 0.8 XP per heart

        // Minimum experience (even 1HP mobs give some XP)
        baseXP = Math.max(baseXP, 3.0f);

        // Boss multiplier for special mobs
        String mobType = entity.getType().getDescriptionId().toLowerCase();
        float bossMultiplier = 1.0f;

        // Boss mobs get bonus multiplier
        if (mobType.contains("wither") || mobType.contains("dragon") || mobType.contains("warden")) {
            bossMultiplier = 3.0f; // 3x multiplier for major bosses
        } else if (mobType.contains("boss") || mobType.contains("iron_golem") || mobType.contains("elder_guardian")) {
            bossMultiplier = 2.0f; // 2x multiplier for mini-bosses
        } else if (mobType.contains("ravager") || mobType.contains("piglin_brute") || mobType.contains("evoker")) {
            bossMultiplier = 1.5f; // 1.5x multiplier for tough mobs
        }

        // Apply boss multiplier
        baseXP *= bossMultiplier;

        // Additional scaling for very high health mobs (diminishing returns)
        if (maxHealth > 100) {
            float extraScaling = Math.min(maxHealth / 100.0f, 3.0f); // Cap at 3x for 300+ HP
            baseXP *= (1.0f + extraScaling * 0.5f);
        }

        // Cap maximum experience per mob to prevent exploits
        baseXP = Math.min(baseXP, 500.0f);

        return Math.round(baseXP);
    }
}