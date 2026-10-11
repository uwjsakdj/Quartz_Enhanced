package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

public class NatureQuartzShovel extends ShovelItem {

    // ========== Tier：与增强石英锹完全一致 ==========
    public static final Tier NATURE_QUARTZ_SHOVEL_TIER = new Tier() {
        @Override public int getUses() { return 1796; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 5.5F; }
        @Override public int getLevel() { return 3; }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get());
        }
    };

    public NatureQuartzShovel() {
        super(NATURE_QUARTZ_SHOVEL_TIER, 0F, -3.0F, new Item.Properties().durability(1796));
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

    // ========== 重写 useOn：任意面转化土径，且不消耗耐久 ==========
    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // 不再限制面，六个面都可转化
        if (!state.is(Blocks.DIRT_PATH)) {
            BlockState pathState = ShovelItem.FLATTENABLES.get(state.getBlock());
            // 仍然需要上方有空间，否则土径会嵌进上方方块里
            if (pathState != null && level.getBlockState(pos.above()).isAir()) {
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos, pathState);
                    level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useOn(context);
    }

    // ========== 事件处理 ==========
    @Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        // 每次挖掘方块有 1% 概率获得 15 秒急迫 II
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (player == null) return;
            if (player.level().isClientSide) return;

            ItemStack tool = player.getMainHandItem();
            if (!(tool.getItem() instanceof NatureQuartzShovel)) return;

            if (player.getRandom().nextFloat() < 0.01F) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SPEED, 15 * 20, 1, false, true
                ));
            }
        }
    }
}