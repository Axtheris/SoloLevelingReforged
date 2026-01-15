package net.xelpha.sololevelingreforged.skills;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.Projectile;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.ModEntities;
import net.xelpha.sololevelingreforged.entity.ShadowDaggerEntity;

/**
 * Dagger Throw - Ranged dagger attack with homing capabilities
 * Throws magical daggers that seek out enemies
 */
public class DaggerThrowSkill extends Skill {

    public DaggerThrowSkill() {
        super(
            Sololevelingreforged.loc("dagger_throw"),
            "Dagger Throw",
            "Throw magical daggers that home in on enemies. Higher levels increase damage and homing accuracy.",
            SkillType.ACTIVE_OFFENSIVE,
            5, // Max level
            25, // Base mana cost
            80, // Base cooldown (4 seconds)
            7 // Unlock level
        );
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canActivate(player)) {
            player.sendSystemMessage(Component.literal("Cannot use Dagger Throw: On cooldown or insufficient mana!"));
            return false;
        }

        // Calculate number of daggers based on skill level
        int numDaggers = 1 + (currentLevel - 1) / 2; // 1 dagger at lv1, 2 at lv3, 3 at lv5

        // Spawn daggers
        for (int i = 0; i < numDaggers; i++) {
            spawnHomingDagger(player, i, numDaggers);
        }

        // Play sound effect
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_THROW, player.getSoundSource(), 1.0F, 1.2F);

        // Send success message
        player.sendSystemMessage(Component.literal("Thrown " + numDaggers + " homing dagger" +
            (numDaggers > 1 ? "s" : "") + "!"));

        markUsed();
        return true;
    }

    private void spawnHomingDagger(ServerPlayer player, int daggerIndex, int numDaggers) {
        // Create the projectile entity
        ShadowDaggerEntity dagger = new ShadowDaggerEntity(ModEntities.SHADOW_DAGGER.get(), player.level());

        // Set initial position with slight spread for multiple daggers
        double spread = numDaggers > 1 ? (daggerIndex - (numDaggers - 1) / 2.0) * 0.3 : 0;
        dagger.setPos(
            player.getX(),
            player.getY() + player.getEyeHeight(),
            player.getZ()
        );

        // Set initial velocity with some randomness
        dagger.shootFromRotation(player, player.getXRot(), player.getYRot() + (float)(spread * 10),
            0.0F, 2.5F, 1.0F);

        // Add to world
        player.level().addFreshEntity(dagger);
    }

    @Override
    public float getDamageMultiplier() {
        // Base damage: 1.0 + 0.3 per level
        return 1.0f + (currentLevel * 0.3f);
    }

    @Override
    public int getEffectDuration() {
        return 0; // Instant effect
    }

    @Override
    public int getManaCost() {
        return 25 + (currentLevel - 1) * 5; // 25, 30, 35, 40, 45 mana
    }
}