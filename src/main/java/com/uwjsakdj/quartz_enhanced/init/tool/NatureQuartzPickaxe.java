package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class NatureQuartzPickaxe extends PickaxeItem {

    // ========== Tier：与增强石英镐完全一致 ==========
    public static final Tier NATURE_QUARTZ_PICKAXE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1796;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 5.0F;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 10;
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get());
        }
    };

    public NatureQuartzPickaxe() {
        super(NATURE_QUARTZ_PICKAXE_TIER, 0, -2.8F, new Item.Properties().durability(1796));
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

    // ========== 事件处理（不再用 @Mod.EventBusSubscriber，改为主类手动注册） ==========
    // ========== 事件处理（不再用 @Mod.EventBusSubscriber，改为主类手动注册） ==========
    public static class Events {

        // 防重：记录最近处理过的方块位置
        private static final java.util.Set<net.minecraft.core.BlockPos> PROCESSED =
                java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (player == null) return;
            if (player.level().isClientSide) return;

            ItemStack tool = player.getMainHandItem();
            if (!(tool.getItem() instanceof NatureQuartzPickaxe)) return;

            // 同一个位置只处理一次
            if (!PROCESSED.add(event.getPos())) {
                return;
            }

            // 防止集合无限增长：每次处理完后，延迟清掉这个位置
            // 用一个简单的办法：集合超过 1000 个元素就清空
            if (PROCESSED.size() > 1000) {
                PROCESSED.clear();
            }

            int xp = 1 + player.getRandom().nextInt(9);
            player.giveExperiencePoints(xp);

            if (player.getRandom().nextFloat() < 0.01F) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED, 15 * 20, 1, false, true
                ));
            }
        }
    }
}
