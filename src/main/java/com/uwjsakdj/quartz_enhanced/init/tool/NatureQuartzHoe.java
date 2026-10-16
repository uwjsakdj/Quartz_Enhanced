package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.block.state.properties.Property;
import java.util.Collection;
import java.util.Collections;

public class NatureQuartzHoe extends HoeItem {

    // ========== Tier：与增强石英锄完全一致 ==========
    public static final Tier NATURE_QUARTZ_HOE_TIER = new Tier() {
        @Override public int getUses() { return 1796; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 3.0F; }
        @Override public int getLevel() { return 3; }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get());
        }
    };

    public NatureQuartzHoe() {
        super(NATURE_QUARTZ_HOE_TIER, -3, 0.0F, new Item.Properties().durability(1796));
    }

    // ========== 主手 / 副手 持有 +8 最大生命 ==========
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers =
                HashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    NatureQuartzSword.NATURE_QUARTZ_HEALTH_UUID,
                    NatureQuartzSword.NATURE_QUARTZ_HEALTH_NAME,
                    8.0D,
                    AttributeModifier.Operation.ADDITION
            ));
        }
        return modifiers;
    }

    // ========== 锄地后耕地直接变成湿的 ==========
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState before = level.getBlockState(pos);
        boolean isTillable = HoeItem.TILLABLES.containsKey(before.getBlock());
        InteractionResult result = super.useOn(context);

        if (isTillable && !level.isClientSide) {
            BlockState after = level.getBlockState(pos);

            // 遍历所有属性，查找名字包含 "moisture" 且类型为整数的属性
            for (Property<?> property : after.getProperties()) {
                if (property.getName().toLowerCase().contains("moisture")
                        && property.getValueClass() == Integer.class) {
                    // 找到后，尝试设置为最大值
                    int max = Collections.max((Collection<Integer>) property.getPossibleValues());
                    level.setBlock(pos, after.setValue((Property<Integer>) property, max), 3);
                    break;
                }
            }
        }
        return result;
    }
}