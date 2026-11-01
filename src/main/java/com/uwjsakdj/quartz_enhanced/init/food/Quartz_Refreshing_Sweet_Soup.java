package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Quartz_Refreshing_Sweet_Soup extends Item {

    private static final FoodProperties SOUP_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 0), 1.0F)
            .build();

    public Quartz_Refreshing_Sweet_Soup() {
        super(new Item.Properties().stacksTo(1).food(SOUP_FOOD));
    }

    // ─── Tooltip ───
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 基础效果：速度与急迫
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.sweet_soup.speed")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.sweet_soup.dig_speed")
                .withStyle(ChatFormatting.BLUE));

        // 兼容农夫乐事的效果
        if (ModList.get().isLoaded("farmersdelight")) {
            tooltip.add(Component.translatable("tooltip.quartz_enhanced.sweet_soup.comfort")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            if (ModList.get().isLoaded("farmersdelight")) {
                applyComfortEffect(player);
            }
        }

        //优化点：合并创造模式与非创造模式的判断，参考原版蘑菇煲的极简逻辑
        return entity instanceof Player player && player.isCreative() ? resultStack : new ItemStack(Items.BOWL);
    }

    private void applyComfortEffect(Player player) {
        try {
            Class<?> modEffectsClass = Class.forName("vectorwing.farmersdelight.common.registry.ModEffects");
            java.lang.reflect.Field comfortField = modEffectsClass.getDeclaredField("COMFORT");
            comfortField.setAccessible(true);
            Object effectObj = comfortField.get(null);

            if (effectObj instanceof MobEffect comfortEffect) {
                player.addEffect(new MobEffectInstance(comfortEffect, 1800, 0));
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }
}