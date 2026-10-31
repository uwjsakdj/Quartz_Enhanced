package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NatureQuartzShovel extends ShovelItem {

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

    private static boolean isFlattening = false;
    public static void setFlattening(boolean flattening) { isFlattening = flattening; }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (isFlattening) return 0;
        return super.damageItem(stack, amount, entity, onBroken);
    }

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

    // ========== 特征扫描版：不猜名字，直接找 Map<Block, BlockState> ==========
    @SuppressWarnings("unchecked")
    public static BlockState getVanillaFlattenedState(BlockState state) {
        try {
            for (java.lang.reflect.Field field : ShovelItem.class.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    if (java.util.Map.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object value = field.get(null);
                        if (value instanceof java.util.Map<?, ?> map) {
                            for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                                if (entry.getKey() instanceof net.minecraft.world.level.block.Block
                                        && entry.getValue() instanceof BlockState) {
                                    return ((java.util.Map<net.minecraft.world.level.block.Block, BlockState>) map)
                                            .get(state.getBlock());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        BlockState modifiedState = null;

        if (stack.canPerformAction(net.minecraftforge.common.ToolActions.SHOVEL_FLATTEN)) {
            modifiedState = state.getToolModifiedState(
                    context, net.minecraftforge.common.ToolActions.SHOVEL_FLATTEN, true
            );
        }
        if (modifiedState == null) {
            modifiedState = getVanillaFlattenedState(state);
        }

        if (modifiedState != null) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, modifiedState);
                level.playSound(null, pos,
                        net.minecraft.sounds.SoundEvents.SHOVEL_FLATTEN,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (player == null || player.level().isClientSide) return;
            ItemStack tool = player.getMainHandItem();
            if (!(tool.getItem() instanceof NatureQuartzShovel)) return;
            if (player.getRandom().nextFloat() < 0.01F) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 15 * 20, 1, false, true));
            }
        }
    }
}