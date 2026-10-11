package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NatureQuartzAxe extends AxeItem {

    // ========== Tier：与增强石英斧完全一致 ==========
    public static final Tier NATURE_QUARTZ_AXE_TIER = new Tier() {
        @Override public int getUses() { return 1796; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 9.0F; }
        @Override public int getLevel() { return 3; }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get());
        }
    };

    public NatureQuartzAxe() {
        super(NATURE_QUARTZ_AXE_TIER, 0, -3.0F, new Item.Properties().durability(1796));
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

    // ========== 事件处理 ==========
    @Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        // 每个玩家挖掘原木的计数器
        private static final Map<UUID, Integer> LOG_COUNTER = new HashMap<>();

        // 1. 去皮不消耗耐久
        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            ItemStack heldItem = event.getItemStack();

            if (!(heldItem.getItem() instanceof NatureQuartzAxe)) return;
            if (!heldItem.canPerformAction(ToolActions.AXE_STRIP)) return;

            BlockState state = level.getBlockState(pos);
            BlockState stripped = AxeItem.getAxeStrippingState(state);
            if (stripped == null) return;

            // 拦下原版逻辑，自己执行
            event.setCanceled(true);

            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, stripped);
                level.levelEvent(null, 2001, pos, Block.getId(state));
                // 关键：不调用 hurtAndBreak，所以耐久不消耗
            }
        }

        // 2. 15% 额外掉落原木 + 每 50 个给急迫 II
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            ItemStack tool = player.getMainHandItem();
            if (!(tool.getItem() instanceof NatureQuartzAxe)) return;

            BlockState state = event.getState();
            if (!state.is(net.minecraft.tags.BlockTags.LOGS)) return;

            // 15% 概率额外掉落同种原木
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                if (serverLevel.random.nextFloat() < 0.15F) {
                    Block.dropResources(state, serverLevel, event.getPos(), null, player, tool);
                }
            }

            // 计数：每 50 个给 15 秒急迫 II
            int count = LOG_COUNTER.merge(player.getUUID(), 1, Integer::sum);
            if (count >= 50) {
                LOG_COUNTER.put(player.getUUID(), 0);
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED, 15 * 20, 1, false, true
                ));
            }
        }
    }
}