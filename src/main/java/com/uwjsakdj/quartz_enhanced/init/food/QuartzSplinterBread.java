package com.uwjsakdj.quartz_enhanced.init.food;

import com.uwjsakdj.quartz_enhanced.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuartzSplinterBread extends Item {

    public QuartzSplinterBread() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(5)               // 原版面包饱食度
                        .saturationMod(0.6F)        // 原版面包饱和度系数
                        // 1.0F 代表 100% 概率触发，1200 ticks = 60秒
                        .effect(() -> new MobEffectInstance(ModEffects.PRICKLY.get(), 1200, 0), 1.0F)
                        .build())
        );
    }

    // ─── Tooltip ───
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 显示皮刺效果（蓝色字）
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.quartz_splinter_bread.prickly")
                .withStyle(ChatFormatting.BLUE));
    }
}