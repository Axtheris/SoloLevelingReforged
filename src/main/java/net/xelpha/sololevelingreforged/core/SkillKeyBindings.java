package net.xelpha.sololevelingreforged.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for Solo Leveling skills
 * Allows players to bind skills to hotkeys for quick activation in combat
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkillKeyBindings {

    // Primary skill hotkeys (1-5)
    public static final KeyMapping SKILL_1 = new KeyMapping(
        "key.sololevelingreforged.skill_1",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_1,
        "key.categories.sololevelingreforged"
    );

    public static final KeyMapping SKILL_2 = new KeyMapping(
        "key.sololevelingreforged.skill_2",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_2,
        "key.categories.sololevelingreforged"
    );

    public static final KeyMapping SKILL_3 = new KeyMapping(
        "key.sololevelingreforged.skill_3",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_3,
        "key.categories.sololevelingreforged"
    );

    public static final KeyMapping SKILL_4 = new KeyMapping(
        "key.sololevelingreforged.skill_4",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_4,
        "key.categories.sololevelingreforged"
    );

    public static final KeyMapping SKILL_5 = new KeyMapping(
        "key.sololevelingreforged.skill_5",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_5,
        "key.categories.sololevelingreforged"
    );

    // Quick skill access
    public static final KeyMapping QUICK_SHADOW_EXTRACT = new KeyMapping(
        "key.sololevelingreforged.quick_shadow_extract",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.sololevelingreforged"
    );

    public static final KeyMapping QUICK_DASH = new KeyMapping(
        "key.sololevelingreforged.quick_dash",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F,
        "key.categories.sololevelingreforged"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SKILL_1);
        event.register(SKILL_2);
        event.register(SKILL_3);
        event.register(SKILL_4);
        event.register(SKILL_5);
        event.register(QUICK_SHADOW_EXTRACT);
        event.register(QUICK_DASH);
    }

    /**
     * Get the skill ID associated with a key binding
     */
    public static String getSkillForKey(KeyMapping keyMapping) {
        if (keyMapping == QUICK_SHADOW_EXTRACT) {
            return "shadow_extraction";
        } else if (keyMapping == QUICK_DASH) {
            return "dash";
        } else if (keyMapping == SKILL_1) {
            return "shadow_extraction"; // Default skill 1
        } else if (keyMapping == SKILL_2) {
            return "berserk_shadows"; // Default skill 2
        } else if (keyMapping == SKILL_3) {
            return "dash"; // Default skill 3
        } else if (keyMapping == SKILL_4) {
            return "dagger_throw"; // Default skill 4
        } else if (keyMapping == SKILL_5) {
            return "shadow_prison"; // Default skill 5
        }
        return null;
    }
}