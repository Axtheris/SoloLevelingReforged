package net.xelpha.sololevelingreforged.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.xelpha.sololevelingreforged.ModEntities;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierFollowGoal;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierAttackGoal;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierDefendGoal;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Shadow Soldier Entity - Loyal soldiers created through Shadow Extraction
 * These entities fight for their master and inherit stats from the original entity
 */
public class ShadowSoldierEntity extends Monster {

    private UUID ownerUUID;
    private LivingEntity sourceEntity; // The entity this shadow was extracted from

    // Shadow soldier properties
    private float inheritedHealth;
    private float inheritedDamage;
    private float inheritedSpeed;

    public ShadowSoldierEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired(); // Don't despawn naturally
    }

    public ShadowSoldierEntity(EntityType<? extends Monster> type, Level level, ServerPlayer owner, LivingEntity sourceEntity) {
        super(type, level);
        this.ownerUUID = owner.getUUID();
        this.sourceEntity = sourceEntity;

        // Inherit stats from source entity
        inheritStats(sourceEntity);

        // Set up visual appearance
        setupVisualAppearance();

        this.setPersistenceRequired();
    }

    public ShadowSoldierEntity(Level level, ServerPlayer owner, LivingEntity sourceEntity) {
        super(ModEntities.SHADOW_SOLDIER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.sourceEntity = sourceEntity;

        // Inherit stats from source entity
        inheritStats(sourceEntity);

        // Set up visual appearance
        setupVisualAppearance();

        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        // Core AI goals
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ShadowSoldierFollowGoal(this));
        this.goalSelector.addGoal(3, new ShadowSoldierAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true,
            entity -> isValidTarget(entity)));
        this.targetSelector.addGoal(3, new ShadowSoldierDefendGoal(this));
    }

    private void inheritStats(LivingEntity sourceEntity) {
        // Inherit base stats from the source entity
        this.inheritedHealth = Math.max(20.0F, (float)sourceEntity.getMaxHealth() * 0.6F); // 60% of original health
        this.inheritedDamage = sourceEntity instanceof Mob mob ?
            Math.max(2.0F, (float)mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.7F) : 3.0F;
        this.inheritedSpeed = (float)(sourceEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.2F); // Slightly faster

        // Apply inherited stats
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(inheritedHealth);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(inheritedDamage);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(inheritedSpeed);

        // Heal to full
        this.setHealth(inheritedHealth);
    }

    private void setupVisualAppearance() {
        // Make shadow soldiers look shadowy and menacing
        // This would involve custom models/renderers in a full implementation
        // For now, we use the base monster appearance with dark effects
    }

    private boolean isValidTarget(LivingEntity entity) {
        // Don't attack the owner or other shadow soldiers owned by the same player
        if (entity == getOwner()) return false;

        if (entity instanceof ShadowSoldierEntity shadowSoldier) {
            return !shadowSoldier.isOwnedBy(getOwner());
        }

        // Attack all other living entities (including players who aren't the owner)
        return true;
    }

    // ===== OWNER MANAGEMENT =====

    public void bindToPlayer(ServerPlayer player) {
        this.ownerUUID = player.getUUID();
    }

    @Nullable
    public ServerPlayer getOwner() {
        if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().getPlayer(this.ownerUUID);
        }
        return null;
    }

    public boolean isOwnedBy(@Nullable Player player) {
        return player != null && player.getUUID().equals(this.ownerUUID);
    }

    // ===== BEHAVIOR =====

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Shadow soldiers are resistant to damage from their owner
        if (source.getEntity() == getOwner()) {
            return false; // Owner cannot damage their own shadows
        }

        // Take reduced damage from all sources (shadows are durable)
        float reducedAmount = amount * 0.7F;
        return super.hurt(source, reducedAmount);
    }

    @Override
    public void tick() {
        super.tick();

        // Visual effects
        if (this.level().isClientSide && this.tickCount % 10 == 0) {
            // Add dark particle trail
            this.level().addParticle(ParticleTypes.SMOKE,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                this.getY() + 1.0,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                0, 0.1, 0);
        }

        // Check if owner is still online
        if (!this.level().isClientSide && getOwner() == null && this.tickCount > 6000) { // 5 minutes
            // Owner logged off, shadow should dissipate
            this.discard();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT; // Spooky shadow sound
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    // ===== PERSISTENCE =====

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }

        tag.putFloat("InheritedHealth", this.inheritedHealth);
        tag.putFloat("InheritedDamage", this.inheritedDamage);
        tag.putFloat("InheritedSpeed", this.inheritedSpeed);

        if (this.sourceEntity != null) {
            tag.putString("SourceEntity", ForgeRegistries.ENTITY_TYPES.getKey(this.sourceEntity.getType()).toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }

        this.inheritedHealth = tag.getFloat("InheritedHealth");
        this.inheritedDamage = tag.getFloat("InheritedDamage");
        this.inheritedSpeed = tag.getFloat("InheritedSpeed");

        // Reapply inherited stats
        if (this.inheritedHealth > 0) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(inheritedHealth);
            this.setHealth(inheritedHealth);
        }
        if (this.inheritedDamage > 0) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(inheritedDamage);
        }
        if (this.inheritedSpeed > 0) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(inheritedSpeed);
        }
    }

    // ===== SPAWNING =====

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 35.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.ATTACK_DAMAGE, 3.0F)
            .add(Attributes.ARMOR, 2.0F)
            .add(Attributes.MAX_HEALTH, 20.0F);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false; // Shadow soldiers cannot be leashed
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Shadow Soldier");
    }
}