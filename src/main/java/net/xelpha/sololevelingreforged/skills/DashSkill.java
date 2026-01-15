package net.xelpha.sololevelingreforged.skills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Dash - High-speed movement ability
 * Instantly moves the player forward at high speed
 */
public class DashSkill extends Skill {

    public DashSkill() {
        super(
            Sololevelingreforged.loc("dash"),
            "Dash",
            "Instantly dash forward at high speed, bypassing obstacles and closing distances.",
            SkillType.ACTIVE_UTILITY,
            5, // Max level
            30, // Base mana cost
            120, // Base cooldown (6 seconds)
            8 // Unlock level
        );
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canActivate(player)) {
            player.sendSystemMessage(Component.literal("Cannot use Dash: On cooldown or insufficient mana!"));
            return false;
        }

        // Calculate dash direction (player's looking direction)
        Vec3 lookDirection = player.getLookAngle();
        Vec3 dashVector = lookDirection.scale(getDashDistance());

        // Check for collisions and adjust if needed
        if (!canDashThrough(player, dashVector)) {
            // Try to find a valid dash position
            dashVector = findValidDashPosition(player, lookDirection);
            if (dashVector == null) {
                player.sendSystemMessage(Component.literal("No valid dash path found!"));
                return false;
            }
        }

        // Perform the dash
        Vec3 newPosition = player.position().add(dashVector);
        player.teleportTo(newPosition.x, newPosition.y, newPosition.z);

        // Play effects
        playDashEffects(player);

        // Send success message
        player.sendSystemMessage(Component.literal("Dashed forward " +
            String.format("%.1f", getDashDistance()) + " blocks!"));

        markUsed();
        return true;
    }

    private boolean canDashThrough(ServerPlayer player, Vec3 dashVector) {
        // Simple collision check - check a few points along the dash path
        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(dashVector);

        // Check if the end position is valid (not inside blocks)
        return player.level().isEmptyBlock(player.blockPosition().offset(
            (int)dashVector.x, (int)dashVector.y, (int)dashVector.z)) &&
            player.level().isEmptyBlock(player.blockPosition().offset(
            (int)dashVector.x, (int)dashVector.y + 1, (int)dashVector.z));
    }

    private Vec3 findValidDashPosition(ServerPlayer player, Vec3 lookDirection) {
        // Try progressively shorter dashes until we find a valid position
        for (double distance = getDashDistance() * 0.8; distance > 1.0; distance -= 0.5) {
            Vec3 testVector = lookDirection.scale(distance);
            if (canDashThrough(player, testVector)) {
                return testVector;
            }
        }
        return null;
    }

    private void playDashEffects(ServerPlayer player) {
        // Sound effect
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.2F);

        // Particles
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
            player.getX(), player.getY(), player.getZ(),
            20, 0.2, 0.2, 0.2, 0.1);
    }

    private double getDashDistance() {
        // Base distance: 8 blocks + 2 blocks per level
        return 8.0 + (currentLevel * 2.0);
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f; // Dash doesn't directly affect damage
    }

    @Override
    public int getEffectDuration() {
        return 0; // Instant effect
    }

    @Override
    public int getManaCost() {
        return 30 + (currentLevel - 1) * 10; // 30, 40, 50, 60, 70 mana
    }
}