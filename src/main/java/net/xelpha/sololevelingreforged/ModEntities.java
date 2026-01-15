package net.xelpha.sololevelingreforged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;
import net.xelpha.sololevelingreforged.entity.ShadowDaggerEntity;

/**
 * Entity registry for the Solo Leveling mod
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Sololevelingreforged.MODID);

    public static final RegistryObject<EntityType<ShadowSoldierEntity>> SHADOW_SOLDIER = ENTITIES.register("shadow_soldier",
        () -> EntityType.Builder.<ShadowSoldierEntity>of((type, level) -> new ShadowSoldierEntity(type, level), MobCategory.MONSTER)
            .sized(0.6F, 1.9F) // Same size as zombie
            .clientTrackingRange(8)
            .updateInterval(2)
            .build("shadow_soldier")
    );

    public static final RegistryObject<EntityType<ShadowDaggerEntity>> SHADOW_DAGGER = ENTITIES.register("shadow_dagger",
        () -> EntityType.Builder.<ShadowDaggerEntity>of((type, level) -> new ShadowDaggerEntity(type, level), MobCategory.MISC)
            .sized(0.25F, 0.25F) // Small projectile size
            .clientTrackingRange(4)
            .updateInterval(1)
            .build("shadow_dagger")
    );
}