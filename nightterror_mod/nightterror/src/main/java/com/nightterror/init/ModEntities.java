package com.nightterror.init;

import com.nightterror.NightTerrorMod;
import com.nightterror.entity.NightStalkerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NightTerrorMod.MOD_ID);

    public static final RegistryObject<EntityType<NightStalkerEntity>> NIGHT_STALKER =
            ENTITIES.register("night_stalker",
                    () -> EntityType.Builder.<NightStalkerEntity>of(NightStalkerEntity::new, MobCategory.MONSTER)
                            .sized(1.2f, 2.8f)
                            .clientTrackingRange(80)
                            .build(new ResourceLocation(NightTerrorMod.MOD_ID, "night_stalker").toString()));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
