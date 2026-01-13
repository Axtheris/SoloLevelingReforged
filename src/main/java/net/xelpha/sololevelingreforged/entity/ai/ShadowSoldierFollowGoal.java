package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Goal for shadow soldiers to follow their owner
 */
public class ShadowSoldierFollowGoal extends Goal {

    private final ShadowSoldierEntity shadowSoldier;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;
    private Player owner;
    private int timeToRecalcPath;

    public ShadowSoldierFollowGoal(ShadowSoldierEntity shadowSoldier) {
        this(shadowSoldier, 1.0D, 10.0F, 2.0F);
    }

    public ShadowSoldierFollowGoal(ShadowSoldierEntity shadowSoldier, double speedModifier,
                                  float stopDistance, float startDistance) {
        this.shadowSoldier = shadowSoldier;
        this.speedModifier = speedModifier;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
    }

    @Override
    public boolean canUse() {
        Player owner = shadowSoldier.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }

        // Don't follow if currently attacking
        if (shadowSoldier.getTarget() != null) {
            return false;
        }

        double distance = shadowSoldier.distanceTo(owner);
        return distance > startDistance * startDistance;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.owner == null || !this.owner.isAlive()) {
            return false;
        }

        // Stop following if we have a target to attack
        if (shadowSoldier.getTarget() != null) {
            return false;
        }

        double distance = shadowSoldier.distanceTo(this.owner);
        return distance > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        this.owner = shadowSoldier.getOwner();
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        shadowSoldier.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        shadowSoldier.getLookControl().setLookAt(this.owner, 10.0F, (float) shadowSoldier.getMaxHeadXRot());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10; // Recalculate path every 10 ticks

            if (!shadowSoldier.isLeashed() && !shadowSoldier.isPassenger()) {
                double distance = shadowSoldier.distanceTo(this.owner);

                if (distance > stopDistance) {
                    shadowSoldier.getNavigation().moveTo(this.owner, speedModifier);
                } else {
                    shadowSoldier.getNavigation().stop();
                }
            }
        }
    }
}