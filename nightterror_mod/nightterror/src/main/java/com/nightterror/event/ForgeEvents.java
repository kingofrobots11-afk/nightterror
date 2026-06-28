package com.nightterror.event;

import com.nightterror.NightTerrorMod;
import com.nightterror.init.ModEntities;
import com.nightterror.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightTerrorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    private static boolean announcedNight = false;

    @SubscribeEvent
    public static void onWorldTick(net.minecraftforge.event.level.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverLevel && !event.level.isClientSide()) {
            long time = serverLevel.getDayTime() % 24000;

            // Announce nightfall once
            if (time == 13000 && !announcedNight) {
                announcedNight = true;
                serverLevel.players().forEach(player ->
                    player.sendSystemMessage(Component.literal(
                        "§5§l[Night Terror] §r§7The darkness stirs... something hunts tonight."
                    ))
                );
            }

            if (time == 23000) {
                announcedNight = false;
            }

            // Dawn warning
            if (time == 22500) {
                serverLevel.players().forEach(player ->
                    player.sendSystemMessage(Component.literal(
                        "§e§l[Night Terror] §r§7Dawn approaches... survive just a little longer."
                    ))
                );
            }
        }
    }

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (event.getEntity().getType() == ModEntities.NIGHT_STALKER.get()) {
            var entity = event.getEntity();
            var level = entity.level();

            if (!level.isClientSide()) {
                // Announce defeat to all players
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.players().forEach(player ->
                        player.sendSystemMessage(Component.literal(
                            "§a§l[Night Terror] §r§7The Night Stalker has been vanquished! Dawn breaks safely."
                        ))
                    );
                }

                // Drop 3-5 Soul Shards
                int count = 3 + entity.level().random.nextInt(3);
                ItemStack shards = new ItemStack(ModItems.SOUL_SHARD.get(), count);
                ItemEntity drop = new ItemEntity(level,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), shards);
                drop.setDeltaMovement(new Vec3(0, 0.3, 0));
                level.addFreshEntity(drop);
            }
        }
    }
}
