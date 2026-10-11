package com.uwjsakdj.quartz_enhanced.init.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import com.uwjsakdj.quartz_enhanced.Config;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.NeutralMob;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuartzJadeMagicWand extends Item {

    // ========== 常量配置 ==========
    private static final int CHARGE_TICKS = 5 * 20;          // 蓄力时长：5秒
    private static final int SPELL_DURATION_TICKS = 25 * 20; // 动画总时长：25秒
    private static final double RADIUS_LOW_CIRCLE = 50.0;    // 底部定点粒子圆半径
    private static final double RADIUS_HIGH_EDGE = 150.0;    // 顶部边缘圆半径
    private static final double RADIUS_LIGHTNING = 400.0;    // 闪电生成范围半径
    private static final double RADIUS_DAMAGE = 500.0;       // 最终伤害圆柱半径
    private static final float DAMAGE_AMOUNT = 400.0F;       // 最终伤害值
    private static final double HEIGHT_OFFSET = 50.0;        // 上下定点高度差

    // 活跃法术实例列表
    private static final List<ActiveSpell> ACTIVE_SPELLS = new ArrayList<>();

    public QuartzJadeMagicWand() {
        super(new Item.Properties()
                .stacksTo(1)       // 一次性用品，不可堆叠
                .fireResistant());
    }

    // ========== 基础物品属性 ==========
    // 完全禁止附魔（附魔台+铁砧均无效）
    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    // 蓄力动作为弓类蓄力姿势
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    // 蓄力总时长
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return CHARGE_TICKS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.quartz_enhanced.high_performance_note"));
    }

    // ========== 【修复新增】右键启动蓄力 ==========
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 非创造模式下，经验等级不足45则无法释放
        if (!player.isCreative() && player.experienceLevel < 45) {
            // 仅服务端发送提示消息，避免客户端重复弹窗
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.quartz_enhanced.insufficient_experience"));
            }
            return InteractionResultHolder.fail(stack);
        }

        // 等级达标，启动蓄力流程
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }


    // ========== 蓄力完成触发法术 ==========
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        super.finishUsingItem(stack, level, livingEntity);

        // 仅服务端执行，且仅玩家可释放
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return stack;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 castPos = player.position();

        // 注册法术实例
        ACTIVE_SPELLS.add(new ActiveSpell(serverLevel, castPos, player.getUUID()));

        // 生存模式消耗法杖
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // ========== 施法代价 ==========
        if (!player.isCreative()) {
            // 扣除45级经验，等级不足45则自动清零
            player.giveExperienceLevels(-45);

            // 扣除19点饱食度
            int currentFood = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.max(0, currentFood - 19));

            // 施加30秒饥饿I
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 30 * 20, 0));
        }

        return stack;
    }

    // ========== 活跃法术数据结构 ==========
    private static class ActiveSpell {
        final ServerLevel level;
        final Vec3 pos1;       // 底部定点（玩家释放位置）
        final Vec3 pos2;       // 顶部定点（pos1上方50格）
        final UUID casterId;   // 施法者UUID，用于伤害豁免
        int ticksElapsed;      // 已运行刻数

        ActiveSpell(ServerLevel level, Vec3 pos1, UUID casterId) {
            this.level = level;
            this.pos1 = pos1;
            this.pos2 = pos1.add(0, HEIGHT_OFFSET, 0);
            this.casterId = casterId;
            this.ticksElapsed = 0;
        }
    }

    // ========== 服务器Tick：法术帧更新 ==========
    @Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class SpellTickHandler {

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            // 迭代器安全遍历，到期自动移除法术
            var iterator = ACTIVE_SPELLS.iterator();
            while (iterator.hasNext()) {
                ActiveSpell spell = iterator.next();
                spell.ticksElapsed++;

                // 每tick生成粒子效果
                spawnSpellParticles(spell);

                // 每2tick生成一道闪电，控制性能开销
                if (spell.ticksElapsed % 2 == 0) {
                    spawnRandomLightning(spell);
                }

                // 动画结束，执行最终伤害
                if (spell.ticksElapsed >= SPELL_DURATION_TICKS) {
                    executeFinalDamage(spell);
                    iterator.remove();
                }
            }
        }
    }

    // ========== 粒子效果生成 ==========
    private static void spawnSpellParticles(ActiveSpell spell) {
        ServerLevel level = spell.level;
        Vec3 pos1 = spell.pos1;
        Vec3 pos2 = spell.pos2;
        float progress = (float) spell.ticksElapsed / SPELL_DURATION_TICKS; // 0→1 动画进度

        // 1. 底部定点：半径50格 composter 粒子圆（带缓慢旋转）
        int lowCircleCount = 20;
        for (int i = 0; i < lowCircleCount; i++) {
            double angle = (2 * Math.PI / lowCircleCount) * i + progress * 0.5;
            double x = pos1.x + RADIUS_LOW_CIRCLE * Math.cos(angle);
            double z = pos1.z + RADIUS_LOW_CIRCLE * Math.sin(angle);
            level.sendParticles(ParticleTypes.COMPOSTER, x, pos1.y, z, 1, 0, 0, 0, 0);
        }

        // 2. 顶部定点：半径150格 damage_indicator 边缘粒子圆（反向缓慢旋转）
        int edgeCircleCount = 30;
        for (int i = 0; i < edgeCircleCount; i++) {
            double angle = (2 * Math.PI / edgeCircleCount) * i - progress * 0.3;
            double x = pos2.x + RADIUS_HIGH_EDGE * Math.cos(angle);
            double z = pos2.z + RADIUS_HIGH_EDGE * Math.sin(angle);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, pos2.y, z, 1, 0, 0, 0, 0);
        }

        // 3. 顶部圆内：稀疏 explosion 粒子随机填充
        int explosionCount = 10;
        for (int i = 0; i < explosionCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * RADIUS_HIGH_EDGE;
            double x = pos2.x + radius * Math.cos(angle);
            double z = pos2.z + radius * Math.sin(angle);
            level.sendParticles(ParticleTypes.EXPLOSION, x, pos2.y, z, 1, 0, 0, 0, 0);
        }

        // 4. 两点连线：向外发散的橙色渐变粒子
        int enchantCount = 15;
        float r = 1.0F - progress * 0.5F;   // 红色：亮橙→暗橙
        float g = 0.5F - progress * 0.3F;   // 绿色：随进度降低
        float b = 0.1F;                      // 蓝色固定低值
        DustParticleOptions orangeDust = new DustParticleOptions(new Vector3f(r, g, b), 1.0F);

        for (int i = 0; i < enchantCount; i++) {
            // 在连线上随机取高度点
            double yOffset = Math.random() * HEIGHT_OFFSET;
            Vec3 linePos = pos1.add(0, yOffset, 0);

            // 随机水平方向向外发散
            double angle = Math.random() * 2 * Math.PI;
            double speed = 0.3 + Math.random() * 0.3;
            double dx = speed * Math.cos(angle);
            double dz = speed * Math.sin(angle);

            level.sendParticles(orangeDust, linePos.x, linePos.y, linePos.z, 1, dx, 0.1, dz, 0);
        }
    }

    // ========== 随机闪电生成 ==========
    private static void spawnRandomLightning(ActiveSpell spell) {
        ServerLevel level = spell.level;
        Vec3 pos2 = spell.pos2;

        // 在顶部400格半径圆内随机选点
        double angle = Math.random() * 2 * Math.PI;
        double radius = Math.random() * RADIUS_LIGHTNING;
        double x = pos2.x + radius * Math.cos(angle);
        double z = pos2.z + radius * Math.sin(angle);

        // 沿高度图落到地面，避免闪电悬浮在半空
        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 0, z));

        // 生成闪电实体
        var lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(groundPos));
            level.addFreshEntity(lightning);
        }
    }

    // ========== 最终范围伤害 ==========
    private static void executeFinalDamage(ActiveSpell spell) {
        ServerLevel level = spell.level;
        Vec3 pos1 = spell.pos1;

        Player caster = level.getPlayerByUUID(spell.casterId);
        DamageSource spellDamage;

        if (caster != null) {
            spellDamage = level.damageSources().indirectMagic(caster, caster);
        } else {
            spellDamage = level.damageSources().explosion(null);
        }

        // 从配置读取伤害半径
        int radius = Config.wandDamageRadius;

        AABB roughArea = new AABB(
                pos1.x - radius, level.getMinBuildHeight(), pos1.z - radius,
                pos1.x + radius, level.getMaxBuildHeight(), pos1.z + radius
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, roughArea, entity -> {
            double horizontalDist = Math.sqrt(
                    Math.pow(entity.getX() - pos1.x, 2) + Math.pow(entity.getZ() - pos1.z, 2)
            );
            if (horizontalDist > radius) return false;
            if (entity.getUUID().equals(spell.casterId)) return false;
            return shouldDamage(entity);
        });

        for (LivingEntity target : targets) {
            target.hurt(spellDamage, DAMAGE_AMOUNT);
        }
    }

    /**
     * 根据配置判断某个生物是否应该被魔杖伤害
     */
    private static boolean shouldDamage(LivingEntity entity) {
        // 玩家
        if (entity instanceof Player) {
            return Config.wandAttackPlayers;
        }
        // 中立生物（注意：必须放在 Monster 之前，因为僵尸猪灵同时是两者）
        if (entity instanceof NeutralMob) {
            return Config.wandAttackNeutral;
        }
        // 敌对生物
        if (entity instanceof Monster) {
            return Config.wandAttackHostile;
        }
        // 剩下的一律归为友好生物
        return Config.wandAttackFriendly;
    }
}