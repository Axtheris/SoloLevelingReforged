package net.xelpha.sololevelingreforged.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xelpha.sololevelingreforged.Sololevelingreforged;
import net.xelpha.sololevelingreforged.entity.ShadowSoldierEntity;

/**
 * Entity attribute creation event handler
 */
@Mod.EventBusSubscriber(modid = Sololevelingreforged.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeEvents {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(net.xelpha.sololevelingreforged.ModEntities.SHADOW_SOLDIER.get(),
            ShadowSoldierEntity.createAttributes().build());
    }
}