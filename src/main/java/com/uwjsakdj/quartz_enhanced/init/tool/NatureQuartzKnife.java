package com.uwjsakdj.quartz_enhanced.init.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NatureQuartzKnife extends SwordItem {

    public static final Tier NATURE_QUARTZ_KNIFE_TIER = new Tier() {
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
            return 15;                                      // 附魔能力
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.NATURE_QUARTZ_INGOT.get());
        }
    };

    public NatureQuartzKnife() {
        super(NATURE_QUARTZ_KNIFE_TIER, 1, -2.0F, new Properties());
    }

    // ========== 主手 / 副手 持有 +4 最大生命 ==========
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers =
                HashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    NatureQuartzSword.NATURE_QUARTZ_HEALTH_UUID,
                    NatureQuartzSword.NATURE_QUARTZ_HEALTH_NAME,
                    4.0D, // 与剑、斧等共用 UUID，防止叠加
                    AttributeModifier.Operation.ADDITION
            ));
        }
        return modifiers;
    }

    // ========== 攻击时 1% 概率获得力量 II ==========
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (!attacker.level().isClientSide && attacker.getRandom().nextFloat() < 0.01F) {
            attacker.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST, 10 * 20, 1, false, true
            ));
        }
        return result;
    }

    // ─── Tooltip：未装FD时显示红字提示 ───
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 未装农夫乐事时显示红字提示
        if (!ModList.get().isLoaded("farmersdelight")) {
            tooltip.add(Component.translatable("tooltip.quartz_enhanced.fd_missing")
                    .withStyle(ChatFormatting.RED));
        }
    }

    // ─── 附魔兼容性 ───
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (super.canApplyAtEnchantingTable(stack, enchantment)) {
            return true;
        }
        if (enchantment == Enchantments.BLOCK_EFFICIENCY) {
            return true;
        }
        if (ModList.get().isLoaded("farmersdelight")) {
            ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            return id != null && id.equals(ResourceLocation.parse("farmersdelight:backstabbing"));
        }
        return false;
    }

    // ========== 事件处理：100% 双倍掉落 ==========
    @Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            // 必须是玩家击杀
            if (!(event.getSource().getEntity() instanceof Player player)) return;
            if (player.level().isClientSide) return;

            // 检查主手是否拿着自然石英刀
            ItemStack tool = player.getMainHandItem();
            if (!(tool.getItem() instanceof NatureQuartzKnife)) return;

            // 把掉落物的数量翻倍
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                // 最多翻倍到该物品的最大堆叠数，防止溢出
                int newCount = Math.min(stack.getMaxStackSize(), stack.getCount() * 2);
                stack.setCount(newCount);
            }
        }
    }
}