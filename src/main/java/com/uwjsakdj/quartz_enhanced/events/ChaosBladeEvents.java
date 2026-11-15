package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.ChaosBlade;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChaosBladeEvents {

    // 1. 手持时：无视凋零 与 无视黑暗
    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            if (hasChaosBlade(player)) {
                if (player.hasEffect(MobEffects.WITHER)) {
                    player.removeEffect(MobEffects.WITHER);
                }
                if (player.hasEffect(MobEffects.DARKNESS)) {
                    player.removeEffect(MobEffects.DARKNESS);
                }
            }
        }
    }

    // 2. 受到攻击前：最高优先级拦截龙息与火球
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && hasChaosBlade(player)) {
            if (isDragonBreathDamage(event.getSource())) {
                event.setCanceled(true);
                player.invulnerableTime = 10;
            }
        }
    }

    // 3. 受到伤害时：第二道防线，处理声波减伤
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && hasChaosBlade(player)) {
            if (isDragonBreathDamage(event.getSource())) {
                event.setCanceled(true);
                return;
            }
            if (event.getSource().is(DamageTypes.SONIC_BOOM)) {
                event.setAmount(event.getAmount() * 0.50F);
            }
        }
    }

    // 4. 铁砧特殊修复
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof ChaosBlade && right.is(Items.NETHER_STAR)) {
            int currentDamage = left.getDamageValue();
            if (currentDamage > 0) {
                int repairAmount = Math.min(100 * right.getCount(), currentDamage);
                ItemStack output = left.copy();
                output.setDamageValue(currentDamage - repairAmount);
                event.setOutput(output);
                event.setCost(repairAmount / 10);
            }
        }
    }

    // --- 辅助方法 ---
    private static boolean hasChaosBlade(Player player) {
        return player.getMainHandItem().getItem() instanceof ChaosBlade ||
                player.getOffhandItem().getItem() instanceof ChaosBlade;
    }

    // 放宽龙息伤害判定，不再限制 Owner 必须是末影龙
    private static boolean isDragonBreathDamage(DamageSource source) {
        // 1. 只要是区域效果云造成的魔法伤害，全都算作龙息云（无视是玩家放的还是龙放的）
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
            if (source.getDirectEntity() instanceof AreaEffectCloud) {
                return true;
            }
        }
        // 2. 拦截火球伤害（龙息弹本体）
        if (source.is(DamageTypes.FIREBALL)) {
            return true;
        }
        // 3. 拦截原版直接龙息类型
        return source.is(DamageTypes.DRAGON_BREATH);
    }
}