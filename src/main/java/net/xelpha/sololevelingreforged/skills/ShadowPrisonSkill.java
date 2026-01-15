package net.xelpha.sololevelingreforged.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Shadow Prison - Creates a dark sphere that immobilizes enemies
 * Prevents enemies from moving or attacking while inside the prison
 */
public class ShadowPrisonSkill extends Skill {

    public ShadowPrisonSkill() {
        super(
            Sololevelingreforged.loc("shadow_prison"),
            "Shadow Prison",
            "Create a sphere of darkness that immobilizes and weakens enemies caught inside.",
            SkillType.ACTIVE_DEFENSIVE,
            5, // Max level
            60, // Base mana cost
            200, // Base cooldown (10 seconds)
            12 // Unlock level
        );
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canActivate(player)) {
            player.sendSystemMessage(Component.literal("Cannot use Shadow Prison: On cooldown or insufficient mana!"));
            return false;
        }

        Level level = player.level();
        BlockPos center = player.blockPosition();

        // Calculate prison radius based on skill level
        double radius = 4.0 + currentLevel; // 5-9 blocks radius

        // Find all enemies in range
        AABB prisonArea = new AABB(center).inflate(radius);
        int affectedEnemies = 0;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, prisonArea)) {
            if (isValidPrisonTarget(entity, player)) {
                imprisonEntity(entity);
                affectedEnemies++;
            }
        }

        if (affectedEnemies == 0) {
            player.sendSystemMessage(Component.literal("No enemies found in range to imprison!"));
            return false;
        }

        // Create visual prison effect
        createPrisonVisuals(level, center, radius);

        // Play sound effect
        level.playSound(null, center.getX(), center.getY(), center.getZ(),
            SoundEvents.WARDEN_TENDRIL_CLICKS, player.getSoundSource(), 1.5F, 0.5F);

        // Send success message
        player.sendSystemMessage(Component.literal("Shadow Prison activated! " + affectedEnemies +
            " enemies imprisoned for " + (getEffectDuration() / 20) + " seconds!"));

        markUsed();
        return true;
    }

    private boolean isValidPrisonTarget(LivingEntity entity, ServerPlayer player) {
        // Don't imprison the player or their shadow soldiers
        if (entity == player) return false;
        if (entity instanceof net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity shadowSoldier) {
            return !shadowSoldier.isOwnedBy(player);
        }

        // Don't imprison other players (for now)
        if (entity instanceof net.minecraft.world.entity.player.Player) return false;

        // Only imprison monsters/hostile entities
        return entity.isAlive() && entity.getType().getCategory() != net.minecraft.world.entity.MobCategory.CREATURE;
    }

    private void imprisonEntity(LivingEntity entity) {
        // Apply slowness effect (immobilization)
        MobEffectInstance slownessEffect = new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            getEffectDuration(),
            10, // Maximum slowness (almost completely immobilized)
            false, false, true
        );

        // Apply weakness (reduced damage)
        MobEffectInstance weaknessEffect = new MobEffectInstance(
            MobEffects.WEAKNESS,
            getEffectDuration(),
            currentLevel - 1, // Damage reduction scales with level
            false, false, true
        );

        entity.addEffect(slownessEffect);
        entity.addEffect(weaknessEffect);

            // Visual effect on the imprisoned entity
            entity.level().addParticle(ParticleTypes.SMOKE,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                0.3, 0.3, 0.3);
    }

    private void createPrisonVisuals(Level level, BlockPos center, double radius) {
        // Create a spherical particle effect
        int particleCount = (int) (radius * 20); // More particles for larger prisons

        for (int i = 0; i < particleCount; i++) {
            // Generate random point on sphere surface
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.sin(phi) * Math.sin(theta);
            double z = center.getZ() + radius * Math.cos(phi);

            // Dark particles for the prison sphere
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
            level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f; // Not a direct damage skill
    }

    @Override
    public int getEffectDuration() {
        // Duration: 5 seconds + 1 second per level
        return (5 + currentLevel) * 20; // Convert to ticks
    }

    @Override
    public int getManaCost() {
        return 60 + (currentLevel - 1) * 15; // 60, 75, 90, 105, 120 mana
    }
}