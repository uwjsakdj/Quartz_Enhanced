package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Quartz_Honey_Pudding extends Item {

    private static final int NUTRITION = 6;
    private static final float SATURATION_MOD = 0.8F;
    private static final int REGEN_TICKS = 90 * 20;

    public Quartz_Honey_Pudding() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(NUTRITION)
                        .saturationMod(SATURATION_MOD)
                        .build())
                .stacksTo(1));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    // ─── Tooltip ───
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 基础效果
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.honey_pudding.clear_effect")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.honey_pudding.regen")
                .withStyle(ChatFormatting.BLUE));

        // 兼容农夫乐事的效果
        if (ModList.get().isLoaded("farmersdelight")) {
            tooltip.add(Component.translatable("tooltip.quartz_enhanced.honey_pudding.comfort")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {

        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        // 仅在服务端处理效果
        if (entity instanceof Player player && !level.isClientSide) {
            List<MobEffect> harmfulEffects = player.getActiveEffects().stream()
                    .map(MobEffectInstance::getEffect)
                    .filter(effect -> !effect.isBeneficial())
                    .toList();

            if (!harmfulEffects.isEmpty()) {
                MobEffect randomEffect = harmfulEffects.get(player.getRandom().nextInt(harmfulEffects.size()));
                player.removeEffect(randomEffect);
            }

            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0));

            if (ModList.get().isLoaded("farmersdelight")) {
                applyComfortEffect(player);
            }
        }
        // 如果是创造模式玩家，返回 resultStack（不消耗物品）
        // 否则（生存玩家或生物），直接返回一个空碗
        return entity instanceof Player player && player.isCreative() ? resultStack : new ItemStack(Items.BOWL);
    }

    private void applyComfortEffect(Player player) {
        try {
            Class<?> modEffectsClass = Class.forName("vectorwing.farmersdelight.common.registry.ModEffects");
            java.lang.reflect.Field comfortField = modEffectsClass.getDeclaredField("COMFORT");
            comfortField.setAccessible(true);
            Object effectObj = comfortField.get(null);

            // 👇 优化点：使用 Java 17 的模式匹配，省去强制类型转换
            if (effectObj instanceof MobEffect comfortEffect) {
                // 舒适 I，持续 60秒（1200 ticks）
                player.addEffect(new MobEffectInstance(comfortEffect, 1200, 0));
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }
}