package com.uwjsakdj.quartz_enhanced.init.tool;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReinforcedQuartzKnife extends SwordItem {

    public static final Tier REINFORCED_QUARTZ_KNIFE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 1796;                                    // 耐久度
        }

        @Override
        public float getSpeed() {
            return 8.0F;                                    // 挖掘速度
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.5F;                                    // 3.5 + 1(构造参数) + 1(基础) = 5.5
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;                                      // 附魔能力（铁=14，钻石=10，金=25）
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.REINFORCED_QUARTZ_INGOT.get());
        }
    };

    public ReinforcedQuartzKnife() {
        // 攻击伤害 = 3.5(Tier) + 1(参数) + 1(基础) = 5.5
        // 攻击速度 = 4.0 + (-2.0) = 2.0
        super(REINFORCED_QUARTZ_KNIFE_TIER, 1, -2.0F, new Properties());
    }


    // ─── Tooltip：未装FD时显示红字提示 ───
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 始终显示伤害提示
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.reinforced_quartz_damage"));

        // 未装农夫乐事时显示红字提示
        if (!ModList.get().isLoaded("farmersdelight")) {
            tooltip.add(Component.translatable("tooltip.quartz_enhanced.fd_missing")
                    .withStyle(ChatFormatting.RED));
        }
    }


    // ─── 附魔兼容性 ───
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // 保留剑类原有的附魔（锋利、亡灵杀手、节肢杀手、击退、火焰附加、抢夺、耐久、经验修补）
        if (super.canApplyAtEnchantingTable(stack, enchantment)) {
            return true;
        }
        // 效率附魔（农夫乐事的刀均支持效率）
        if (enchantment == Enchantments.BLOCK_EFFICIENCY) {
            return true;
        }
        // 背刺附魔（仅在农夫乐事安装时生效）
        if (ModList.get().isLoaded("farmersdelight")) {
            ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            return id != null && id.equals(new ResourceLocation("farmersdelight", "backstabbing"));
        }
        return false;

    }
}
