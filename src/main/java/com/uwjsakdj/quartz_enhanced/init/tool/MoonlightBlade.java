package com.uwjsakdj.quartz_enhanced.init.tool;

import com.uwjsakdj.quartz_enhanced.events.MoonlightBladeEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MoonlightBlade extends SwordItem {

    public static final Tier MOONLIGHT_BLADE_TIER = new Tier() {
        @Override public int getUses() { return 2500; }
        @Override public float getSpeed() { return 10.0F; }
        @Override public float getAttackDamageBonus() { return 5.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public @NotNull Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public MoonlightBlade() {
        super(MOONLIGHT_BLADE_TIER, 1, -2.4F, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.dimension().equals(Level.NETHER)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.quartz_enhanced.moonlight_blade.nether").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (level.dimension().equals(Level.OVERWORLD) && level.isDay()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.quartz_enhanced.moonlight_blade.day").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        long currentTime = level.getGameTime();
        long lastUsed = stack.getOrCreateTag().getLong("moonlightSkillCooldown");
        if (currentTime - lastUsed < 300) {
            if (!level.isClientSide) {
                long remaining = 300 - (currentTime - lastUsed);
                player.displayClientMessage(Component.translatable("message.quartz_enhanced.moonlight_blade.cooldown", remaining / 20).withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        // 👈 使用新的辅助方法检测目标
        LivingEntity target = getAimingTarget(player);
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 0, false, false));

            if (level instanceof ServerLevel serverLevel) {
                int useTime = 72000 - remainingUseDuration;
                int particleCount = Math.min(1 + (useTime / 5), 10);
                // 对于末影龙，粒子出生点要抬高一点
                double yOffset = target.getBbHeight() + 0.5;
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, target.getX(), target.getY() + yOffset, target.getZ(), particleCount, 0.2, 0.1, 0.2, 0.02);
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK, target.getX(), target.getY() + yOffset, target.getZ(), particleCount, 0.2, 0.1, 0.2, 0.02);
            }
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide) return;

        int useTime = 72000 - timeLeft;
        if (useTime < 40) return;

        // 👈 使用新的辅助方法检测目标
        LivingEntity target = getAimingTarget(player);
        if (target != null) {
            MoonlightBladeEvents.addMarkedTarget(target, player);
            stack.getOrCreateTag().putLong("moonlightSkillCooldown", level.getGameTime());
            stack.hurtAndBreak(10, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    // 👈 新增辅助方法：基于 AABB 碰撞箱扫描，完美兼容末影龙等巨型实体
    @Nullable
    private LivingEntity getAimingTarget(Player player) {
        double reach = 20.0D; // 增加探测距离
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(reach));

        // 获取视线范围内所有的活体实体
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().expandTowards(look.scale(reach)).inflate(2.0),
                e -> e != player && e.isAlive() && !e.isSpectator()
        );

        LivingEntity bestTarget = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity target : entities) {
            // 获取目标的碰撞箱，向外扩展 0.5 格，防止准星擦边
            AABB aabb = target.getBoundingBox().inflate(0.5);
            // 检测视线是否与这个碰撞箱相交
            Optional<Vec3> hit = aabb.clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceToSqr(hit.get());
                if (dist < bestDist) {
                    bestDist = dist;
                    bestTarget = target;
                }
            }
        }
        return bestTarget;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.moonlight_blade.line1").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.moonlight_blade.line2").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.moonlight_blade.line3").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.ITALIC));

        if (level != null) {
            long currentTime = level.getGameTime();
            long lastUsed = stack.getOrCreateTag().getLong("moonlightSkillCooldown");
            if (currentTime - lastUsed < 300) {
                long remaining = (300 - (currentTime - lastUsed)) / 20;
                tooltip.add(Component.translatable("tooltip.quartz_enhanced.moonlight_blade.cooldown", remaining).withStyle(ChatFormatting.RED));
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}