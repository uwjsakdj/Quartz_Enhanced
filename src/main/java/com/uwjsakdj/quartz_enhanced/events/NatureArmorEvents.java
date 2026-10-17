package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NatureArmorEvents {

    // 记录玩家上次触发低血回血的时间，防止每秒无限刷新 buff
    private static final Map<UUID, Long> REGEN_COOLDOWN = new HashMap<>();

    // 判断是否穿戴了任意一件自然石英盔甲
    private static boolean hasAnyNatureArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(ModItems.NATURE_QUARTZ_HELMET.get()) ||
                    stack.is(ModItems.NATURE_QUARTZ_CHESTPLATE.get()) ||
                    stack.is(ModItems.NATURE_QUARTZ_LEGGINGS.get()) ||
                    stack.is(ModItems.NATURE_QUARTZ_BOOTS.get())) {
                return true;
            }
        }
        return false;
    }

    // 判断是否穿戴了全套自然石英盔甲
    private static boolean hasFullNatureArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.NATURE_QUARTZ_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NATURE_QUARTZ_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.NATURE_QUARTZ_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.NATURE_QUARTZ_BOOTS.get());
    }

    // ========== 拦截伤害事件（最底层拦截，防止任何受击反馈） ==========
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 1. 免疫浆果丛伤害（完全取消伤害事件，不触发受击动画）
        if (event.getSource().is(DamageTypes.SWEET_BERRY_BUSH) && hasAnyNatureArmor(player)) {
            event.setCanceled(true);
            return;
        }

        // 2. 免疫凋零伤害
        if (event.getSource().is(DamageTypes.WITHER) && hasAnyNatureArmor(player)) {
            event.setCanceled(true);
        }
    }

    // ========== 处理受伤后的效果 ==========
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // 全套效果：受伤 5% 概率获得抗性提升 II (30s)
        if (hasFullNatureArmor(player) && player.getRandom().nextFloat() < 0.05F) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 1, false, true));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        // 4. 全套效果：生命值低于 10 时获得生命恢复 III (15s)
        if (hasFullNatureArmor(player) && player.getHealth() < 10.0F) {
            long currentTime = player.level().getGameTime();
            long lastTrigger = REGEN_COOLDOWN.getOrDefault(player.getUUID(), 0L);

            // 加个冷却，比如 30 秒（600 tick）内只触发一次，防止无限回血
            if (currentTime - lastTrigger > 600) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 2, false, true));
                REGEN_COOLDOWN.put(player.getUUID(), currentTime);
            }
        }
    }
}