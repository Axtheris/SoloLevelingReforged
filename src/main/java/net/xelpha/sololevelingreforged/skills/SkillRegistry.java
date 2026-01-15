package net.xelpha.sololevelingreforged.skills;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.xelpha.sololevelingreforged.Sololevelingreforged;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Modern skill registry using Codec-based registration
 * Avoids deprecated ForgeRegistryEntry patterns
 */
public class SkillRegistry {

    private static final Map<ResourceLocation, Supplier<Skill>> SKILL_SUPPLIERS = new HashMap<>();
    private static final Map<ResourceLocation, Skill> REGISTERED_SKILLS = new HashMap<>();

    // Register all skills during mod initialization
    public static void registerSkills() {
        // Active Skills
        register("shadow_extraction", () -> new ShadowExtractionSkill());
        register("berserk_shadows", () -> new BerserkShadowsSkill());
        register("dash", () -> new DashSkill());
        register("dagger_throw", () -> new DaggerThrowSkill());
        register("shadow_prison", () -> new ShadowPrisonSkill());

        // Passive Skills
        register("will_to_recover", () -> new WillToRecoverSkill());
        register("detoxification", () -> new DetoxificationSkill());
        register("shadow_affinity", () -> new ShadowAffinitySkill());
        register("predator", () -> new PredatorSkill());

        // Create instances
        for (Map.Entry<ResourceLocation, Supplier<Skill>> entry : SKILL_SUPPLIERS.entrySet()) {
            REGISTERED_SKILLS.put(entry.getKey(), entry.getValue().get());
        }
    }

    /**
     * Register a skill supplier
     */
    public static void register(String id, Supplier<Skill> skillSupplier) {
        ResourceLocation key = Sololevelingreforged.loc(id);
        SKILL_SUPPLIERS.put(key, skillSupplier);
    }

    /**
     * Get a skill by ID
     */
    public static Skill getSkill(ResourceLocation id) {
        return REGISTERED_SKILLS.get(id);
    }

    /**
     * Get all registered skills
     */
    public static Map<ResourceLocation, Skill> getAllSkills() {
        return ImmutableMap.copyOf(REGISTERED_SKILLS);
    }

    /**
     * Get skills by type
     */
    public static Map<ResourceLocation, Skill> getSkillsByType(SkillType type) {
        Map<ResourceLocation, Skill> filtered = new HashMap<>();
        for (Map.Entry<ResourceLocation, Skill> entry : REGISTERED_SKILLS.entrySet()) {
            if (entry.getValue().getType() == type) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * Check if a skill is registered
     */
    public static boolean isRegistered(ResourceLocation id) {
        return REGISTERED_SKILLS.containsKey(id);
    }

    /**
     * Codec for serializing skill IDs
     */
    public static final Codec<Skill> SKILL_CODEC = ResourceLocation.CODEC.xmap(
        SkillRegistry::getSkill,
        Skill::getId
    );
}