package net.xelpha.sololevelingreforged.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public abstract class SoloLevelingWeaponItem extends SwordItem {

    private final String weaponLore;
    private final String weaponAbility;

    public SoloLevelingWeaponItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties, String weaponLore, String weaponAbility) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.weaponLore = weaponLore;
        this.weaponAbility = weaponAbility;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // Add weapon lore
        if (weaponLore != null && !weaponLore.isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(weaponLore).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        }

        // Add weapon ability
        if (weaponAbility != null && !weaponAbility.isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Ability: " + weaponAbility).withStyle(ChatFormatting.GOLD));
        }
    }
}