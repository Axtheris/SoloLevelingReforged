package net.xelpha.sololevelingreforged.skills;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.ModEntities;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Shadow Extraction - The signature skill of Sung Jin-Woo
 * Converts defeated enemies into shadow soldiers that fight for the player
 */
public class ShadowExtractionSkill extends Skill {

    public ShadowExtractionSkill() {
        super(
            Sololevelingreforged.loc("shadow_extraction"),
            "Shadow Extraction",
            "Extract shadows from defeated enemies to create loyal soldiers that fight for you.",
            SkillType.ACTIVE_UTILITY,
            10, // Max level
            50, // Base mana cost
            200, // Base cooldown (10 seconds)
            5 // Unlock level
        );
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canActivate(player)) {
            player.sendSystemMessage(Component.literal("Cannot use Shadow Extraction: On cooldown or insufficient mana!"));
            return false;
        }

        // Find nearby defeated enemies (within 10 blocks, dead for less than 5 seconds)
        ServerLevel level = player.serverLevel();
        boolean foundTarget = false;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(10.0D))) {

            if (entity != player && entity.isDeadOrDying() && canExtractShadow(entity)) {
                // Create shadow soldier
                if (spawnShadowSoldier(player, entity)) {
                    foundTarget = true;

                    // Play extraction effects
                    playExtractionEffects(level, entity);

                    // Send success message
                    player.sendSystemMessage(Component.literal("Shadow extracted from " +
                        entity.getType().getDescription().getString() + "!"));

                    break; // Only extract one shadow at a time
                }
            }
        }

        if (!foundTarget) {
            player.sendSystemMessage(Component.literal("No suitable targets found for shadow extraction!"));
            return false;
        }

        markUsed();
        return true;
    }

    private boolean canExtractShadow(LivingEntity entity) {
        // Check if entity died recently (within 5 seconds)
        return entity.deathTime < 100 && entity.deathTime > 0;
    }

    private boolean spawnShadowSoldier(ServerPlayer player, LivingEntity sourceEntity) {
        ServerLevel level = player.serverLevel();

        // Create shadow soldier at the entity's location
        ShadowSoldierEntity shadowSoldier = new ShadowSoldierEntity(ModEntities.SHADOW_SOLDIER.get(), level, player, sourceEntity);

        // Set position slightly above ground
        shadowSoldier.setPos(sourceEntity.getX(), sourceEntity.getY() + 0.1D, sourceEntity.getZ());

        // Add to world
        boolean added = level.addFreshEntity(shadowSoldier);

        if (added) {
            // Bind soldier to player (this will be implemented in ShadowSoldierEntity)
            shadowSoldier.bindToPlayer(player);

            // Send success message
            player.sendSystemMessage(Component.literal("Shadow soldier created! Army size: " +
                getPlayerShadowCount(player) + "/" + getMaxShadowArmySize()));
        }

        return added;
    }

    private void playExtractionEffects(ServerLevel level, LivingEntity entity) {
        // Dark particles
        level.sendParticles(ParticleTypes.SMOKE,
            entity.getX(), entity.getY() + 1, entity.getZ(),
            20, 0.5, 0.5, 0.5, 0.1);

        // Purple particles for shadow effect
        level.sendParticles(ParticleTypes.PORTAL,
            entity.getX(), entity.getY() + 1, entity.getZ(),
            30, 0.3, 0.3, 0.3, 0.5);

        // Sound effect
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
    }

    private int getPlayerShadowCount(ServerPlayer player) {
        // Count shadow soldiers owned by this player
        return (int) player.serverLevel().getEntitiesOfClass(ShadowSoldierEntity.class,
            player.getBoundingBox().inflate(200.0D))
            .stream()
            .filter(soldier -> soldier.getOwner() == player)
            .count();
    }

    private int getMaxShadowArmySize() {
        // Scale with skill level: 1 + level (max 11 soldiers)
        return 1 + currentLevel;
    }

    @Override
    public float getDamageMultiplier() {
        // Shadow soldiers get stronger with skill level
        return 0.8f + (currentLevel * 0.1f);
    }

    @Override
    public int getEffectDuration() {
        // Shadow soldiers are permanent until death
        return Integer.MAX_VALUE;
    }
}