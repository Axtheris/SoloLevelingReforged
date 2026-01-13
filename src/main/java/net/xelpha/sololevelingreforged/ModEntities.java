package net.xelpha.sololevelingreforged;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Entity registrations for the Solo Leveling mod
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
        ForgeRegistries.ENTITY_TYPES, Sololevelingreforged.MODID);

    // Shadow Soldier Entity
    public static final RegistryObject<EntityType<ShadowSoldierEntity>> SHADOW_SOLDIER = ENTITIES.register(
        "shadow_soldier",
        () -> EntityType.Builder.<ShadowSoldierEntity>of(ShadowSoldierEntity::new, MobCategory.MONSTER)
                .sized(0.6F, 1.8F) // Standard player size
                .clientTrackingRange(8)
                .build(Sololevelingreforged.loc("shadow_soldier").toString())
    );
}