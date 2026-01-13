package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class MoonlightRefractorItem extends SoloLevelingWeaponItem {

    public MoonlightRefractorItem(Properties properties) {
        super(ModToolTiers.A_RANK, 12, -2.0F, properties,
            "\"A mystical sword that refracts moonlight into devastating energy blasts.\"",
            "Light Refraction - Deals light elemental damage and creates reflective barriers");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Light refraction effect - bonus light damage
        target.hurt(attacker.damageSources().magic(), 8.0F); // Light elemental damage

        // Create light particle effect
        for (int i = 0; i < 15; i++) {
            attacker.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                target.getX() + attacker.getRandom().nextDouble() - 0.5,
                target.getY() + attacker.getRandom().nextDouble(),
                target.getZ() + attacker.getRandom().nextDouble() - 0.5,
                0, 0.1, 0);
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}