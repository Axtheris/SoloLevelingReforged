package net.xelpha.sololevelingreforged;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xelpha.sololevelingreforged.item.*;

public class ModItems {

    // Deferred register for items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Sololevelingreforged.MODID);

    // Custom Spirit Bow Item
    public static class SpiritBowItem extends BowItem {
        public SpiritBowItem(Properties properties) {
            super(properties);
        }
    }

    // ========================================
    // NAMED WEAPONS
    // ========================================

    // High-tier named weapons (A or S Rank)
    public static final RegistryObject<Item> KASAKAS_VENOM_FANG = ITEMS.register("kasakas_venom_fang",
            () -> new KasakasVenomFangItem(new Item.Properties()));

    public static final RegistryObject<Item> KNIGHT_KILLER = ITEMS.register("knight_killer",
            () -> new KnightKillerItem(new Item.Properties()));

    public static final RegistryObject<Item> BARUKAS_DAGGER = ITEMS.register("barukas_dagger",
            () -> new BarukasDaggerItem(new Item.Properties()));

    public static final RegistryObject<Item> DEMON_KING_DAGGERS = ITEMS.register("demon_king_daggers",
            () -> new DemonKingDaggersItem(new Item.Properties()));

    public static final RegistryObject<Item> DEMON_KING_LONGSWORD = ITEMS.register("demon_king_longsword",
            () -> new DemonKingLongswordItem(new Item.Properties()));

    public static final RegistryObject<Item> MOONLIGHT_REFRACTOR = ITEMS.register("moonlight_refractor",
            () -> new MoonlightRefractorItem(new Item.Properties()));

    public static final RegistryObject<Item> WAR_AXE = ITEMS.register("war_axe",
            () -> new AxeItem(ModToolTiers.A_RANK, 6.0F, -3.1F, new Item.Properties()));

    public static final RegistryObject<Item> GRAVITY_DAGGER = ITEMS.register("gravity_dagger",
            () -> new GravityDaggerItem(new Item.Properties()));

    public static final RegistryObject<Item> MYTHIC_DAGGER = ITEMS.register("mythic_dagger",
            () -> new MythicDaggerItem(new Item.Properties()));

    public static final RegistryObject<Item> FROST_BLADE = ITEMS.register("frost_blade",
            () -> new FrostBladeItem(new Item.Properties()));

    public static final RegistryObject<Item> KAMISH_DAGGERS = ITEMS.register("kamish_daggers",
            () -> new KamishDaggersItem(new Item.Properties()));

    public static final RegistryObject<Item> AWAKENED_VENOM_FANG = ITEMS.register("awakened_venom_fang",
            () -> new AwakenedVenomFangItem(new Item.Properties()));

    public static final RegistryObject<Item> SPIRIT_BOW = ITEMS.register("spirit_bow",
            () -> new SpiritBowItem(new Item.Properties().durability(384)));

    // ========================================
    // RANKED WEAPONS
    // ========================================

    // E Rank Weapons
    public static final RegistryObject<Item> E_RANK_SWORD = ITEMS.register("e_rank_sword",
            () -> new SwordItem(ModToolTiers.E_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> E_RANK_KATANA = ITEMS.register("e_rank_katana",
            () -> new SwordItem(ModToolTiers.E_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> E_RANK_DAGGER = ITEMS.register("e_rank_dagger",
            () -> new SwordItem(ModToolTiers.E_RANK, 2, -1.8F, new Item.Properties()));

    // D Rank Weapons
    public static final RegistryObject<Item> D_RANK_SWORD = ITEMS.register("d_rank_sword",
            () -> new SwordItem(ModToolTiers.D_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> D_RANK_KATANA = ITEMS.register("d_rank_katana",
            () -> new SwordItem(ModToolTiers.D_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> D_RANK_DAGGER = ITEMS.register("d_rank_dagger",
            () -> new SwordItem(ModToolTiers.D_RANK, 2, -1.8F, new Item.Properties()));

    // C Rank Weapons
    public static final RegistryObject<Item> C_RANK_SWORD = ITEMS.register("c_rank_sword",
            () -> new SwordItem(ModToolTiers.C_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> C_RANK_KATANA = ITEMS.register("c_rank_katana",
            () -> new SwordItem(ModToolTiers.C_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> C_RANK_DAGGER = ITEMS.register("c_rank_dagger",
            () -> new SwordItem(ModToolTiers.C_RANK, 2, -1.8F, new Item.Properties()));

    // B Rank Weapons
    public static final RegistryObject<Item> B_RANK_SWORD = ITEMS.register("b_rank_sword",
            () -> new SwordItem(ModToolTiers.B_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> B_RANK_KATANA = ITEMS.register("b_rank_katana",
            () -> new SwordItem(ModToolTiers.B_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> B_RANK_DAGGER = ITEMS.register("b_rank_dagger",
            () -> new SwordItem(ModToolTiers.B_RANK, 2, -1.8F, new Item.Properties()));

    // A Rank Weapons
    public static final RegistryObject<Item> A_RANK_SWORD = ITEMS.register("a_rank_sword",
            () -> new SwordItem(ModToolTiers.A_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> A_RANK_KATANA = ITEMS.register("a_rank_katana",
            () -> new SwordItem(ModToolTiers.A_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> A_RANK_DAGGER = ITEMS.register("a_rank_dagger",
            () -> new SwordItem(ModToolTiers.A_RANK, 2, -1.8F, new Item.Properties()));

    // S Rank Weapons
    public static final RegistryObject<Item> S_RANK_SWORD = ITEMS.register("s_rank_sword",
            () -> new SwordItem(ModToolTiers.S_RANK, 3, -2.4F, new Item.Properties()));

    public static final RegistryObject<Item> S_RANK_KATANA = ITEMS.register("s_rank_katana",
            () -> new SwordItem(ModToolTiers.S_RANK, 3, -2.2F, new Item.Properties()));

    public static final RegistryObject<Item> S_RANK_DAGGER = ITEMS.register("s_rank_dagger",
            () -> new SwordItem(ModToolTiers.S_RANK, 2, -1.8F, new Item.Properties()));
}