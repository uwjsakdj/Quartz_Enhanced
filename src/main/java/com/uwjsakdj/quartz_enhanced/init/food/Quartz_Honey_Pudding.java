package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Quartz_Honey_Pudding extends Item {

    // 饱食度 6 点
    private static final int NUTRITION = 6;
    // 饱和度系数：总饱和度 4.8 = 6 * 0.8
    private static final float SATURATION_MOD = 0.8F;
    // 生命恢复 I 持续 90 秒 = 1800 游戏刻
    private static final int REGEN_TICKS = 90 * 20;

    public Quartz_Honey_Pudding() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(NUTRITION)
                        .saturationMod(SATURATION_MOD)
                        .build())
                .stacksTo(1));
    }

    // 进食动画
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    // 进食时长：
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (!(entity instanceof Player player) || level.isClientSide) {
            return resultStack;
        }

        List<MobEffect> harmfulEffects = player.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> !effect.isBeneficial())
                .toList();
        if (!harmfulEffects.isEmpty()) {
            MobEffect randomEffect = harmfulEffects.get(player.getRandom().nextInt(harmfulEffects.size()));
            player.removeEffect(randomEffect);
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0));

        if (ModList.get().isLoaded("farmersdelight")) {
            applyComfortEffect(player);
        }

        if (player.isCreative()) {

            return stack;
        } else {

            if (resultStack.isEmpty()) {

                return new ItemStack(Items.BOWL);
            } else {

                if (!player.getInventory().add(new ItemStack(Items.BOWL))) {

                    player.drop(new ItemStack(Items.BOWL), false);
                }
                return resultStack;
            }
        }
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
                // 添加效果：舒适 I，持续 60秒 (1200 ticks)
                player.addEffect(new MobEffectInstance(comfortEffect, 1200, 0));
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
