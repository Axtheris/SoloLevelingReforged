package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class BarukasDaggerItem extends SoloLevelingWeaponItem {

    public BarukasDaggerItem(Properties properties) {
        super(ModToolTiers.A_RANK, 6, -1.4F, properties,
            "\"The signature dagger of S-rank hunter Baruka, known for its incredible speed and precision.\"",
            "Wind Element & Agility - Deals wind damage and grants speed boost");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Wind elemental damage bonus
        target.hurt(attacker.damageSources().magic(), 3.0F); // Additional wind damage

        // Grant speed boost to wielder
        if (attacker instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0)); // 3 seconds of speed
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}