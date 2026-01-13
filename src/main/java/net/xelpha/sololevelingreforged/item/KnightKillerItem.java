package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class KnightKillerItem extends SoloLevelingWeaponItem {

    public KnightKillerItem(Properties properties) {
        super(ModToolTiers.S_RANK, 10, -2.2F, properties,
            "\"A legendary sword designed specifically to slay armored knights and powerful beings.\"",
            "Armor Piercing - Deals bonus damage to armored enemies");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Deal bonus damage based on target's armor
        float armorValue = target.getArmorValue();
        if (armorValue > 0) {
            // Bonus damage scales with armor (up to 50% bonus damage for heavily armored targets)
            float bonusDamage = armorValue * 0.1F;
            target.hurt(attacker.damageSources().generic(), bonusDamage);
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}