package net.xelpha.sololevelingreforged.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.ModEntities;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierAttackGoal;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierFollowGoal;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierGuardGoal;
import net.xelpha.sololevelingreforged.entity.ai.ShadowSoldierTargetGoal;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Shadow Soldier Entity - Loyal undead servants created through Shadow Extraction
 * Inherits abilities from the original monster but serves the Shadow Monarch
 */
public class ShadowSoldierEntity extends Monster {

    private UUID ownerUUID;
    private int despawnTimer = 24000; // 20 minutes default lifespan
    private String originalEntityType = "";
    private float inheritedHealth = 20.0f;
    private float inheritedDamage = 2.0f;

    // Shadow soldier state
    private boolean isBerserk = false;
    private int berserkTimer = 0;

    public ShadowSoldierEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired(); // Don't despawn naturally
    }

    /**
     * Create a shadow soldier from a defeated enemy
     */
    public static ShadowSoldierEntity createShadowSoldier(Player owner, LivingEntity originalEntity) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        // Create shadow soldier entity
        ShadowSoldierEntity shadowSoldier = new ShadowSoldierEntity(
            ModEntities.SHADOW_SOLDIER.get(), serverLevel);

        // Set position at original entity location
        shadowSoldier.setPos(originalEntity.getX(), originalEntity.getY(), originalEntity.getZ());

        // Initialize shadow soldier properties
        shadowSoldier.setOwner(owner);
        shadowSoldier.inheritFromOriginal(originalEntity);

        // Spawn the shadow soldier
        serverLevel.addFreshEntity(shadowSoldier);

        return shadowSoldier;
    }

    @Override
    protected void registerGoals() {
        // Initialize AI goals
        initializeGoals();
    }

    /**
     * Initialize AI goals for shadow soldier
     */
    private void initializeGoals() {
        // Clear existing goals
        this.goalSelector.removeAllGoals();
        this.targetSelector.removeAllGoals();

        // Combat and movement goals (lower priority numbers = higher priority)
        this.goalSelector.addGoal(1, new ShadowSoldierAttackGoal(this));      // Highest priority - attack when targeting
        this.goalSelector.addGoal(2, new ShadowSoldierFollowGoal(this));      // Follow owner when not attacking
        this.goalSelector.addGoal(3, new ShadowSoldierGuardGoal(this));       // Guard owner when nearby

        // Target selection goals
        this.targetSelector.addGoal(1, new ShadowSoldierTargetGoal(this));    // Find targets to attack
    }

    @Override
    public void tick() {
        super.tick();

        // Update despawn timer
        if (!this.level().isClientSide) {
            despawnTimer--;

            // Despawn after timer expires
            if (despawnTimer <= 0) {
                this.discard();
                return;
            }

            // Update berserk state
            if (isBerserk) {
                berserkTimer--;
                if (berserkTimer <= 0) {
                    setBerserk(false);
                }
            }
        }

        // Apply shadow visual effects
        if (this.level().isClientSide) {
            applyShadowEffects();
        }
    }

    /**
     * Apply shadow particle effects and visuals
     */
    private void applyShadowEffects() {
        // Add dark particles around shadow soldiers
        if (this.random.nextFloat() < 0.1f) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth();
            double y = this.getY() + this.random.nextDouble() * this.getBbHeight();
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth();

            // Add shadow particles (will be implemented with custom particles)
            // this.level().addParticle(ModParticles.SHADOW_PARTICLE.get(), x, y, z, 0, 0, 0);
        }
    }

    /**
     * Inherit properties from the original entity
     */
    public void inheritFromOriginal(LivingEntity originalEntity) {
        this.originalEntityType = originalEntity.getType().getDescriptionId();

        // Inherit health (scaled down to prevent overpowered shadows)
        float originalMaxHealth = originalEntity.getMaxHealth();
        this.inheritedHealth = Math.max(10.0f, Math.min(originalMaxHealth * 0.6f, 100.0f));

        // Inherit attack damage
        if (originalEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.inheritedDamage = (float) originalEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }

        // Set shadow soldier attributes
        updateShadowAttributes();

        // Set custom name to show it's a shadow
        this.setCustomName(Component.literal("§8Shadow " + originalEntity.getType().getDescription().getString()));
        this.setCustomNameVisible(true);
    }

    /**
     * Update shadow soldier attributes based on inherited properties
     */
    private void updateShadowAttributes() {
        // Set health
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.inheritedHealth);
            this.setHealth(this.inheritedHealth);
        }

        // Set attack damage
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            float shadowDamage = this.inheritedDamage * 0.8f; // Slightly less than original
            if (isBerserk) {
                shadowDamage *= 2.0f; // Double damage when berserk
            }
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(shadowDamage);
        }

        // Set movement speed (slightly faster than original)
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25f);
        }
    }


    /**
     * Set the owner of this shadow soldier
     */
    public void setOwner(Player owner) {
        this.ownerUUID = owner.getUUID();
    }

    /**
     * Get the owner of this shadow soldier
     */
    @Nullable
    public Player getOwner() {
        if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(this.ownerUUID);
        }
        return null;
    }

    /**
     * Activate berserk mode (from Berserk Shadows skill)
     */
    public void setBerserk(boolean berserk) {
        this.isBerserk = berserk;
        if (berserk) {
            this.berserkTimer = 600; // 30 seconds
            // Visual effects for berserk state
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                 SoundEvents.WITHER_AMBIENT, this.getSoundSource(), 1.0f, 0.5f);
        } else {
            this.berserkTimer = 0;
        }
        updateShadowAttributes(); // Update damage
    }

    public boolean isBerserk() {
        return isBerserk;
    }

    /**
     * Extend lifespan of shadow soldier
     */
    public void extendLifespan(int additionalTicks) {
        this.despawnTimer += additionalTicks;
    }

    /**
     * Get remaining lifespan in seconds
     */
    public float getRemainingLifespanSeconds() {
        return despawnTimer / 20.0f;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Shadow soldiers take reduced damage from light-based attacks
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING)) {
            amount *= 0.5f; // 50% resistance to lightning
        }

        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putInt("DespawnTimer", this.despawnTimer);
        compound.putString("OriginalEntityType", this.originalEntityType);
        compound.putFloat("InheritedHealth", this.inheritedHealth);
        compound.putFloat("InheritedDamage", this.inheritedDamage);
        compound.putBoolean("IsBerserk", this.isBerserk);
        compound.putInt("BerserkTimer", this.berserkTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        this.despawnTimer = compound.getInt("DespawnTimer");
        this.originalEntityType = compound.getString("OriginalEntityType");
        this.inheritedHealth = compound.getFloat("InheritedHealth");
        this.inheritedDamage = compound.getFloat("InheritedDamage");
        this.isBerserk = compound.getBoolean("IsBerserk");
        this.berserkTimer = compound.getInt("BerserkTimer");

        updateShadowAttributes();
    }

    /**
     * Create attribute supplier for shadow soldiers
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

}