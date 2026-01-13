package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

import javax.annotation.Nullable;

/**
 * Target selection goal for shadow soldiers
 */
public class ShadowSoldierTargetGoal extends TargetGoal {

    private final ShadowSoldierEntity shadowSoldier;

    public ShadowSoldierTargetGoal(ShadowSoldierEntity shadowSoldier) {
        super(shadowSoldier, false);
        this.shadowSoldier = shadowSoldier;
    }

    @Override
    public boolean canUse() {
        return findTarget();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = shadowSoldier.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Stop targeting if the target is the owner or another shadow soldier
        if (isInvalidTarget(target)) {
            return false;
        }

        return super.canContinueToUse();
    }

    protected boolean findTarget() {
        // Priority 1: Attack enemies attacking the owner
        LivingEntity ownerTarget = findOwnerAttacker();
        if (ownerTarget != null) {
            shadowSoldier.setTarget(ownerTarget);
            return true;
        }

        // Priority 2: Attack enemies near the owner
        LivingEntity nearbyThreat = findNearbyThreat();
        if (nearbyThreat != null) {
            shadowSoldier.setTarget(nearbyThreat);
            return true;
        }

        return false;
    }

    /**
     * Find entities attacking the owner
     */
    @Nullable
    private LivingEntity findOwnerAttacker() {
        Player owner = shadowSoldier.getOwner();
        if (owner == null) return null;

        // Check if owner is being attacked
        LivingEntity ownerAttacker = owner.getLastAttacker();
        if (ownerAttacker != null && ownerAttacker.isAlive() &&
            !isInvalidTarget(ownerAttacker) &&
            shadowSoldier.distanceTo(ownerAttacker) <= 16.0D) {
            return ownerAttacker;
        }

        return null;
    }

    /**
     * Find nearby hostile entities
     */
    @Nullable
    private LivingEntity findNearbyThreat() {
        Player owner = shadowSoldier.getOwner();
        if (owner == null) return null;

        double searchRange = 12.0D;

        // Find closest valid hostile target
        LivingEntity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : shadowSoldier.level().getEntitiesOfClass(LivingEntity.class,
             shadowSoldier.getBoundingBox().inflate(searchRange))) {

            if (isInvalidTarget(entity)) continue;

            // Prioritize monsters and hostile players
            if (!(entity instanceof Monster) && !(entity instanceof Player)) continue;

            double distance = shadowSoldier.distanceTo(entity);
            if (distance < closestDistance) {
                closestTarget = entity;
                closestDistance = distance;
            }
        }

        return closestTarget;
    }

    /**
     * Check if an entity is an invalid target
     */
    private boolean isInvalidTarget(LivingEntity entity) {
        // Don't target owner
        if (entity == shadowSoldier.getOwner()) {
            return true;
        }

        // Don't target other shadow soldiers owned by the same player
        if (entity instanceof ShadowSoldierEntity otherShadow) {
            return shadowSoldier.getOwner() != null &&
                   otherShadow.getOwner() == shadowSoldier.getOwner();
        }

        // Don't target non-hostile entities
        if (!(entity instanceof Monster) && !(entity instanceof Player)) {
            return true;
        }

        // Don't target players who aren't in combat with owner
        if (entity instanceof Player player && shadowSoldier.getOwner() != null) {
            // Could add more complex logic here for PvP scenarios
            return false; // For now, allow targeting other players
        }

        return false;
    }
}