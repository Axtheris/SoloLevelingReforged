package net.xelpha.sololevelingreforged.skills;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Detoxification - Passive skill that automatically removes negative effects
 * Clears poisons, debuffs, and harmful status effects
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class DetoxificationSkill extends Skill {

    // Effects that this skill can remove
    private static final java.util.Set<MobEffect> REMOVABLE_EFFECTS = java.util.Set.of(
        net.minecraft.world.effect.MobEffects.POISON,
        net.minecraft.world.effect.MobEffects.WITHER,
        net.minecraft.world.effect.MobEffects.WEAKNESS,
        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
        net.minecraft.world.effect.MobEffects.BLINDNESS,
        net.minecraft.world.effect.MobEffects.CONFUSION,
        net.minecraft.world.effect.MobEffects.HUNGER,
        net.minecraft.world.effect.MobEffects.DARKNESS
    );

    public DetoxificationSkill() {
        super(
            Sololevelingreforged.loc("detoxification"),
            "Detoxification",
            "Automatically neutralize and remove harmful status effects like poison and debuffs.",
            SkillType.PASSIVE_BUFF,
            5, // Max level
            0, // No mana cost (passive)
            0, // No cooldown (passive)
            10 // Unlock level
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
     * Event handler for effect application - prevent harmful effects
     */
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        // Check if player has detoxification skill
        player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
            .ifPresent(cap -> {
                Skill detoxSkill = cap.getSkill(Sololevelingreforged.loc("detoxification"));
                if (detoxSkill instanceof DetoxificationSkill skill) {
                    skill.handleEffectAddition(player, event);
                }
            });
    }

    /**
     * Event handler for effect expiration - clean up remaining effects
     */
    @SubscribeEvent
    public static void onMobEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        // Check if player has detoxification skill
        player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
            .ifPresent(cap -> {
                Skill detoxSkill = cap.getSkill(Sololevelingreforged.loc("detoxification"));
                if (detoxSkill instanceof DetoxificationSkill skill) {
                    skill.handleEffectExpiration(player, event);
                }
            });
    }

    private void handleEffectAddition(net.minecraft.server.level.ServerPlayer player, MobEffectEvent.Added event) {
        MobEffect effect = event.getEffectInstance().getEffect();

        if (REMOVABLE_EFFECTS.contains(effect)) {
            // Chance to prevent the effect based on skill level
            float preventionChance = getPreventionChance();

            if (player.getRandom().nextFloat() < preventionChance) {
                // Prevent the effect
                event.setCanceled(true);

                // Visual effect
                playDetoxEffects(player);

                // Send message
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Detoxification prevented " + effect.getDisplayName().getString() + "!"));
            }
        }
    }

    private void handleEffectExpiration(net.minecraft.server.level.ServerPlayer player, MobEffectEvent.Expired event) {
        MobEffect effect = event.getEffectInstance().getEffect();

        if (REMOVABLE_EFFECTS.contains(effect)) {
            // Chance to instantly remove the effect when it would expire
            float removalChance = getRemovalChance();

            if (player.getRandom().nextFloat() < removalChance) {
                // The effect is already expiring, just add visual feedback
                playDetoxEffects(player);
            }
        }
    }

    private float getPreventionChance() {
        // Base 30% + 10% per level = 30%-80% chance to prevent effects
        return 0.3f + (currentLevel * 0.1f);
    }

    private float getRemovalChance() {
        // Base 50% + 10% per level = 50%-100% chance to remove effects instantly
        return 0.5f + (currentLevel * 0.1f);
    }

    private void playDetoxEffects(net.minecraft.server.level.ServerPlayer player) {
        // Cleansing particles
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
            player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ(),
            15, 0.3, 0.3, 0.3, 0.1);

        // Gentle cleansing sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            net.minecraft.sounds.SoundEvents.BREWING_STAND_BREW, player.getSoundSource(), 0.6F, 1.8F);
    }
}