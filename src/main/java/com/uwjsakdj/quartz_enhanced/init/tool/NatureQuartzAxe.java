package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

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

    // ========== 挖掘方块：额外掉落 + 急迫（不依赖 Forge 事件） ==========
    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level,
                             @NotNull BlockState state, @NotNull BlockPos pos,
                             @NotNull LivingEntity entity) {
        boolean result = super.mineBlock(stack, level, state, pos, entity);

        // 仅在服务端处理，避免客户端重复掉落
        if (level.isClientSide) return result;
        if (!(entity instanceof Player player)) return result;
        if (!state.is(BlockTags.LOGS)) return result;

        // 15% 额外掉落一份原木（原版掉落已由 playerDestroy 处理，这里是“额外”部分）
        if (level.random.nextFloat() < 0.15F) {
            Block.dropResources(state, level, pos, null, player, stack);
        }

        // 每次挖掘 1% 概率获得 15 秒急迫 II
        if (player.getRandom().nextFloat() < 0.01F) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SPEED, 15 * 20, 1, false, true
            ));
        }

        return result;
    }

    // ========== 事件处理：仅保留去皮逻辑 ==========
    @Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        // 去皮不消耗耐久
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

            event.setCanceled(true);

            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, stripped);
                level.levelEvent(null, 2001, pos, Block.getId(state));
            }
        }
    }
}