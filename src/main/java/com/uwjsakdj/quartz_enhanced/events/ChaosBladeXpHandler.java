package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.ChaosBlade;
import com.uwjsakdj.quartz_enhanced.init.tool.MoonlightBlade;
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

    private static Field orbValueField = null;

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();
        int xp = orb.getValue();
        if (xp <= 0) return;

        boolean repairedAnything = false;

        for (ItemStack stack : player.getInventory().items) {
            boolean isChaos = stack.getItem() instanceof ChaosBlade;
            boolean isMoonlight = stack.getItem() instanceof MoonlightBlade;

            if ((isChaos || isMoonlight) && stack.isDamaged() && stack.getEnchantmentLevel(Enchantments.MENDING) > 0) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                int level = enchantments.get(Enchantments.MENDING);
                enchantments.remove(Enchantments.MENDING);
                EnchantmentHelper.setEnchantments(enchantments, stack);

                int damage = stack.getDamageValue();
                int canRepair;
                int xpCost;

                if (isChaos) {
                    // 混沌之刃：3倍经验消耗
                    canRepair = xp * 2 / 3;
                    if (canRepair > 0) {
                        int toRepair = Math.min(damage, canRepair);
                        xpCost = (toRepair * 3 + 1) / 2;
                        stack.setDamageValue(damage - toRepair);
                        xp -= xpCost;
                        repairedAnything = true;
                    }
                } else {
                    // 明月之刃：2倍经验消耗
                    canRepair = xp / 2; // 1点经验修1点耐久（原版是1点经验修2点）
                    if (canRepair > 0) {
                        int toRepair = Math.min(damage, canRepair);
                        xpCost = toRepair * 2;
                        stack.setDamageValue(damage - toRepair);
                        xp -= xpCost;
                        repairedAnything = true;
                    }
                }

                enchantments.put(Enchantments.MENDING, level);
                EnchantmentHelper.setEnchantments(enchantments, stack);
                if (xp <= 0) break;
            }
        }

        if (repairedAnything) {
            try {
                if (orbValueField == null) {
                    orbValueField = ExperienceOrb.class.getDeclaredField("value");
                    orbValueField.setAccessible(true);
                }
                orbValueField.setInt(orb, Math.max(0, xp));
            } catch (Exception e) {
                event.setCanceled(true);
                player.giveExperiencePoints(xp);
            }
        }
    }
}