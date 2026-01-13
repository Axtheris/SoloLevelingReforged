package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class DemonKingLongswordItem extends SoloLevelingWeaponItem {

    public DemonKingLongswordItem(Properties properties) {
        super(ModToolTiers.S_RANK, 20, -2.8F, properties,
            "\"The massive longsword of the Demon King, capable of cleaving through armies.\"",
            "Lightning Strike - Large lightning bolts damage nearby enemies");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Lightning strike - damage nearby enemies
        float baseDamage = 16.0F; // Represents 1600% of attack damage scaling

        // Find all entities within 4 blocks and deal lightning damage
        attacker.level().getEntities(attacker, attacker.getBoundingBox().inflate(4.0D)).forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity && livingEntity != attacker) {
                livingEntity.hurt(attacker.damageSources().lightningBolt(), baseDamage);
                // Create lightning particle effect
                for (int i = 0; i < 10; i++) {
                    attacker.level().addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        livingEntity.getX() + attacker.getRandom().nextDouble() - 0.5,
                        livingEntity.getY() + attacker.getRandom().nextDouble(),
                        livingEntity.getZ() + attacker.getRandom().nextDouble() - 0.5,
                        0, 0, 0);
                }
            }
        });

        return super.hurtEnemy(stack, target, attacker);
    }
}