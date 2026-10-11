package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobEquipmentEvents {

    private static final String EQUIPMENT_CHECKED_TAG = "quartz_equipment_applied";
    private static final float DEFAULT_DROP_CHANCE = 0.085F;

    @SubscribeEvent
    public static void onZombieJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }

        if (zombie.getPersistentData().getBoolean(EQUIPMENT_CHECKED_TAG)) {
            return;
        }
        zombie.getPersistentData().putBoolean(EQUIPMENT_CHECKED_TAG, true);

        Difficulty difficulty = event.getLevel().getCurrentDifficultyAt(zombie.blockPosition()).getDifficulty();

        float armorSlotChance;
        float swordChance;
        switch (difficulty) {
            case PEACEFUL -> {
                armorSlotChance = 0.00F;
                swordChance = 0.00F;
            }
            case EASY -> {
                armorSlotChance = 0.001F;     // 简单难度：每个盔甲槽位 0.1% 概率生成
                swordChance = 0.002F;         // 简单难度：主手武器 0.2% 概率生成
            }
            case NORMAL -> {
                armorSlotChance = 0.002F;     // 普通难度：每个盔甲槽位 0.2% 概率生成
                swordChance = 0.005F;         // 普通难度：主手武器 0.5% 概率生成
            }
            case HARD -> {
                armorSlotChance = 0.004F;     // 困难难度：每个盔甲槽位 0.4% 概率生成
                swordChance = 0.008F;         // 困难难度：主手武器 0.8% 概率生成
            }
            default -> {
                armorSlotChance = 0.0F;
                swordChance = 0.0F;
            }
        }

        // 头盔
        if (zombie.getRandom().nextFloat() < armorSlotChance
                && zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.REINFORCED_QUARTZ_HELMET.get()));
            zombie.setDropChance(EquipmentSlot.HEAD, DEFAULT_DROP_CHANCE);
        }

        // 胸甲
        if (zombie.getRandom().nextFloat() < armorSlotChance
                && zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.REINFORCED_QUARTZ_CHESTPLATE.get()));
            zombie.setDropChance(EquipmentSlot.CHEST, DEFAULT_DROP_CHANCE);
        }

        // 护腿
        if (zombie.getRandom().nextFloat() < armorSlotChance
                && zombie.getItemBySlot(EquipmentSlot.LEGS).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.REINFORCED_QUARTZ_LEGGINGS.get()));
            zombie.setDropChance(EquipmentSlot.LEGS, DEFAULT_DROP_CHANCE);
        }

        // 靴子
        if (zombie.getRandom().nextFloat() < armorSlotChance
                && zombie.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.REINFORCED_QUARTZ_BOOTS.get()));
            zombie.setDropChance(EquipmentSlot.FEET, DEFAULT_DROP_CHANCE);
        }

        // 主手强化石英剑
        if (zombie.getRandom().nextFloat() < swordChance
                && zombie.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.REINFORCED_QUARTZ_SWORD.get()));
            zombie.setDropChance(EquipmentSlot.MAINHAND, DEFAULT_DROP_CHANCE);
        }
    }
}