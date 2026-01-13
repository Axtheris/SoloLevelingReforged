package net.xelpha.sololevelingreforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    // Deferred register for creative mode tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sololevelingreforged.MODID);

    // Solo Leveling Weapons Tab
    public static final RegistryObject<CreativeModeTab> SOLO_LEVELING_WEAPONS_TAB = CREATIVE_MODE_TABS.register("solo_leveling_weapons",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.KASAKAS_VENOM_FANG.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.sololevelingreforged.solo_leveling_weapons"))
                    .displayItems((parameters, output) -> {
                        // Add all named weapons
                        output.accept(ModItems.KASAKAS_VENOM_FANG.get());
                        output.accept(ModItems.KNIGHT_KILLER.get());
                        output.accept(ModItems.BARUKAS_DAGGER.get());
                        output.accept(ModItems.DEMON_KING_DAGGERS.get());
                        output.accept(ModItems.DEMON_KING_LONGSWORD.get());
                        output.accept(ModItems.MOONLIGHT_REFRACTOR.get());
                        output.accept(ModItems.WAR_AXE.get());
                        output.accept(ModItems.GRAVITY_DAGGER.get());
                        output.accept(ModItems.MYTHIC_DAGGER.get());
                        output.accept(ModItems.FROST_BLADE.get());
                        output.accept(ModItems.KAMISH_DAGGERS.get());
                        output.accept(ModItems.AWAKENED_VENOM_FANG.get());
                        output.accept(ModItems.SPIRIT_BOW.get());

                        // Add all ranked weapons (E to S rank)
                        // E Rank
                        output.accept(ModItems.E_RANK_SWORD.get());
                        output.accept(ModItems.E_RANK_KATANA.get());
                        output.accept(ModItems.E_RANK_DAGGER.get());

                        // D Rank
                        output.accept(ModItems.D_RANK_SWORD.get());
                        output.accept(ModItems.D_RANK_KATANA.get());
                        output.accept(ModItems.D_RANK_DAGGER.get());

                        // C Rank
                        output.accept(ModItems.C_RANK_SWORD.get());
                        output.accept(ModItems.C_RANK_KATANA.get());
                        output.accept(ModItems.C_RANK_DAGGER.get());

                        // B Rank
                        output.accept(ModItems.B_RANK_SWORD.get());
                        output.accept(ModItems.B_RANK_KATANA.get());
                        output.accept(ModItems.B_RANK_DAGGER.get());

                        // A Rank
                        output.accept(ModItems.A_RANK_SWORD.get());
                        output.accept(ModItems.A_RANK_KATANA.get());
                        output.accept(ModItems.A_RANK_DAGGER.get());

                        // S Rank
                        output.accept(ModItems.S_RANK_SWORD.get());
                        output.accept(ModItems.S_RANK_KATANA.get());
                        output.accept(ModItems.S_RANK_DAGGER.get());
                    })
                    .build());
}