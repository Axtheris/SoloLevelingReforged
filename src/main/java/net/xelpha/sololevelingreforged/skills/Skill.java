package net.xelpha.sololevelingreforged.skills;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Base class for all Solo Leveling skills
 * Skills have mana costs, cooldowns, levels, and can be active or passive
 */
public abstract class Skill {

    // Core skill properties
    protected final ResourceLocation id;
    protected final String name;
    protected final String description;
    protected final SkillType type;
    protected final int maxLevel;

    // Level-based properties (calculated per level)
    protected final int baseManaCost;
    protected final int baseCooldownTicks; // Minecraft ticks (20 = 1 second)
    protected final int unlockLevel;

    // Current state
    protected int currentLevel = 0;
    protected long lastUsedTime = 0;

    public Skill(ResourceLocation id, String name, String description, SkillType type,
                int maxLevel, int baseManaCost, int baseCooldownTicks, int unlockLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxLevel = maxLevel;
        this.baseManaCost = baseManaCost;
        this.baseCooldownTicks = baseCooldownTicks;
        this.unlockLevel = unlockLevel;
    }

    // ===== ABSTRACT METHODS =====

    /**
     * Activate the skill
     * @param player The player using the skill
     * @return true if activation was successful
     */
    public abstract boolean activate(ServerPlayer player);

    /**
     * Get the damage multiplier for this skill level
     */
    public abstract float getDamageMultiplier();

    /**
     * Get the effect duration in ticks for this skill level
     */
    public abstract int getEffectDuration();

    // ===== LEVEL CALCULATIONS =====

    /**
     * Get mana cost for current level
     */
    public int getManaCost() {
        return baseManaCost + (currentLevel - 1) * (baseManaCost / 4);
    }

    /**
     * Get cooldown in ticks for current level
     */
    public int getCooldownTicks() {
        return Math.max(20, baseCooldownTicks - (currentLevel - 1) * 10);
    }

    /**
     * Get cooldown in seconds for display
     */
    public float getCooldownSeconds() {
        return getCooldownTicks() / 20.0f;
    }

    /**
     * Check if skill is on cooldown
     */
    public boolean isOnCooldown() {
        if (lastUsedTime == 0) return false;
        return System.currentTimeMillis() - lastUsedTime < getCooldownTicks() * 50; // Convert ticks to ms
    }

    /**
     * Get remaining cooldown in ticks
     */
    public int getRemainingCooldownTicks() {
        if (!isOnCooldown()) return 0;
        long elapsedMs = System.currentTimeMillis() - lastUsedTime;
        long remainingMs = (getCooldownTicks() * 50) - elapsedMs;
        return (int) Math.max(0, remainingMs / 50);
    }

    /**
     * Get remaining cooldown as percentage (0.0 to 1.0)
     */
    public float getCooldownProgress() {
        if (!isOnCooldown()) return 1.0f;
        return 1.0f - (getRemainingCooldownTicks() / (float) getCooldownTicks());
    }

    // ===== LEVEL MANAGEMENT =====

    /**
     * Check if player can level up this skill
     */
    public boolean canLevelUp(Player player) {
        return currentLevel < maxLevel;
    }

    /**
     * Level up the skill
     */
    public boolean levelUp() {
        if (currentLevel < maxLevel) {
            currentLevel++;
            return true;
        }
        return false;
    }

    /**
     * Check if player meets unlock requirements using Solo Leveling level
     */
    public boolean canUnlock(net.xelpha.sololevelingreforged.core.PlayerCapability capability) {
        // Check Solo Leveling level instead of vanilla experience level
        return capability != null && capability.getLevel() >= unlockLevel;
    }

    // ===== ACTIVATION HELPERS =====

    /**
     * Common activation checks
     */
    protected boolean canActivate(ServerPlayer player) {
        if (isOnCooldown()) return false;
        if (getManaCost() > 0 && !player.getCapability(net.xelpha.sololevelingreforged.core.PlayerCapability.PLAYER_SYSTEM_CAP)
            .map(cap -> cap.consumeMana(getManaCost())).orElse(false)) {
            return false;
        }
        return true;
    }

    /**
     * Mark skill as used (update cooldown)
     */
    protected void markUsed() {
        lastUsedTime = System.currentTimeMillis();
    }

    // ===== GETTERS =====

    public ResourceLocation getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public int getMaxLevel() { return maxLevel; }
    public int getCurrentLevel() { return currentLevel; }
    public int getUnlockLevel() { return unlockLevel; }

    // ===== SERIALIZATION =====

    public static final Codec<Skill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(Skill::getId),
        Codec.STRING.fieldOf("name").forGetter(Skill::getName),
        Codec.STRING.fieldOf("description").forGetter(Skill::getDescription),
        SkillType.CODEC.fieldOf("type").forGetter(Skill::getType),
        Codec.INT.fieldOf("max_level").forGetter(Skill::getMaxLevel),
        Codec.INT.fieldOf("base_mana_cost").forGetter(skill -> skill.baseManaCost),
        Codec.INT.fieldOf("base_cooldown_ticks").forGetter(skill -> skill.baseCooldownTicks),
        Codec.INT.fieldOf("unlock_level").forGetter(Skill::getUnlockLevel),
        Codec.INT.fieldOf("current_level").forGetter(Skill::getCurrentLevel),
        Codec.LONG.fieldOf("last_used_time").forGetter(skill -> skill.lastUsedTime)
    ).apply(instance, Skill::fromCodec));

    protected static Skill fromCodec(ResourceLocation id, String name, String description, SkillType type,
                                   int maxLevel, int baseManaCost, int baseCooldownTicks, int unlockLevel,
                                   int currentLevel, long lastUsedTime) {
        // This will be overridden by subclasses
        return new Skill(id, name, description, type, maxLevel, baseManaCost, baseCooldownTicks, unlockLevel) {
            @Override
            public boolean activate(ServerPlayer player) { return false; }
            @Override
            public float getDamageMultiplier() { return 1.0f; }
            @Override
            public int getEffectDuration() { return 0; }
        };
    }
}