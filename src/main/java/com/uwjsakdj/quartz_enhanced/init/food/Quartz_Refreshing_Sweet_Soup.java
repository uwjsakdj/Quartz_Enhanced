package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {

            if (ModList.get().isLoaded("farmersdelight")) {
                applyComfortEffect(player);
            }

            if (player.getAbilities().instabuild) {
                return stack;
            }
        }

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            return new ItemStack(Items.BOWL);
        }

        // 如果是非玩家实体或其他情况，返回 super 的结果
        return resultStack;
    }

    private void applyComfortEffect(Player player) {
        try {
            // 农夫乐事 1.20.1 的效果注册类通常是 vectorwing.farmersdelight.common.registry.ModEffects
            Class<?> modEffectsClass = Class.forName("vectorwing.farmersdelight.common.registry.ModEffects");

            java.lang.reflect.Field comfortField = modEffectsClass.getDeclaredField("COMFORT");
            comfortField.setAccessible(true);
            Object effectObj = comfortField.get(null);

            if (effectObj instanceof MobEffect) {
                MobEffect comfortEffect = (MobEffect) effectObj;

                player.addEffect(new MobEffectInstance(comfortEffect, 1800, 0));
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
