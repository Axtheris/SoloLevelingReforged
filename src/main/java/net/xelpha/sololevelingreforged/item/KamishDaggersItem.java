package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class KamishDaggersItem extends SoloLevelingWeaponItem {

    public KamishDaggersItem(Properties properties) {
        super(ModToolTiers.S_RANK, 14, -1.0F, properties,
            "\"The razor-sharp teeth of the Shadow Monarch Kamish, capable of tearing through reality itself.\"",
            "Shadow Rend - Advanced shadow manipulation with enhanced darkness effects");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Enhanced shadow effects
        if (attacker.getRandom().nextFloat() < 0.45F) {
            // Darkness effect (blindness + confusion)
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0)); // 4 seconds of blindness
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0)); // 5 seconds of nausea

            // Shadow teleport - small chance to teleport attacker toward target
            if (attacker.getRandom().nextFloat() < 0.3F && attacker instanceof Player player) {
                // Teleport closer to target for follow-up attacks
                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance > 2) {
                    double teleportDistance = Math.min(distance * 0.7, 3.0);
                    double newX = player.getX() + (dx / distance) * teleportDistance;
                    double newZ = player.getZ() + (dz / distance) * teleportDistance;

                    player.teleportTo(newX, player.getY(), newZ);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}