package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.MoonlightBlade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MoonlightBladeEvents {

    private static class TargetInfo {
        final Player attacker;
        int ticks;
        TargetInfo(Player attacker, int ticks) {
            this.attacker = attacker;
            this.ticks = ticks;
        }
    }

    private static final Map<UUID, TargetInfo> MARKED_TARGETS = new HashMap<>();
    private static final UUID MOONLIGHT_SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    // ==================== 环境伤害与攻速加成 ====================
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof MoonlightBlade) {
            float multiplier = getEnvironmentMultiplier(player.level());
            if (multiplier > 1.0F) {
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

    // 👈 修改：加入夜视逻辑和攻速加成
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        Player player = event.player;
        Level level = player.level();

        boolean holdingBlade = player.getMainHandItem().getItem() instanceof MoonlightBlade || player.getOffhandItem().getItem() instanceof MoonlightBlade;

        // 1. 夜视逻辑：主世界夜晚 或 末地
        boolean isNight = !level.isDay();
        if (holdingBlade) {
            if (level.dimension().equals(Level.END) || (level.dimension().equals(Level.OVERWORLD) && isNight)) {
                // 给予夜视，持续 300 ticks（15秒），每 tick 刷新，离开条件后 15 秒消失
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
            }
        }

        // 2. 攻速加成逻辑
        AttributeInstance speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr == null) return;

        float multiplier = (holdingBlade && player.getMainHandItem().getItem() instanceof MoonlightBlade) ? getEnvironmentMultiplier(level) : 1.0F;

        if (speedAttr.getModifier(MOONLIGHT_SPEED_UUID) != null) {
            speedAttr.removeModifier(MOONLIGHT_SPEED_UUID);
        }

        if (multiplier > 1.0F) {
            double bonus = multiplier - 1.0F;
            speedAttr.addTransientModifier(new AttributeModifier(MOONLIGHT_SPEED_UUID, "moonlight_speed", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static float getEnvironmentMultiplier(Level level) {
        if (level.dimension().equals(Level.NETHER)) return 1.0F;
        if (level.dimension().equals(Level.END)) return 1.2F;

        boolean isNight = !level.isDay();
        if (!isNight) return 1.0F;

        boolean isFullMoon = level.getMoonPhase() == 0;
        boolean isRaining = level.isRaining();

        if (isFullMoon) return 1.2F;
        else if (isRaining) return 1.05F;
        else return 1.1F;
    }

    // ==================== 3秒延迟结算机制 ====================
    public static void addMarkedTarget(LivingEntity target, Player attacker) {
        MARKED_TARGETS.put(target.getUUID(), new TargetInfo(attacker, 60));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, TargetInfo>> iterator = MARKED_TARGETS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TargetInfo> entry = iterator.next();
            UUID uuid = entry.getKey();
            TargetInfo info = entry.getValue();

            LivingEntity target = null;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                var entity = level.getEntity(uuid);
                if (entity instanceof LivingEntity living) {
                    target = living;
                    break;
                }
            }

            if (target == null || !target.isAlive() || info.attacker == null) {
                iterator.remove();
                continue;
            }

            if (target.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + target.getBbHeight() + 0.5, target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
                serverLevel.sendParticles(ParticleTypes.FIREWORK, target.getX(), target.getY() + target.getBbHeight() + 0.5, target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            }

            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 0, false, false));

            if (info.ticks <= 0) {
                float damage = target.getMaxHealth() * 0.5F + 5.0F;
                target.hurt(target.damageSources().playerAttack(info.attacker), damage);

                if (target.getHealth() > 0 && !target.isDeadOrDying() && target.getHealth() > target.getMaxHealth() - damage + 1) {
                    target.setHealth(Math.max(0, target.getHealth() - damage));
                }

                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));

                iterator.remove();
            } else {
                info.ticks--;
            }
        }
    }
}