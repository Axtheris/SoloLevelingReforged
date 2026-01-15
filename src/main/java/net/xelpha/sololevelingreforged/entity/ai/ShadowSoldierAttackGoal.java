package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Enhanced attack goal for Shadow Soldiers with better target prioritization
 */
public class ShadowSoldierAttackGoal extends MeleeAttackGoal {

    private final ShadowSoldierEntity shadowSoldier;
    private LivingEntity pendingTarget;
    private int raiseArmTicks;

    public ShadowSoldierAttackGoal(ShadowSoldierEntity shadowSoldier, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(shadowSoldier, speedModifier, followingTargetEvenIfNotSeen);
        this.shadowSoldier = shadowSoldier;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = shadowSoldier.getTarget();
        if (target == null) {
            return false;
        }

        // Don't attack owner or other owner's shadows
        if (target == shadowSoldier.getOwner()) {
            return false;
        }

        if (target instanceof ShadowSoldierEntity otherShadow) {
            if (otherShadow.isOwnedBy(shadowSoldier.getOwner())) {
                return false;
            }
        }

        return super.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        shadowSoldier.setAggressive(false);
        this.raiseArmTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = shadowSoldier.getTarget();
        if (target != null) {
            shadowSoldier.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Attack animation
            if (shadowSoldier.getSensing().hasLineOfSight(target)) {
                ++this.raiseArmTicks;
                if (this.raiseArmTicks >= 5) {
                    shadowSoldier.setAggressive(true);
                }
            } else {
                this.raiseArmTicks = 0;
                shadowSoldier.setAggressive(false);
            }
        } else {
            shadowSoldier.setAggressive(false);
            this.raiseArmTicks = 0;
        }
    }

    @Override
    protected double getAttackReachSqr(LivingEntity attackTarget) {
        // Shadow soldiers have slightly extended reach
        return super.getAttackReachSqr(attackTarget) + 1.0D;
    }
}