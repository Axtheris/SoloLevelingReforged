package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * AI Goal for Shadow Soldiers to follow their owner
 */
public class ShadowSoldierFollowGoal extends Goal {

    private final ShadowSoldierEntity shadowSoldier;
    private final double speedModifier;
    private final float followDistance;
    private final float stopDistance;

    private Player owner;
    private int timeToRecalcPath;

    public ShadowSoldierFollowGoal(ShadowSoldierEntity shadowSoldier) {
        this(shadowSoldier, 1.0D, 8.0F, 2.0F);
    }

    public ShadowSoldierFollowGoal(ShadowSoldierEntity shadowSoldier, double speedModifier, float followDistance, float stopDistance) {
        this.shadowSoldier = shadowSoldier;
        this.speedModifier = speedModifier;
        this.followDistance = followDistance;
        this.stopDistance = stopDistance;
    }

    @Override
    public boolean canUse() {
        this.owner = shadowSoldier.getOwner();
        return owner != null && !shadowSoldier.isLeashed() && shadowSoldier.distanceTo(owner) > stopDistance;
    }

    @Override
    public boolean canContinueToUse() {
        return !shadowSoldier.getNavigation().isDone() &&
               shadowSoldier.distanceTo(owner) > stopDistance &&
               !shadowSoldier.isLeashed();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        shadowSoldier.getNavigation().moveTo(owner, speedModifier);
    }

    @Override
    public void stop() {
        shadowSoldier.getNavigation().stop();
        owner = null;
    }

    @Override
    public void tick() {
        shadowSoldier.getLookControl().setLookAt(owner, 10.0F, shadowSoldier.getMaxHeadXRot());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10; // Recalculate path every 10 ticks

            if (shadowSoldier.distanceTo(owner) > followDistance) {
                shadowSoldier.getNavigation().moveTo(owner, speedModifier);
            } else {
                shadowSoldier.getNavigation().stop();
            }
        }
    }
}