package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Custom attack goal for shadow soldiers with enhanced targeting
 */
public class ShadowSoldierAttackGoal extends MeleeAttackGoal {

    private final ShadowSoldierEntity shadowSoldier;

    public ShadowSoldierAttackGoal(ShadowSoldierEntity shadowSoldier) {
        super(shadowSoldier, 1.2D, false);
        this.shadowSoldier = shadowSoldier;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.shadowSoldier.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Don't attack the owner or other shadow soldiers owned by the same player
        if (target == shadowSoldier.getOwner()) {
            return false;
        }

        if (target instanceof ShadowSoldierEntity otherShadow &&
            shadowSoldier.getOwner() != null &&
            otherShadow.getOwner() == shadowSoldier.getOwner()) {
            return false;
        }

        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();

        // Enhanced attack speed when berserk
        if (shadowSoldier.isBerserk()) {
            // Reduce attack cooldown when berserk
            this.attackTime = Math.max(0, this.attackTime - 1);
        }
    }
}