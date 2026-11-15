package com.uwjsakdj.quartz_enhanced.init.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChaosBlade extends SwordItem {

    public static final Tier CHAOS_BLADE_TIER = new Tier() {
        @Override public int getUses() { return 3000; }
        @Override public float getSpeed() { return 10.0F; }
        @Override public float getAttackDamageBonus() { return 6.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public @NotNull Ingredient getRepairIngredient() { return Ingredient.of(Items.NETHER_STAR); }
    };

    private static final MobEffect[] POSITIVE_EFFECTS = {
            MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED,
            MobEffects.JUMP, MobEffects.REGENERATION, MobEffects.DAMAGE_RESISTANCE,
            MobEffects.FIRE_RESISTANCE, MobEffects.ABSORPTION, MobEffects.LUCK
    };
    private static final MobEffect[] NEGATIVE_EFFECTS = {
            MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN, MobEffects.WEAKNESS,
            MobEffects.POISON, MobEffects.WITHER, MobEffects.BLINDNESS,
            MobEffects.HUNGER, MobEffects.CONFUSION, MobEffects.LEVITATION
    };

    public ChaosBlade() {
        super(CHAOS_BLADE_TIER, 1, -2.4F, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
    }

    // ==================== 原有近战特效 ====================
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        Level level = attacker.level();
        if (level.isClientSide) return result;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        }

        long currentTime = level.getGameTime();
        long lastUsed = stack.getOrCreateTag().getLong("chaosBuffCooldown");

        if (currentTime - lastUsed >= 60) {
            boolean targetPositive = level.random.nextFloat() < 0.25F;
            MobEffect targetEffect = targetPositive ?
                    POSITIVE_EFFECTS[level.random.nextInt(POSITIVE_EFFECTS.length)] :
                    NEGATIVE_EFFECTS[level.random.nextInt(NEGATIVE_EFFECTS.length)];
            target.addEffect(new MobEffectInstance(targetEffect, 100, 0));

            boolean selfPositive = level.random.nextFloat() < 0.75F;
            MobEffect selfEffect = selfPositive ?
                    POSITIVE_EFFECTS[level.random.nextInt(POSITIVE_EFFECTS.length)] :
                    NEGATIVE_EFFECTS[level.random.nextInt(NEGATIVE_EFFECTS.length)];
            attacker.addEffect(new MobEffectInstance(selfEffect, 100, 0));

            stack.getOrCreateTag().putLong("chaosBuffCooldown", currentTime);
        }
        return result;
    }

    // ==================== 右键长按技能逻辑 ====================
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        long currentTime = level.getGameTime();
        long lastUsed = stack.getOrCreateTag().getLong("chaosSkillCooldown");

        // 20秒 = 400 ticks
        if (currentTime - lastUsed < 400) {
            if (!level.isClientSide) {
                long remaining = 400 - (currentTime - lastUsed);
                player.displayClientMessage(Component.translatable("message.quartz_enhanced.chaos_blade.cooldown", remaining / 20).withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        // 开始蓄力
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // 👈 关键修复：返回极大值，防止游戏自动结束蓄力
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        // 计算已蓄力时间
        int useTime = this.getUseDuration(stack) - timeLeft;

        // 如果蓄力没满2秒，直接返回
        if (useTime < 40) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("蓄力不足！").withStyle(ChatFormatting.GRAY), true);
            }
            return;
        }

        if (!level.isClientSide) {
            int roll = level.random.nextInt(3);
            Vec3 look = player.getLookAngle();

            if (roll == 0) {
                WitherSkull skull = new WitherSkull(level, player, look.x, look.y, look.z);
                skull.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
                level.addFreshEntity(skull);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else if (roll == 1) {
                DragonFireball fireball = new DragonFireball(level, player, look.x, look.y, look.z);
                fireball.setPos(player.getX(), player.getEyeY() - 0.2, player.getZ());
                level.addFreshEntity(fireball);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                castSonicBoom(level, player);
            }

            // 设置冷却并消耗耐久
            stack.getOrCreateTag().putLong("chaosSkillCooldown", level.getGameTime());
            stack.hurtAndBreak(10, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    // ==================== 声波技能实现 ====================
    private void castSonicBoom(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(15.0));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 hitPos = blockHit.getLocation();

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, hitPos, player.getBoundingBox().expandTowards(look.scale(15.0)).inflate(1.0), (e) -> !e.isSpectator() && e.isPickable());

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            target.hurt(level.damageSources().sonicBoom(player), 10.0F);
            if (target instanceof LivingEntity livingTarget) {
                Vec3 knockback = target.position().subtract(player.position()).normalize().scale(1.5).add(0, 0.5, 0);
                livingTarget.push(knockback.x, knockback.y, knockback.z);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, start.x, start.y, start.z, 1, 0, 0, 0, 0);
            for (int i = 0; i < 15; i++) {
                double t = i / 15.0;
                double px = start.x + (hitPos.x - start.x) * t;
                double py = start.y + (hitPos.y - start.y) * t;
                double pz = start.z + (hitPos.z - start.z) * t;
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // ==================== Tooltip ====================
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.chaos_blade.line1").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.chaos_blade.line2").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.quartz_enhanced.chaos_blade.line3").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.ITALIC));

        if (level != null) {
            long currentTime = level.getGameTime();
            long lastUsed = stack.getOrCreateTag().getLong("chaosSkillCooldown");
            if (currentTime - lastUsed < 400) {
                long remaining = (400 - (currentTime - lastUsed)) / 20;
                tooltip.add(Component.translatable("tooltip.quartz_enhanced.chaos_blade.cooldown", remaining).withStyle(ChatFormatting.RED));
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}