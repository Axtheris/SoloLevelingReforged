package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class AwakenedVenomFangItem extends SoloLevelingWeaponItem {

    public AwakenedVenomFangItem(Properties properties) {
        super(ModToolTiers.S_RANK, 12, -1.6F, properties,
            "\"An awakened form of the Kasaka's venom fang, pulsing with deadly energy.\"",
            "Enhanced Venom Strike - 50% chance to poison and weaken enemies for 7 seconds");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 50% chance to apply poison and weakness
        if (attacker.getRandom().nextFloat() < 0.5F) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1)); // 7 seconds of poison level 2
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0)); // 7 seconds of weakness
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}