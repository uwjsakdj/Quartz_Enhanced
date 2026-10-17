package com.uwjsakdj.quartz_enhanced.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
    // 强化石英套：耐久低于10时触发7点额外反伤
    REINFORCED_QUARTZ(
            "reinforced_quartz",
            35,                     // 耐久乘数
            15,                     // 附魔能力
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,                   // 盔甲韧性
            0.0F,                   // 击退抗性
            () -> Ingredient.of(ModItems.REINFORCED_QUARTZ_INGOT.get()),
            10,                     // 触发反伤的耐久阈值
            7.0F                    // 反伤伤害
    ),

    // 自然石英套：无低耐反伤，生命值加成由物品类处理
    NATURE_QUARTZ(
            "nature_quartz",
            35,                     // 耐久乘数，与强化石英相同
            15,                     // 附魔能力
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,                   // 盔甲韧性
            0.0F,                   // 击退抗性
            () -> Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get()), // 修复材料
            -1,                     // 负数阈值，永不触发低耐反伤
            0.0F                    // 无额外反伤伤害
    );

    // 基础属性
    private final String name;
    private final int durabilityMultiplier;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    // 特效参数
    private final int effectDurabilityThreshold;
    private final float bonusAttackDamage;

    // 对应部位顺序：头盔、胸甲、护腿、靴子
    private static final int[] BASE_DURABILITY = {11, 16, 15, 13};
    private static final int[] DEFENSE_VALUES = {3, 8, 6, 3};

    ModArmorMaterials(String name, int durabilityMultiplier, int enchantmentValue,
                      SoundEvent equipSound, float toughness, float knockbackResistance,
                      Supplier<Ingredient> repairIngredient,
                      int effectDurabilityThreshold, float bonusAttackDamage) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
        this.effectDurabilityThreshold = effectDurabilityThreshold;
        this.bonusAttackDamage = bonusAttackDamage;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY[type.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE_VALUES[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    // 特效参数Getter
    public int getEffectDurabilityThreshold() {
        return effectDurabilityThreshold;
    }

    public float getBonusAttackDamage() {
        return bonusAttackDamage;
    }
}