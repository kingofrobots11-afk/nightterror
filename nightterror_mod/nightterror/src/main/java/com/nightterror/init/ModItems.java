package com.nightterror.init;

import com.nightterror.NightTerrorMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NightTerrorMod.MOD_ID);

    public static final RegistryObject<Item> SOUL_SHARD =
            ITEMS.register("soul_shard",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> NIGHT_STALKER_SPAWN_EGG =
            ITEMS.register("night_stalker_spawn_egg",
                    () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                            ModEntities.NIGHT_STALKER, 0x0a0a1a, 0x6600cc,
                            new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
