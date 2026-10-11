package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.item.ModArmorItem;
import com.uwjsakdj.quartz_enhanced.item.ModArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModArmorCombatEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 仅处理玩家受到伤害的场景
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 1. 排除所有弹射物远程伤害（弓箭、火球、三叉戟、潜影弹等）
        if (event.getSource().getDirectEntity() instanceof Projectile) {
            return;
        }

        // 2. 排除魔法伤害（瞬间伤害药水、唤魔者尖牙、凋零效果等）
        if (event.getSource().is(DamageTypes.MAGIC)) {
            return;
        }

        // 仅处理有实体攻击者的伤害（排除摔落、岩浆、溺水等环境伤害）
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        float totalReflectDamage = 0F;

        // 遍历四个护甲槽位，逐件检查特效触发条件
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }

            ItemStack armorStack = player.getItemBySlot(slot);
            if (!(armorStack.getItem() instanceof ModArmorItem modArmor)) {
                continue;
            }

            ModArmorMaterials material = modArmor.getModMaterial();
            int remainingDurability = armorStack.getMaxDamage() - armorStack.getDamageValue();

            // 剩余耐久低于阈值时，累加反伤数值
            if (remainingDurability < material.getEffectDurabilityThreshold()) {
                totalReflectDamage += material.getBonusAttackDamage();
            }
        }

        if (totalReflectDamage > 0) {
            // 使用原版荆棘伤害源，避免伤害死循环
            attacker.hurt(attacker.damageSources().thorns(player), totalReflectDamage);
        }
    }
}