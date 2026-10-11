package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced")
public class ToolDamageEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 仅处理玩家造成的近战伤害
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        // 判断是否为强化石英系列工具
        boolean isReinforcedTool = mainHandItem.getItem() instanceof ReinforcedQuartzSword
                || mainHandItem.getItem() instanceof ReinforcedQuartzShovel
                || mainHandItem.getItem() instanceof ReinforcedQuartzPickaxe
                || mainHandItem.getItem() instanceof ReinforcedQuartzHoe
                || mainHandItem.getItem() instanceof ReinforcedQuartzAxe
                || mainHandItem.getItem() instanceof ReinforcedQuartzKnife;

        if (!isReinforcedTool) return;

        // 耐久仅剩最后10点时，总伤害提升300%（×3倍）
        if (mainHandItem.getDamageValue() >= mainHandItem.getMaxDamage() - 10) {
            float originalDamage = event.getAmount();
            event.setAmount(originalDamage * 3.0F);
        }
    }
}