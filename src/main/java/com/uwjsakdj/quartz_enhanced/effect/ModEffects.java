package com.uwjsakdj.quartz_enhanced.effect;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Quartz_Enhanced.MOD_ID);

    // 注册 "皮刺" 效果，颜色用冰蓝色
    public static final RegistryObject<MobEffect> PRICKLY = EFFECTS.register("prickly",
            () -> new PricklyEffect(MobEffectCategory.BENEFICIAL, 0xA5F2F3));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}