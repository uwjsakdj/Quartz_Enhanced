package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.uwjsakdj.quartz_enhanced.init.tool.ReinforcedQuartzSword.QUARTZ_TIER;

public class NatureQuartzSword extends SwordItem {

    // 与自然石英斧共用同一个 UUID，避免多件工具叠加生命上限
    public static final UUID NATURE_QUARTZ_HEALTH_UUID =
            UUID.fromString("8e6e4c2a-1234-4b5c-9d6f-a1b2c3d4e5f6");
    public static final String NATURE_QUARTZ_HEALTH_NAME = "nature_quartz_health";

    public NatureQuartzSword() {
        super(QUARTZ_TIER, -1, -2.4F, new Item.Properties().durability(1796));
    }

    // ========== 主手 / 副手 持有 +8 最大生命 ==========
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers =
                HashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    NATURE_QUARTZ_HEALTH_UUID,
                    NATURE_QUARTZ_HEALTH_NAME,
                    8.0D,
                    AttributeModifier.Operation.ADDITION
            ));
        }
        return modifiers;
    }

    // 低血量生命恢复
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof LivingEntity livingEntity)) return;

        boolean mainHand = ItemStack.isSameItem(stack, livingEntity.getMainHandItem());
        boolean offHand = ItemStack.isSameItem(stack, livingEntity.getOffhandItem());
        if (!mainHand && !offHand) return;

        if (livingEntity.getHealth() < 6.0F) {
            MobEffectInstance regen = livingEntity.getEffect(MobEffects.REGENERATION);
            if (regen == null || regen.getAmplifier() < 1 || regen.getDuration() < 20) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, false, true));
            }
        }
    }

    // 攻击凋零效果
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide && attacker.getHealth() < 8.0F && attacker.getRandom().nextFloat() < 0.5F) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 0, false, true));
        }
        return result;
    }
}