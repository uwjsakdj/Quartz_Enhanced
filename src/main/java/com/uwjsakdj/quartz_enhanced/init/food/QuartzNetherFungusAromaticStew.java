package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class QuartzNetherFungusAromaticStew extends Item {

    private static final int NUTRITION = 7;
    private static final float SATURATION_MOD = 0.6F;
    private static final int FIRE_RESIST_TICKS = 120 * 20;

    public QuartzNetherFungusAromaticStew() {
        super(new Item.Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(NUTRITION)
                        .saturationMod(SATURATION_MOD)
                        .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESIST_TICKS, 0), 1.0F)
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

        // 基础效果：抗火
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.nether_stew.fire_resist")
                .withStyle(ChatFormatting.BLUE));

        // 兼容农夫乐事的效果
        if (ModList.get().isLoaded("farmersdelight")) {
            tooltip.add(Component.translatable("tooltip.quartz_enhanced.nether_stew.nourishment")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        // super 方法处理了食物属性里的抗火效果、以及生存模式下的物品消耗
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            // 农夫乐事滋养buff：1分钟（仅在安装FD时生效）
            if (ModList.get().isLoaded("farmersdelight")) {
                MobEffect nourishment = BuiltInRegistries.MOB_EFFECT.get(
                        ResourceLocation.tryParse("farmersdelight:nourishment"));
                if (nourishment != null) {
                    player.addEffect(new MobEffectInstance(nourishment, 1200, 0));
                }
            }
        }
        // 如果是玩家且是创造模式，返回 result（不消耗物品）
        // 否则（生存模式玩家、或狐狸等生物吃下），直接返回一个空碗
        return entity instanceof Player player && player.isCreative() ? result : new ItemStack(Items.BOWL);
    }
}