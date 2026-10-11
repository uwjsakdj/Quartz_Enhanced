package com.uwjsakdj.quartz_enhanced.init.food;

import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

public class Quartz_Nether_Fungus_Aromatic_Stew extends Item {

    private static final int NUTRITION = 7;
    private static final float SATURATION_MOD = 0.6F;
    private static final int FIRE_RESIST_TICKS = 120 * 20;

    public Quartz_Nether_Fungus_Aromatic_Stew() {
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

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            // 农夫乐事滋养buff：1分钟（仅在安装FD时生效）
            if (ModList.get().isLoaded("farmersdelight")) {
                MobEffect nourishment = BuiltInRegistries.MOB_EFFECT.get(
                        new ResourceLocation("farmersdelight", "nourishment"));
                if (nourishment != null) {
                    player.addEffect(new MobEffectInstance(nourishment, 1200, 0));
                }
            }

            // 创造模式：不消耗物品、不返还空碗
            if (player.isCreative()) {
                return stack;
            }
            // 生存模式：食用后返还空碗
            if (stack.getCount() > 1) {
                stack.shrink(1);
                if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                    player.drop(new ItemStack(Items.BOWL), false);
                }
                return stack;
            } else {
                return new ItemStack(Items.BOWL);
            }
        }

        return result;
    }
}