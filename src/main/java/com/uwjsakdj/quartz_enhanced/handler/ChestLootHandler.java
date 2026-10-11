package com.uwjsakdj.quartz_enhanced.handler;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChestLootHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();

        // ========== 照着这个格式往下加就行 ==========
        // if (id.equals(new ResourceLocation("minecraft", "chests/xxx"))) {
        //     addItem(event, 你的物品Supplier, 权重, 最小数量, 最大数量);
        // }
    }

    /**
     * 向战利品表追加一个物品池，不覆盖原版掉落
     */
    private static void addItem(LootTableLoadEvent event, Supplier<? extends Item> itemSupplier,
                                int weight, int minCount, int maxCount) {
        LootPool pool = LootPool.lootPool()
                .add(LootItem.lootTableItem(itemSupplier.get())
                        .setWeight(weight)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)))
                )
                .build();

        event.getTable().addPool(pool);
    }
}
