package com.uwjsakdj.quartz_enhanced.init.tool;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ReinforcedQuartzSword extends SwordItem {

    public static final Tier QUARTZ_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1796;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 7.0F; // 材质攻击加成：7.0
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

    public ReinforcedQuartzSword() {
        // 最终伤害计算：玩家基础1.0 + 材质加成7.0 + 剑额外加成0 = 8.0
        super(QUARTZ_TIER, 0, -2.4F, new Item.Properties().durability(1796));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.quartz_enhanced.reinforced_quartz_damage"));
    }
}