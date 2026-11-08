package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class QuartzApple extends Item {

    public QuartzApple() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(1)               // 饥饿值：1 (恢复半格鸡腿)
                        .saturationMod(1.0F)        // 饱和度修饰符：1.0
                        // 反胃 I, 30秒 (600 ticks), 100%概率
                        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 600, 0), 1.0F)
                        // 饥饿 I, 20秒 (400 ticks), 100%概率
                        .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400, 0), 1.0F)
                        .build())
        );
    }
}