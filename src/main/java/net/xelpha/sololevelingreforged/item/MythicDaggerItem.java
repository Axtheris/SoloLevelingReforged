package net.xelpha.sololevelingreforged.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.xelpha.sololevelingreforged.ModToolTiers;

public class MythicDaggerItem extends SoloLevelingWeaponItem {

    public MythicDaggerItem(Properties properties) {
        super(ModToolTiers.S_RANK, 16, -1.3F, properties,
            "\"A dagger of mythical power, said to be capable of slaying gods themselves.\"",
            "Mythic Power - Transcends standard rarity with supernatural mystical properties");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Mythic effects - multiple supernatural debuffs
        if (attacker.getRandom().nextFloat() < 0.4F) {
            // Divine smiting effects
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2)); // 6 seconds of wither level 3
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 2)); // 6 seconds of weakness level 3
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0)); // 5 seconds of glowing (marked for death)

            // Create divine particle effect
            for (int i = 0; i < 20; i++) {
                attacker.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX() + attacker.getRandom().nextDouble() - 0.5,
                    target.getY() + attacker.getRandom().nextDouble(),
                    target.getZ() + attacker.getRandom().nextDouble() - 0.5,
                    0, 0.05, 0);
            }

            // Boost attacker with divine strength
            if (attacker instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 1)); // 4 seconds of strength level 2
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0)); // 3 seconds of regeneration
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}