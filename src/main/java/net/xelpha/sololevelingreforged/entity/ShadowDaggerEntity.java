package net.xelpha.sololevelingreforged.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.xelpha.sololevelingreforged.ModEntities;

import javax.annotation.Nullable;

/**
 * Shadow Dagger projectile entity with homing capabilities
 */
public class ShadowDaggerEntity extends Projectile {

    private LivingEntity target;
    private ServerPlayer owner;
    private int homingTicks = 0;
    private static final int MAX_HOMING_TICKS = 100; // 5 seconds max homing
    private static final float HOMING_STRENGTH = 0.15f;
    private static final float BASE_DAMAGE = 8.0f;

    public ShadowDaggerEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public ShadowDaggerEntity(Level level, ServerPlayer owner) {
        super(ModEntities.SHADOW_DAGGER.get(), level);
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData() {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();

        // Visual effects
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE,
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0);
        }

        // Homing logic (server side only)
        if (!this.level().isClientSide && homingTicks < MAX_HOMING_TICKS) {
            updateHoming();
            homingTicks++;
        }

        // Check for expiration
        if (this.tickCount > 200) { // 10 seconds max lifetime
            this.discard();
        }
    }

    private void updateHoming() {
        // Find target if we don't have one
        if (target == null || !target.isAlive()) {
            findNewTarget();
        }

        // Apply homing if we have a target
        if (target != null && target.isAlive()) {
            Vec3 currentPos = this.position();
            Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0); // Aim for center mass
            Vec3 directionToTarget = targetPos.subtract(currentPos).normalize();

            // Get current velocity
            Vec3 currentVelocity = this.getDeltaMovement();

            // Apply homing force
            Vec3 homingForce = directionToTarget.scale(HOMING_STRENGTH);
            Vec3 newVelocity = currentVelocity.add(homingForce).normalize().scale(currentVelocity.length());

            // Apply the new velocity
            this.setDeltaMovement(newVelocity);
        }
    }

    private void findNewTarget() {
        // Find the closest valid enemy
        double closestDistance = 16.0D; // Max homing range
        LivingEntity closestTarget = null;

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
            this.getBoundingBox().inflate(16.0D))) {

            if (isValidTarget(entity)) {
                double distance = this.distanceTo(entity);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTarget = entity;
                }
            }
        }

        this.target = closestTarget;
    }

    private boolean isValidTarget(LivingEntity entity) {
        // Don't target owner or other players (for now)
        if (entity == owner) return false;
        if (entity instanceof Player) return false;

        // Only target monsters/hostile entities
        return entity.isAlive() && entity.getType().getCategory() != net.minecraft.world.entity.MobCategory.CREATURE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity hitEntity = result.getEntity();

            // Calculate damage based on owner's skill level
            float finalDamage = BASE_DAMAGE;
            if (owner != null) {
                var capOpt = owner.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP);
                if (capOpt.isPresent()) {
                    var cap = capOpt.orElse(null);
                    if (cap != null) {
                        net.xelpha.sololevelingreforged.skills.Skill daggerSkill =
                            cap.getSkill(net.xelpha.sololevelingreforged.Sololevelingreforged.loc("dagger_throw"));
                        if (daggerSkill != null) {
                            finalDamage *= daggerSkill.getDamageMultiplier();
                        }
                    }
                }
            }

            // Deal damage
            hitEntity.hurt(this.damageSources().generic(), finalDamage);

            // Play hit sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, this.getSoundSource(), 1.0F, 1.0F);

            // Visual effects
            this.level().addParticle(ParticleTypes.CRIT,
                hitEntity.getX(), hitEntity.getY() + hitEntity.getBbHeight() / 2, hitEntity.getZ(),
                0.2, 0.2, 0.2);

            // Remove the dagger
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            // Play hit sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, this.getSoundSource(), 0.8F, 1.0F);

            // Remove the dagger
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() {
        return true; // Daggers don't fall
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("Owner", owner.getUUID());
        }
        tag.putInt("HomingTicks", homingTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            // Note: Owner will be null on client side, but that's okay
            // Server side will handle owner lookup
        }
        homingTicks = tag.getInt("HomingTicks");
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024.0D; // Render up to 32 blocks away
    }
}