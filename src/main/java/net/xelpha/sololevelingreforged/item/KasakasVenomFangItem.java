package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class KasakasVenomFangItem extends SoloLevelingWeaponItem {

    public KasakasVenomFangItem(Properties properties) {
        super(ModToolTiers.S_RANK, 8, -1.8F, properties,
            "\"A dagger crafted from the fang of the Blue Venom-Fanged Kasaka. The venom remains potent within the blade.\"",
            "Paralyze & Bleed - 40% chance to paralyze and cause bleeding damage");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 40% chance to apply paralysis and bleeding
        if (attacker.getRandom().nextFloat() < 0.4F) {
            // Paralysis effect (slowness level 3)
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2)); // 3 seconds

            // Bleeding effect (poison represents bleed damage)
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1)); // 5 seconds of poison level 2
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}