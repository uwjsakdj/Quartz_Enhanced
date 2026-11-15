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

        // 判断当前加载的是否是 远古城市(Ancient City) 或 末地城(End City) 的战利品箱
        if (name.equals(BuiltInLootTables.ANCIENT_CITY) || 
            name.equals(BuiltInLootTables.END_CITY_TREASURE)) {
            
            // 构建一个新的战利品池
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    // 每个箱子只抽取1次
                    .setRolls(ConstantValue.exactly(1))
                    // 放入混沌之刃，权重为 1
                    .add(LootItem.lootTableItem(ModItems.CHAOS_BLADE.get())
                            .setWeight(1))
                    // 放入“空”，权重为 49。这意味着 1 / 50 的概率出刀（2% 的低概率）
                    .add(EmptyLootItem.emptyItem().setWeight(49));

            // 将这个池子加入到原有的战利品表中
            event.getTable().addPool(poolBuilder.build());
        }
    }
}