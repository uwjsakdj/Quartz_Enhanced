package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.ChaosBlade;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosBladeXpHandler {

    // 反射缓存字段，避免重复查找
    private static Field orbValueField = null;

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();
        int xp = orb.getValue();
        if (xp <= 0) return;

        boolean repairedAnything = false;

        // 遍历玩家背包
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ChaosBlade && stack.isDamaged() && stack.getEnchantmentLevel(Enchantments.MENDING) > 0) {

                // 1. 获取并暂存附魔
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                int level = enchantments.get(Enchantments.MENDING);

                // 2. 临时移除经验修补，防止原版底层再次修复
                enchantments.remove(Enchantments.MENDING);
                EnchantmentHelper.setEnchantments(enchantments, stack);

                // 3. 手动计算修复：3倍经验消耗 (1点经验修 2/3 点耐久)
                int damage = stack.getDamageValue();
                int canRepair = xp * 2 / 3; // 当前经验最多能修多少耐久
                if (canRepair > 0) {
                    int toRepair = Math.min(damage, canRepair); // 实际修复的耐久
                    int xpCost = (toRepair * 3 + 1) / 2; // 计算消耗的经验量（向上取整）

                    stack.setDamageValue(damage - toRepair);
                    xp -= xpCost;
                    repairedAnything = true;
                }

                // 4. 恢复经验修补附魔
                enchantments.put(Enchantments.MENDING, level);
                EnchantmentHelper.setEnchantments(enchantments, stack);

                if (xp <= 0) break;
            }
        }

        // 5. 如果发生了修复，通过反射将剩余的经验写回经验球
        if (repairedAnything) {
            try {
                if (orbValueField == null) {
                    // 字段名在官方映射（Mojang mappings）中为 "value"
                    orbValueField = ExperienceOrb.class.getDeclaredField("value");
                    orbValueField.setAccessible(true);
                }
                orbValueField.setInt(orb, Math.max(0, xp));
            } catch (Exception e) {
                // 如果反射失败（例如字段名改变），则直接取消事件并手动给玩家加经验
                event.setCanceled(true);
                player.giveExperiencePoints(xp);
            }
        }
    }
}