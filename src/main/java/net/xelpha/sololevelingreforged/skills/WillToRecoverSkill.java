package net.xelpha.sololevelingreforged.skills;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

/**
 * Will to Recover - Passive skill that auto-heals when HP drops below 30%
 * Sung Jin-Woo's legendary recovery ability
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID)
public class WillToRecoverSkill extends Skill {

    // Track last heal time per player to prevent spam healing
    private static final java.util.Map<java.util.UUID, Long> lastHealTimes = new java.util.HashMap<>();

    public WillToRecoverSkill() {
        super(
            Sololevelingreforged.loc("will_to_recover"),
            "Will to Recover",
            "Automatically recover health when HP drops below 30%. Higher levels increase healing amount and reduce cooldown.",
            SkillType.PASSIVE_BUFF,
            5, // Max level
            0, // No mana cost (passive)
            0, // No cooldown (passive)
            15 // Unlock level
        );
    }

    @Override
    public boolean activate(net.minecraft.server.level.ServerPlayer player) {
        // Passive skill - always "active"
        return false;
    }

    @Override
    public float getDamageMultiplier() {
        return 1.0f; // No direct damage effect
    }

    @Override
    public int getEffectDuration() {
        return Integer.MAX_VALUE; // Always active
    }

    /**
     * Event handler for auto-healing when health is low
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        // Check if player has this skill
        player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
            .ifPresent(cap -> {
                Skill willToRecover = cap.getSkill(Sololevelingreforged.loc("will_to_recover"));
                if (willToRecover instanceof WillToRecoverSkill skill) {
                    skill.checkAndHeal(player);
                }
            });
    }

    private void checkAndHeal(net.minecraft.server.level.ServerPlayer player) {
        // Check if health is below 30%
        float healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent >= 0.3f) {
            return; // Health is fine
        }

        // Check cooldown (prevents spam healing)
        long currentTime = System.currentTimeMillis();
        long lastHealTime = lastHealTimes.getOrDefault(player.getUUID(), 0L);
        long cooldownMs = getHealCooldownMs();

        if (currentTime - lastHealTime < cooldownMs) {
            return; // Still on cooldown
        }

        // Heal the player
        float healAmount = getHealAmount(player);
        player.heal(healAmount);

        // Update last heal time
        lastHealTimes.put(player.getUUID(), currentTime);

        // Visual and audio effects
        playHealEffects(player);

        // Send message
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            String.format("Will to Recover activated! Healed for %.1f HP!", healAmount)));
    }

    private float getHealAmount(net.minecraft.server.level.ServerPlayer player) {
        // Base heal: 10% of max HP + 5% per level
        float baseHeal = player.getMaxHealth() * (0.1f + currentLevel * 0.05f);

        // Minimum heal of 4 HP
        return Math.max(4.0f, baseHeal);
    }

    private long getHealCooldownMs() {
        // Cooldown: 30 seconds - 3 seconds per level
        int cooldownSeconds = Math.max(5, 30 - currentLevel * 3);
        return cooldownSeconds * 1000L;
    }

    private void playHealEffects(net.minecraft.server.level.ServerPlayer player) {
        // Healing particles
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
            player.getX(), player.getY() + player.getBbHeight() + 0.5, player.getZ(),
            10, 0.3, 0.1, 0.3, 0.1);

        // Gentle healing sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 0.7F, 1.5F);
    }
}