package net.xelpha.sololevelingreforged.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class FrostBladeItem extends SoloLevelingWeaponItem {

    public FrostBladeItem(Properties properties) {
        super(ModToolTiers.A_RANK, 10, -2.2F, properties,
            "\"A blade forged from eternal ice, capable of freezing enemies solid.\"",
            "Frostbite - 40% chance to freeze enemies with slowness and damage over time");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 40% chance to apply frost effects
        if (attacker.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1)); // 6 seconds of slowness level 2

            // Create ice particle effect
            if (attacker.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1, target.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}