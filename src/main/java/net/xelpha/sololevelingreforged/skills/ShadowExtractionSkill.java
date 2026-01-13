package net.xelpha.sololevelingreforged.skills;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.xelpha.sololevelingreforged.core.PlayerCapability;
import net.xelpha.sololevelingreforged.core.Skill;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Shadow Extraction - Converts defeated enemies into shadow soldiers
 * Core ability that defines the Necromancer class
 */
public class ShadowExtractionSkill extends Skill {

    // Skill constants
    private static final float BASE_MANA_COST = 50.0f;
    private static final int BASE_COOLDOWN = 60; // 3 seconds
    private static final int MAX_LEVEL = 10;

    // Extraction range increases with level
    private static final double BASE_EXTRACTION_RANGE = 10.0;
    private static final double RANGE_PER_LEVEL = 2.0;

    public ShadowExtractionSkill() {
        super("shadow_extraction", "Shadow Extraction", SkillType.ACTIVE, MAX_LEVEL,
              BASE_MANA_COST, BASE_COOLDOWN,
              "Convert defeated enemies into loyal shadow soldiers that fight by your side.");
    }

    @Override
    protected void executeSkill(Player player) {
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return;

        double extractionRange = getExtractionRange();
        int maxExtractions = getMaxExtractions();

        // Find nearby defeated enemies and extract shadows
        // This will be enhanced when we implement the shadow soldier entity
        extractShadowsFromNearbyCorpses(player, extractionRange, maxExtractions);

        // Send system message
        cap.setSystemMessage("Shadow Extraction activated! Range: " + String.format("%.1f", extractionRange) +
                           " blocks, Max shadows: " + maxExtractions);
    }

    @Override
    protected boolean canActivate(Player player) {
        // Check if player is in combat or has recent kills
        // For now, always allow activation (will be enhanced later)
        return true;
    }

    /**
     * Extract shadows from nearby defeated enemies
     */
    private void extractShadowsFromNearbyCorpses(Player player, double range, int maxExtractions) {
        // Get all living entities in range
        var entities = player.level().getEntities(player, player.getBoundingBox().inflate(range),
            entity -> entity instanceof LivingEntity && !entity.isAlive() && entity != player);

        int extracted = 0;
        for (var entity : entities) {
            if (extracted >= maxExtractions) break;

            LivingEntity livingEntity = (LivingEntity) entity;
            if (canExtractFromEntity(livingEntity)) {
                extractShadowFromEntity(player, livingEntity);
                extracted++;
            }
        }

        if (extracted == 0) {
            PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
            if (cap != null) {
                cap.setSystemMessage("No suitable corpses found in range for shadow extraction.");
            }
        }
    }

    /**
     * Check if an entity can be extracted as a shadow
     */
    private boolean canExtractFromEntity(LivingEntity entity) {
        // For now, allow extraction from most hostile mobs
        // This will be expanded with more sophisticated logic
        String entityType = entity.getType().getDescriptionId().toLowerCase();

        // Allow most hostile mobs, exclude certain entities
        if (entityType.contains("player") || entityType.contains("villager") ||
            entityType.contains("iron_golem") || entityType.contains("snow_golem")) {
            return false;
        }

        return true;
    }

    /**
     * Extract shadow from a specific entity
     */
    private void extractShadowFromEntity(Player player, LivingEntity entity) {
        // Create shadow soldier at entity location
        // This will spawn the actual ShadowSoldierEntity when implemented
        ShadowSoldierEntity.createShadowSoldier(player, entity);

        // Play extraction effect/sound
        playExtractionEffects(player, entity);

        // Award experience for successful extraction
        awardExtractionExperience(player, entity);
    }

    /**
     * Play visual and audio effects for shadow extraction
     */
    private void playExtractionEffects(Player player, LivingEntity entity) {
        // Add particle effects and sounds here
        // For now, just log the extraction
        System.out.println("Extracted shadow from " + entity.getType().getDescriptionId() +
                          " at " + entity.position());
    }

    /**
     * Award bonus experience for shadow extraction
     */
    private void awardExtractionExperience(Player player, LivingEntity entity) {
        PlayerCapability cap = player.getCapability(PlayerCapability.PLAYER_SYSTEM_CAP).orElse(null);
        if (cap == null) return;

        // Base extraction XP scales with entity max health
        int baseXP = Math.max(1, (int)(entity.getMaxHealth() * 0.5f));

        // Bonus XP based on skill level
        float levelBonus = getEffectivenessMultiplier();
        int totalXP = (int)(baseXP * levelBonus);

        cap.addExperience(totalXP);
    }

    /**
     * Get extraction range based on skill level
     */
    public double getExtractionRange() {
        return BASE_EXTRACTION_RANGE + (currentLevel - 1) * RANGE_PER_LEVEL;
    }

    /**
     * Get maximum number of shadows that can be extracted at once
     */
    public int getMaxExtractions() {
        return Math.min(5 + currentLevel, 20); // 5 at level 1, up to 20 at level 15+
    }

    /**
     * Get current shadow army size limit
     */
    public int getMaxShadowArmySize() {
        return 100 + (currentLevel * 50); // 100 at level 1, 150 at level 2, etc.
    }

    @Override
    public float getManaCost() {
        // Mana cost increases with level due to more powerful extractions
        return super.getManaCost() * (1.0f + (currentLevel - 1) * 0.1f);
    }
}