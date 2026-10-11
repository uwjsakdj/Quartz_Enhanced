package com.uwjsakdj.quartz_enhanced.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class ModArmorItem extends ArmorItem {

    public ModArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    // 统一处理自定义穿戴贴图路径
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        int layer = (slot == EquipmentSlot.LEGS) ? 2 : 1;
        return "quartz_enhanced:textures/models/armor/" + this.material.getName() + "_layer_" + layer + ".png";
    }

    // 统一处理物品提示文本，自动匹配材质对应的翻译键
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        String tooltipKey = "tooltip.quartz_enhanced.armor." + this.material.getName() + ".desc";
        tooltipComponents.add(Component.translatable(tooltipKey).withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    // 快捷获取对应盔甲材料
    public ModArmorMaterials getModMaterial() {
        return (ModArmorMaterials) this.material;
    }
}