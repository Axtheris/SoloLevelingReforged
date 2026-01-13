package net.xelphene.sololevelingreforged.core;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Base class for all Solo Leveling System skills
 * Handles mana cost, cooldown, leveling, and activation logic
 */
public abstract class Skill extends ForgeRegistryEntry<Skill> {

    protected final String skillId;
    protected final String displayName;
    protected final SkillType skillType;
    protected final int maxLevel;

    // Base stats (level 1)
    protected final float baseManaCost;
    protected final int baseCooldown; // in ticks
    protected final String description;

    // Current state
    protected int currentLevel = 1;
    protected int currentCooldown = 0; // remaining cooldown in ticks

    public Skill(String skillId, String displayName, SkillType skillType, int maxLevel,
                float baseManaCost, int baseCooldown, String description) {
        this.skillId = skillId;
        this.displayName = displayName;
        this.skillType = skillType;
        this.maxLevel = maxLevel;
        this.baseManaCost = baseManaCost;
        this.baseCooldown = baseCooldown;
        this.description = description;
    }

    /**
     * Attempt to activate this skill
     * @param player The player attempting to use the skill
     * @return true if activation was successful
     */
    public boolean activate(Player player) {
        // Check if skill is on cooldown
        if (isOnCooldown()) {
            return false;
        }

        // Check mana cost
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return false;

        float manaCost = getManaCost();
        if (!cap.consumeMana(manaCost)) {
            return false;
        }

        // Check activation conditions
        if (!canActivate(player)) {
            // Refund mana if conditions not met
            cap.regenerateMana(manaCost);
            return false;
        }

        // Start cooldown
        currentCooldown = getCooldown();

        // Execute skill logic
        executeSkill(player);

        return true;
    }

    /**
     * Execute the skill's specific logic
     * @param player The player using the skill
     */
    protected abstract void executeSkill(Player player);

    /**
     * Check if the skill can be activated
     * @param player The player attempting to use the skill
     * @return true if activation conditions are met
     */
    protected abstract boolean canActivate(Player player);

    /**
     * Update cooldown each tick
     */
    public void tickCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    /**
     * Level up the skill (usage-based progression)
     */
    public void levelUp() {
        if (currentLevel < maxLevel) {
            currentLevel++;
        }
    }

    /**
     * Check if skill can be leveled up through skill points
     * @param player The player attempting to upgrade
     * @return true if upgrade is possible
     */
    public boolean canUpgradeWithPoints(Player player) {
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        return cap != null && cap.getAvailableAP() > 0 && currentLevel < maxLevel;
    }

    /**
     * Upgrade skill using skill points
     * @param player The player upgrading the skill
     * @return true if upgrade was successful
     */
    public boolean upgradeWithPoints(Player player) {
        if (!canUpgradeWithPoints(player)) {
            return false;
        }

        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return false;

        // Spend skill point
        cap.addAvailableAP(-1);
        levelUp();

        return true;
    }

    // ===== GETTERS =====

    public String getSkillId() { return skillId; }
    public String getDisplayName() { return displayName; }
    public SkillType getSkillType() { return skillType; }
    public int getMaxLevel() { return maxLevel; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isOnCooldown() { return currentCooldown > 0; }
    public int getRemainingCooldownTicks() { return currentCooldown; }
    public float getRemainingCooldownSeconds() { return currentCooldown / 20.0f; }
    public String getDescription() { return description; }

    /**
     * Get mana cost scaled by level
     */
    public float getManaCost() {
        // Mana cost decreases slightly with level
        float costReduction = (currentLevel - 1) * 0.05f; // 5% reduction per level
        return Math.max(baseManaCost * (1.0f - costReduction), baseManaCost * 0.5f);
    }

    /**
     * Get cooldown scaled by level
     */
    public int getCooldown() {
        // Cooldown decreases with level
        float cooldownReduction = (currentLevel - 1) * 0.1f; // 10% reduction per level
        return Math.max((int)(baseCooldown * (1.0f - cooldownReduction)), baseCooldown / 2);
    }

    /**
     * Get cooldown in seconds for display
     */
    public float getCooldownSeconds() {
        return baseCooldown / 20.0f;
    }

    /**
     * Get skill effectiveness multiplier (damage, duration, etc.)
     */
    public float getEffectivenessMultiplier() {
        return 1.0f + (currentLevel - 1) * 0.15f; // 15% increase per level
    }

    public enum SkillType {
        ACTIVE,    // Requires activation, costs mana
        PASSIVE    // Always active, provides continuous effects
    }

    @Override
    public String toString() {
        return String.format("%s (Level %d/%d)", displayName, currentLevel, maxLevel);
    }
}