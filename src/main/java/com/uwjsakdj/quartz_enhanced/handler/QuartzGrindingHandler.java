package com.uwjsakdj.quartz_enhanced.handler;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuartzGrindingHandler {

    // ========== 服务端研磨数据 ==========
    private static class GrindingData {
        final BlockPos obsidianPos;
        int ticksElapsed;
        int graceTicks; // 👈 新增：右键宽限期，按住时会不断刷新

        GrindingData(BlockPos pos) {
            this.obsidianPos = pos;
            this.ticksElapsed = 0;
            this.graceTicks = 6; // 初始给 6 tick 的容错
        }
    }

    private static final Map<UUID, GrindingData> GRINDING = new HashMap<>();
    private static final int GRINDING_DURATION = 40;
    private static final int PARTICLE_INTERVAL = 3;

    // ========== 客户端动画数据 ==========
    private static final Map<UUID, BlockPos> CLIENT_GRINDING = new HashMap<>();
    private static final Map<UUID, Integer> CLIENT_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> CLIENT_GRACE = new HashMap<>(); // 👈 客户端宽限期

    // ==================== 右键点击 ====================
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack heldItem = event.getItemStack();
        InteractionHand hand = event.getHand();

        if (hand == InteractionHand.MAIN_HAND
                && heldItem.is(Items.QUARTZ)
                && level.getBlockState(pos).is(Blocks.OBSIDIAN)) {

            event.setCanceled(true);

            if (level.isClientSide) {
                // 客户端：每次右键刷新宽限期
                CLIENT_GRINDING.put(player.getUUID(), pos);
                CLIENT_GRACE.put(player.getUUID(), 6);
                CLIENT_TICKS.putIfAbsent(player.getUUID(), 0);
            } else {
                // 服务端
                GrindingData data = GRINDING.get(player.getUUID());
                if (data == null) {
                    // 第一次按下，播放音效并创建数据
                    level.playSound(null, pos, Quartz_Enhanced.QUARTZ_CRUSH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    GRINDING.put(player.getUUID(), new GrindingData(pos));
                } else {
                    // 已经按住，只需重置宽限期（并可以在这里加持续音效逻辑）
                    data.graceTicks = 6;
                }
            }
        }
    }

    // ==================== 服务端计时与产物 ====================
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        GrindingData data = GRINDING.get(player.getUUID());
        if (data == null) return;

        BlockPos pos = data.obsidianPos;

        // 1. 检查基本条件（方块还在不在、距离、手持物）
        if (!player.level().getBlockState(pos).is(Blocks.OBSIDIAN)
                || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 16.0
                || !player.getMainHandItem().is(Items.QUARTZ)) {
            abortGrinding(player, "条件不满足");
            return;
        }

        // 2. 宽限期判定（松手打断的核心逻辑）
        if (data.graceTicks <= 0) {
            abortGrinding(player, "松开右键或未持续按住");
            return;
        }
        data.graceTicks--; // 每 tick 消耗宽限期

        // 3. 施加缓慢效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 5, false, false, true));

        data.ticksElapsed++;

        // 4. 研磨完成
        if (data.ticksElapsed >= GRINDING_DURATION) {
            GRINDING.remove(player.getUUID());
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);

            // 消耗石英
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.is(Items.QUARTZ)) {
                heldItem.shrink(1);
            }

            // 给予石英粉
            double roll = player.level().random.nextDouble();
            int amount = 1;
            if (roll < 0.15) amount = 4;
            else if (roll < 0.40) amount = 3;
            else if (roll < 0.80) amount = 2;

            ItemStack quartzPowder = new ItemStack(ModItems.QUARTZ_POWDER.get(), amount);
            if (!player.getInventory().add(quartzPowder)) {
                player.drop(quartzPowder, false);
            }
        }
    }

    // 统一的打断方法，清理状态
    private static void abortGrinding(Player player, String reason) {
        GRINDING.remove(player.getUUID());
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        // System.out.println("研磨打断，原因：" + reason);
    }

    // ==================== 客户端动画 ====================
    @Mod.EventBusSubscriber(modid = Quartz_Enhanced.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            UUID uuid = mc.player.getUUID();
            BlockPos pos = CLIENT_GRINDING.get(uuid);
            if (pos == null) return;

            // 基础条件检查
            if (!mc.player.getMainHandItem().is(Items.QUARTZ)
                    || !mc.level.getBlockState(pos).is(Blocks.OBSIDIAN)
                    || mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 16.0) {
                CLIENT_GRINDING.remove(uuid);
                CLIENT_TICKS.remove(uuid);
                CLIENT_GRACE.remove(uuid);
                return;
            }

            // 宽限期判定（客户端松手立刻停止出粒子）
            int grace = CLIENT_GRACE.getOrDefault(uuid, 0);
            if (grace <= 0) {
                CLIENT_GRINDING.remove(uuid);
                CLIENT_TICKS.remove(uuid);
                CLIENT_GRACE.remove(uuid);
                return;
            }
            CLIENT_GRACE.put(uuid, grace - 1);

            int ticks = CLIENT_TICKS.merge(uuid, 1, Integer::sum);

            // 播放粒子
            if (ticks % PARTICLE_INTERVAL == 0) {
                mc.level.addParticle(
                        ParticleTypes.POOF,
                        pos.getX() + 0.2 + mc.level.random.nextDouble() * 0.6,
                        pos.getY() + 1.05,
                        pos.getZ() + 0.2 + mc.level.random.nextDouble() * 0.6,
                        0, 0.03, 0
                );
            }

            // 完成特效
            if (ticks >= GRINDING_DURATION) {
                for (int i = 0; i < 8; i++) {
                    mc.level.addParticle(
                            ParticleTypes.POOF,
                            pos.getX() + 0.3 + mc.level.random.nextDouble() * 0.4,
                            pos.getY() + 1.05 + mc.level.random.nextDouble() * 0.3,
                            pos.getZ() + 0.3 + mc.level.random.nextDouble() * 0.4,
                            (mc.level.random.nextDouble() - 0.5) * 0.1,
                            0.05,
                            (mc.level.random.nextDouble() - 0.5) * 0.1
                    );
                }
                CLIENT_GRINDING.remove(uuid);
                CLIENT_TICKS.remove(uuid);
                CLIENT_GRACE.remove(uuid);
            }
        }
    }
}