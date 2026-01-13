package net.xelpha.sololevelingreforged.core;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.skills.*;

import java.util.function.Supplier;

/**
 * Registry for all Solo Leveling skills
 */
public class SkillRegistry {

    public static final DeferredRegister<Skill> SKILLS = DeferredRegister.create(
        Sololevelingreforged.loc("skill"), Sololevelingreforged.MODID);

    // Active Skills
    public static final RegistryObject<Skill> SHADOW_EXTRACTION = SKILLS.register("shadow_extraction",
        () -> new ShadowExtractionSkill());

    public static final RegistryObject<Skill> BERSERK_SHADOWS = SKILLS.register("berserk_shadows",
        () -> new BerserkShadowsSkill());

    public static final RegistryObject<Skill> DASH = SKILLS.register("dash",
        () -> new DashSkill());

    public static final RegistryObject<Skill> DAGGER_THROW = SKILLS.register("dagger_throw",
        () -> new DaggerThrowSkill());

    public static final RegistryObject<Skill> SHADOW_PRISON = SKILLS.register("shadow_prison",
        () -> new ShadowPrisonSkill());

    // Passive Skills
    public static final RegistryObject<Skill> WILL_TO_RECOVER = SKILLS.register("will_to_recover",
        () -> new WillToRecoverSkill());

    public static final RegistryObject<Skill> DETOXIFICATION = SKILLS.register("detoxification",
        () -> new DetoxificationSkill());

    public static final RegistryObject<Skill> SHADOW_AFFINITY = SKILLS.register("shadow_affinity",
        () -> new ShadowAffinitySkill());

    public static final RegistryObject<Skill> PREDATOR = SKILLS.register("predator",
        () -> new PredatorSkill());

    // Custom registry for skills
    public static final Supplier<IForgeRegistry<Skill>> REGISTRY = SKILLS.makeRegistry(RegistryBuilder::new);

    /**
     * Get a skill by its ID
     */
    public static Skill getSkill(String skillId) {
        for (RegistryObject<Skill> skillEntry : SKILLS.getEntries()) {
            if (skillEntry.getId().getPath().equals(skillId)) {
                return skillEntry.get();
            }
        }
        return null;
    }

    /**
     * Get all registered skills
     */
    public static java.util.Collection<RegistryObject<Skill>> getAllSkills() {
        return SKILLS.getEntries();
    }
}