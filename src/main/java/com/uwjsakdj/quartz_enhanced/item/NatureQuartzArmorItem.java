package com.uwjsakdj.quartz_enhanced.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class NatureQuartzArmorItem extends ModArmorItem {

    // 自然石英盔甲专用的 UUID，与工具、强化石英盔甲区分开
    public static final UUID NATURE_ARMOR_HEALTH_UUID =
            UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    public static final String NATURE_ARMOR_HEALTH_NAME = "nature_quartz_armor_health";

    public NatureQuartzArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create(super.getAttributeModifiers(slot, stack));

        // 只有穿戴在对应的护甲槽位时生效
        if (slot == this.getEquipmentSlot()) {
            double bonusHealth = switch (this.getType()) {
                case HELMET -> 4.0D;
                case CHESTPLATE -> 8.0D;
                case LEGGINGS -> 6.0D;
                case BOOTS -> 2.0D;
            };

            // 注意：为了允许四件套叠加，每个部位使用独立的 UUID
            // 在原版中，护甲槽位的 UUID 本来就不同，所以这里动态生成或者使用固定的 4 个
            // 为了防止重载导致 UUID 变化，这里写死 4 个 UUID
            UUID uuid = switch (this.getType()) {
                case HELMET -> UUID.fromString("11111111-aaaa-bbbb-cccc-000000000001");
                case CHESTPLATE -> UUID.fromString("22222222-aaaa-bbbb-cccc-000000000002");
                case LEGGINGS -> UUID.fromString("33333333-aaaa-bbbb-cccc-000000000003");
                case BOOTS -> UUID.fromString("44444444-aaaa-bbbb-cccc-000000000004");
            };

            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    uuid,
                    NATURE_ARMOR_HEALTH_NAME,
                    bonusHealth,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        return modifiers;
    }
}