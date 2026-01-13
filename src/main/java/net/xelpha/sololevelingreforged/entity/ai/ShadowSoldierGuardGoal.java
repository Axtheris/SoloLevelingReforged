package net.xelpha.sololevelingreforged.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Goal for shadow soldiers to guard their owner when not attacking
 */
public class ShadowSoldierGuardGoal extends Goal {

    private final ShadowSoldierEntity shadowSoldier;
    private Player owner;
    private int guardTime;

    public ShadowSoldierGuardGoal(ShadowSoldierEntity shadowSoldier) {
        this.shadowSoldier = shadowSoldier;
    }

    @Override
    public boolean canUse() {
        Player owner = shadowSoldier.getOwner();
        if (owner == null || shadowSoldier.getTarget() != null) {
            return false;
        }

        // Guard when close to owner and not in combat
        double distance = shadowSoldier.distanceTo(owner);
        return distance <= 8.0D;
    }

    @Override
    public void start() {
        this.owner = shadowSoldier.getOwner();
        this.guardTime = 0;
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        guardTime++;

        // Every 20 ticks, check if we should reposition for better guarding
        if (guardTime % 20 == 0) {
            repositionForGuarding();
        }

        // Look at potential threats
        lookForThreats();
    }

    /**
     * Reposition to better protect the owner
     */
    private void repositionForGuarding() {
        if (owner == null) return;

        // Position slightly behind and to the side of the owner
        // This creates a protective formation
        double angle = Math.toRadians(owner.getYRot() + 135); // 135 degrees behind owner
        double distance = 2.0D;

        double targetX = owner.getX() + Math.sin(angle) * distance;
        double targetZ = owner.getZ() + Math.cos(angle) * distance;

        shadowSoldier.getNavigation().moveTo(targetX, owner.getY(), targetZ, 1.0D);
    }

    /**
     * Look for potential threats around the owner
     */
    private void lookForThreats() {
        if (owner == null) return;

        // Look in the direction the owner is facing, but scan for threats
        shadowSoldier.getLookControl().setLookAt(owner, 30.0F, 30.0F);
    }
}