package net.xelpha.sololevelingreforged.skills;

import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Shadow Affinity - Passive skill that boosts shadow-based abilities
 * Increases damage and effectiveness of shadow soldiers and shadow skills
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class ShadowAffinitySkill extends Skill {

    public ShadowAffinitySkill() {
        super(
            Sololevelingreforged.loc("shadow_affinity"),
            "Shadow Affinity",
            "Enhance all shadow-based abilities. Shadow soldiers deal more damage and shadow skills are more effective.",
            SkillType.PASSIVE_BUFF,
            10, // Max level
            0, // No mana cost (passive)
            0, // No cooldown (passive)
            8 // Unlock level
        );
    }

    @Override
    public boolean activate(net.minecraft.server.level.ServerPlayer player) {
        // Passive skill - always "active"
        return false;
    }

    @Override
    public float getDamageMultiplier() {
        // Damage bonus: 10% + 5% per level = 10%-60% bonus
        return 1.0f + (0.1f + currentLevel * 0.05f);
    }

    @Override
    public int getEffectDuration() {
        return Integer.MAX_VALUE; // Always active
    }

    /**
     * Event handler to boost shadow soldier damage
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // Check if the damage source is a shadow soldier
        if (event.getSource().getEntity() instanceof net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity shadowSoldier) {

            // Get the owner and check for Shadow Affinity skill
            net.minecraft.server.level.ServerPlayer owner = shadowSoldier.getOwner();
            if (owner != null) {
                owner.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
                    .ifPresent(cap -> {
                        Skill affinitySkill = cap.getSkill(Sololevelingreforged.loc("shadow_affinity"));
                        if (affinitySkill instanceof ShadowAffinitySkill skill) {
                            // Apply damage multiplier
                            float multiplier = skill.getDamageMultiplier();
                            event.setAmount(event.getAmount() * multiplier);
                        }
                    });
            }
        }
    }

    /**
     * Get shadow soldier stat boost multiplier
     */
    public float getShadowSoldierStatMultiplier() {
        // Stats boost: 5% + 3% per level = 5%-35% stat increase
        return 1.0f + (0.05f + currentLevel * 0.03f);
    }

    /**
     * Get mana cost reduction for shadow skills
     */
    public float getManaCostReduction() {
        // Mana reduction: 5% + 2% per level = 5%-25% mana cost reduction
        return 1.0f - (0.05f + currentLevel * 0.02f);
    }

    /**
     * Get cooldown reduction for shadow skills
     */
    public int getCooldownReductionTicks() {
        // Cooldown reduction: 5 ticks + 2 ticks per level = 5-25 tick reduction
        return 5 + currentLevel * 2;
    }

    /**
     * Apply shadow affinity bonuses to shadow skills
     */
    public static void applyShadowAffinityBonuses(net.minecraft.server.level.ServerPlayer player, Skill shadowSkill) {
        player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
            .ifPresent(cap -> {
                Skill affinitySkill = cap.getSkill(Sololevelingreforged.loc("shadow_affinity"));
                if (affinitySkill instanceof ShadowAffinitySkill skill) {
                    // The bonuses are applied in the skill activation methods
                    // This method exists for future expansion
                }
            });
    }
}