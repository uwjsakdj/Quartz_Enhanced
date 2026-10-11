package com.uwjsakdj.quartz_enhanced.init.tool;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReinforcedQuartzHoe extends HoeItem {

    public static final Tier QUARTZ_HOE_TIER = new Tier() {
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
            return 3.0F;
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
            return Ingredient.of(ModItems.REINFORCED_QUARTZ_INGOT.get());
        }
    };

    public ReinforcedQuartzHoe() {
        super(QUARTZ_HOE_TIER, -3, 0.0F, new Item.Properties().durability(1796));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.quartz_enhanced.reinforced_quartz_damage"));
    }
}