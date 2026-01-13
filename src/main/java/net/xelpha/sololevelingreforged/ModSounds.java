package net.xelpha.sololevelingreforged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Sound event registry for the Solo Leveling Reforged mod
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Sololevelingreforged.MODID);

    // Interface sounds
    public static final RegistryObject<SoundEvent> INTERFACE_OPEN = registerSoundEvent("interface_open");
    public static final RegistryObject<SoundEvent> UI_CLICK = registerSoundEvent("click");

    /**
     * Helper method to register a sound event
     */
    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(Sololevelingreforged.MODID, name)));
    }
}