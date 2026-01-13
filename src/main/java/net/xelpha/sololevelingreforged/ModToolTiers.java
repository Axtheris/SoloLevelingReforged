package net.xelpha.sololevelingreforged;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum ModToolTiers implements Tier {
    E_RANK(1, 2500, 6.0F, 18.0F, 12),
    D_RANK(2, 3500, 7.0F, 32.0F, 14),
    C_RANK(3, 5000, 8.0F, 48.0F, 16),
    B_RANK(4, 7000, 9.0F, 65.0F, 18),
    A_RANK(5, 10000, 10.0F, 85.0F, 22),
    S_RANK(6, 15000, 12.0F, 110.0F, 28);

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;

    ModToolTiers(int level, int uses, float speed, float damage, int enchantmentValue) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // For now, return empty ingredient - can be customized later if needed
        return Ingredient.EMPTY;
    }
}