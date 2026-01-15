package net.xelpha.sololevelingreforged.skills;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Predator - Passive skill that increases XP gain from killing monsters
 * Higher level monsters give exponentially more XP
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class PredatorSkill extends Skill {

    public PredatorSkill() {
        super(
            Sololevelingreforged.loc("predator"),
            "Predator",
            "Gain bonus XP from killing monsters. Higher level monsters give exponentially more XP.",
            SkillType.PASSIVE_BUFF,
            10, // Max level
            0, // No mana cost (passive)
            0, // No cooldown (passive)
            1 // Unlock level (available from start)
        );
    }

    @Override
    public boolean activate(net.minecraft.server.level.ServerPlayer player) {
        // Passive skill - always "active"
        return false;
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f; // No direct damage effect
    }

    @Override
    public int getEffectDuration() {
        return Integer.MAX_VALUE; // Always active
    }

    /**
     * Calculate XP multiplier based on skill level and monster difficulty
     */
    public float getXpMultiplier(net.minecraft.world.entity.LivingEntity target) {
        if (currentLevel == 0) return 1.0f;

        // Base multiplier: 1.0 + 0.1 per level
        float baseMultiplier = 1.0f + (currentLevel * 0.1f);

        // Bonus for higher health monsters
        float healthRatio = target.getMaxHealth() / 20.0f; // 20 = player health
        if (healthRatio > 1.0f) {
            baseMultiplier += (healthRatio - 1.0f) * (currentLevel * 0.05f);
        }

        // Boss bonus
        String entityType = target.getType().getDescriptionId().toLowerCase();
        if (entityType.contains("wither") || entityType.contains("ender_dragon") ||
            entityType.contains("warden") || target.getMaxHealth() >= 200) {
            baseMultiplier *= (1.5f + currentLevel * 0.1f);
        }

        return baseMultiplier;
    }

    /**
     * Event handler for XP gain modification
     * This is called from XPEventHandler when calculating XP
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // This is handled in XPEventHandler to avoid double processing
        // The Predator skill multiplier is applied there
    }
}