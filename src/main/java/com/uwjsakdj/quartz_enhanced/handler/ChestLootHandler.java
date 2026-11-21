package com.uwjsakdj.quartz_enhanced.handler;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChestLootHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();

        // 1. 远古城市：只加入混沌之刃 (2%)
        if (name.equals(BuiltInLootTables.ANCIENT_CITY)) {
            LootPool.Builder chaosPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    // 混沌之刃权重 1，空物品权重 49 -> 1/50 = 2%
                    .add(LootItem.lootTableItem(ModItems.CHAOS_BLADE.get()).setWeight(1))
                    .add(EmptyLootItem.emptyItem().setWeight(49));
            event.getTable().addPool(chaosPool.build());
        }

        // 2. 末地城：加入混沌之刃 (2%) 和 明月之刃 (2.3%)
        else if (name.equals(BuiltInLootTables.END_CITY_TREASURE)) {
            // 混沌之刃池子 (2%)
            LootPool.Builder chaosPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.CHAOS_BLADE.get()).setWeight(1))
                    .add(EmptyLootItem.emptyItem().setWeight(49));
            event.getTable().addPool(chaosPool.build());

            // 明月之刃池子 (2.3%) -> 权重 1 和 42 (1/43 ≈ 2.325%)
            LootPool.Builder moonlightPool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.MOONLIGHT_BLADE.get()).setWeight(1))
                    .add(EmptyLootItem.emptyItem().setWeight(42));
            event.getTable().addPool(moonlightPool.build());
        }
    }
}