package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.food.Quartz_Refreshing_Sweet_Soup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced")
public class CraftingReturnHandler {

    @SubscribeEvent
    public static void onItemCrafted(ItemCraftedEvent event) {
        // 仅服务端执行，避免客户端重复触发
        if (event.getEntity().level().isClientSide) return;

        // 仅处理石英清润甜汤的合成返还逻辑
        if (!(event.getCrafting().getItem() instanceof Quartz_Refreshing_Sweet_Soup)) {
            return;
        }

        // 遍历合成网格，检测是否使用了水瓶作为原料
        boolean usedWaterBottle = false;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack inputStack = event.getInventory().getItem(i);
            // 标准药水判定：比对药水类型，比直接读NBT更稳定兼容
            if (PotionUtils.getPotion(inputStack) == Potions.WATER) {
                usedWaterBottle = true;
                break;
            }
        }

        // 仅水瓶合成时返还空瓶；水桶/其他原料合成不返还任何物品
        if (usedWaterBottle) {
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            // 优先放入玩家背包，背包满则掉落在脚边
            if (!event.getEntity().getInventory().add(emptyBottle)) {
                event.getEntity().drop(emptyBottle, false);
            }
        }
    }
}