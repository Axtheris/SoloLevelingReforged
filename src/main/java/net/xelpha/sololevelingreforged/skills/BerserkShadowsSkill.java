package net.xelpha.sololevelingreforged.skills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Berserk Shadows - Temporarily boosts all shadow soldiers' attack power
 * Increases damage and speed at the cost of durability
 */
public class BerserkShadowsSkill extends Skill {

    public BerserkShadowsSkill() {
        super(
            Sololevelingreforged.loc("berserk_shadows"),
            "Berserk Shadows",
            "Temporarily empower all shadow soldiers with increased strength and speed, but reduced durability.",
            SkillType.ACTIVE_OFFENSIVE,
            5, // Max level
            80, // Base mana cost
            300, // Base cooldown (15 seconds)
            10 // Unlock level
        );
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canActivate(player)) {
            player.sendSystemMessage(Component.literal("Cannot use Berserk Shadows: On cooldown or insufficient mana!"));
            return false;
        }

        ServerLevel level = player.serverLevel();
        int affectedSoldiers = 0;

        // Find all shadow soldiers owned by this player
        for (ShadowSoldierEntity soldier : level.getEntitiesOfClass(ShadowSoldierEntity.class,
            player.getBoundingBox().inflate(200.0D))) {

            if (soldier.isOwnedBy(player)) {
                applyBerserkEffects(soldier);
                affectedSoldiers++;
            }
        }

        if (affectedSoldiers == 0) {
            player.sendSystemMessage(Component.literal("No shadow soldiers found to empower!"));
            return false;
        }

        // Play activation effects
        playBerserkEffects(level, player);

        // Send success message
        player.sendSystemMessage(Component.literal("Berserk Shadows activated! " + affectedSoldiers +
            " shadow soldiers empowered for " + (getEffectDuration() / 20) + " seconds!"));

        markUsed();
        return true;
    }

    private void applyBerserkEffects(ShadowSoldierEntity soldier) {
        // Apply strength boost (increased damage)
        MobEffectInstance strengthEffect = new MobEffectInstance(
            MobEffects.DAMAGE_BOOST,
            getEffectDuration(),
            currentLevel - 1, // Level 1: +1 damage, Level 5: +5 damage
            false, false, true // No particles, no icon, ambient
        );

        // Apply speed boost
        MobEffectInstance speedEffect = new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED,
            getEffectDuration(),
            currentLevel - 1, // Level 1: +20% speed, Level 5: +100% speed
            false, false, true
        );

        // Apply weakness (reduced durability)
        MobEffectInstance weaknessEffect = new MobEffectInstance(
            MobEffects.WEAKNESS,
            getEffectDuration(),
            0, // Always level 1 weakness (25% damage reduction)
            false, false, true
        );

        soldier.addEffect(strengthEffect);
        soldier.addEffect(speedEffect);
        soldier.addEffect(weaknessEffect);
    }

    private void playBerserkEffects(ServerLevel level, ServerPlayer player) {
        // Red particles around player
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
            player.getX(), player.getY() + 1, player.getZ(),
            20, 0.5, 0.5, 0.5, 0.1);

        // Dark red particles
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
            player.getX(), player.getY() + 1, player.getZ(),
            30, 0.3, 0.3, 0.3, 0.5);

        // Sound effect
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            net.minecraft.sounds.SoundEvents.BLAZE_AMBIENT, net.minecraft.sounds.SoundSource.PLAYERS,
            1.0F, 0.5F);
    }

    @Override
    public float getDamageMultiplier() {
        // Berserk damage multiplier: 1.5x + 0.2x per level
        return 1.5f + (currentLevel * 0.2f);
    }

    @Override
    public int getEffectDuration() {
        // Duration: 10 seconds + 2 seconds per level
        return (10 + currentLevel * 2) * 20; // Convert to ticks
    }

    @Override
    public int getManaCost() {
        // Mana cost: 80 + 20 per level
        return 80 + (currentLevel - 1) * 20;
    }
}