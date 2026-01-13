package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class GravityDaggerItem extends SoloLevelingWeaponItem {

    public GravityDaggerItem(Properties properties) {
        super(ModToolTiers.A_RANK, 8, -1.6F, properties,
            "\"A dagger infused with gravitational energy, capable of crushing enemies with immense weight.\"",
            "Gravity Manipulation - Creates gravitational distortions to control enemy positioning");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Gravity manipulation - pull enemies closer and apply heavy gravity
        if (attacker.getRandom().nextFloat() < 0.35F) {
            // Pull effect (slight knockback toward attacker)
            double dx = attacker.getX() - target.getX();
            double dz = attacker.getZ() - target.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                double pullStrength = 0.5;
                target.setDeltaMovement(
                    target.getDeltaMovement().x + (dx / distance) * pullStrength,
                    target.getDeltaMovement().y,
                    target.getDeltaMovement().z + (dz / distance) * pullStrength
                );
            }

            // Heavy gravity effect
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1)); // 4 seconds of slowness level 2
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, -5)); // Reduced jump height
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}