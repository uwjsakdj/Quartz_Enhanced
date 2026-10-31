package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.effect.ModEffects;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PricklyEvents {

    /** 基础反伤倍率（I 级） */
    private static final float BASE_MULTIPLIER = 1.5F;
    /** 每提升一级额外增加的倍率 */
    private static final float MULTIPLIER_PER_LEVEL = 0.2F;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        // 1. 一次性取出 "皮刺" 效果实例，顺便判空
        MobEffectInstance prickly = victim.getEffect(ModEffects.PRICKLY.get());
        if (prickly == null) return;

        // 2. 防止死循环：荆棘反伤自身不再触发皮刺
        if (event.getSource().is(DamageTypes.THORNS)) return;

        // 3. 获取攻击者，排除环境伤害
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        // 4. 仅限近战：投掷物直接来源不触发
        if (event.getSource().getDirectEntity() instanceof Projectile) return;

        // 5. 计算倍率：amplifier 0 = I级 -> 1.5；amplifier 1 = II级 -> 1.7 ...
        int amplifier = prickly.getAmplifier();
        float multiplier = BASE_MULTIPLIER + MULTIPLIER_PER_LEVEL * amplifier;

        // 6. 对攻击者造成反伤，使用荆棘伤害源（不会二次触发皮刺）
        float reflectDamage = event.getAmount() * multiplier;
        livingAttacker.hurt(livingAttacker.damageSources().thorns(victim), reflectDamage);
    }
}