package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Target goal for Shadow Soldiers to defend their owner by attacking threats
 */
public class ShadowSoldierDefendGoal extends TargetGoal {

    private final ShadowSoldierEntity shadowSoldier;
    private final TargetingConditions targetingConditions;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public ShadowSoldierDefendGoal(ShadowSoldierEntity shadowSoldier) {
        super(shadowSoldier, false);
        this.shadowSoldier = shadowSoldier;
        this.targetingConditions = TargetingConditions.forCombat()
            .range(32.0D)
            .selector(entity -> isValidDefendTarget(entity));
    }

    private boolean isValidDefendTarget(LivingEntity entity) {
        // Only attack entities that are targeting the owner
        if (shadowSoldier.getOwner() == null) return false;

        // Check if this entity is attacking the owner
        if (entity.getLastHurtByMob() == shadowSoldier.getOwner()) {
            return true;
        }

        // Check if entity is targeting the owner (for mobs that have target selectors)
        if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == shadowSoldier.getOwner()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canUse() {
        if (shadowSoldier.getOwner() == null) {
            return false;
        }

        // Check if owner was recently hurt
        this.ownerLastHurtBy = shadowSoldier.getOwner().getLastHurtByMob();
        int lastHurtTime = shadowSoldier.getOwner().getLastHurtByMobTimestamp();
        return lastHurtTime != this.timestamp &&
               this.ownerLastHurtBy != null &&
               this.canAttack(this.ownerLastHurtBy, this.targetingConditions);
    }

    @Override
    public void start() {
        shadowSoldier.setTarget(this.ownerLastHurtBy);
        this.timestamp = shadowSoldier.getOwner().getLastHurtByMobTimestamp();

        // Send message to owner about defense
        if (shadowSoldier.getOwner() != null) {
            shadowSoldier.getOwner().sendSystemMessage(
                net.minecraft.network.chat.Component.literal("Shadow soldier defending against " +
                    ownerLastHurtBy.getType().getDescription().getString() + "!")
            );
        }

        super.start();
    }
}