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
        int graceTicks;

        GrindingData(BlockPos pos) {
            this.obsidianPos = pos;
            this.ticksElapsed = 0;
            this.graceTicks = 6;
        }
    }

    private static final Map<UUID, GrindingData> GRINDING = new HashMap<>();
    private static final int GRINDING_DURATION = 40;
    private static final int PARTICLE_INTERVAL = 3;

    // ========== 客户端动画数据 ==========
    private static final Map<UUID, BlockPos> CLIENT_GRINDING = new HashMap<>();
    private static final Map<UUID, Integer> CLIENT_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> CLIENT_GRACE = new HashMap<>();

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
                CLIENT_GRINDING.put(player.getUUID(), pos);
                CLIENT_GRACE.put(player.getUUID(), 6);
                CLIENT_TICKS.putIfAbsent(player.getUUID(), 0);
            } else {
                GrindingData data = GRINDING.get(player.getUUID());
                if (data == null) {
                    level.playSound(null, pos, Quartz_Enhanced.QUARTZ_CRUSH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    GRINDING.put(player.getUUID(), new GrindingData(pos));
                } else {
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
        data.graceTicks--;

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

            // 👇 新增：给予石英碎刺 (0.5%给2个, 2%给1个)
            double splinterRoll = player.level().random.nextDouble();
            int splinterAmount = 0;
            if (splinterRoll < 0.005) {
                splinterAmount = 2;
            } else if (splinterRoll < 0.025) {
                splinterAmount = 1;
            }

            if (splinterAmount > 0) {
                // 注意：这里的 QUARTZ_SPLINTER 请与你 ModItems 中实际注册的变量名保持一致
                ItemStack quartzSplinter = new ItemStack(ModItems.QUARTZ_SPLINTER.get(), splinterAmount);
                if (!player.getInventory().add(quartzSplinter)) {
                    player.drop(quartzSplinter, false);
                }
            }
        }
    }

    private static void abortGrinding(Player player, String reason) {
        GRINDING.remove(player.getUUID());
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
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

            if (!mc.player.getMainHandItem().is(Items.QUARTZ)
                    || !mc.level.getBlockState(pos).is(Blocks.OBSIDIAN)
                    || mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 16.0) {
                CLIENT_GRINDING.remove(uuid);
                CLIENT_TICKS.remove(uuid);
                CLIENT_GRACE.remove(uuid);
                return;
            }

            int grace = CLIENT_GRACE.getOrDefault(uuid, 0);
            if (grace <= 0) {
                CLIENT_GRINDING.remove(uuid);
                CLIENT_TICKS.remove(uuid);
                CLIENT_GRACE.remove(uuid);
                return;
            }
            CLIENT_GRACE.put(uuid, grace - 1);

            int ticks = CLIENT_TICKS.merge(uuid, 1, Integer::sum);

            if (ticks % PARTICLE_INTERVAL == 0) {
                mc.level.addParticle(
                        ParticleTypes.POOF,
                        pos.getX() + 0.2 + mc.level.random.nextDouble() * 0.6,
                        pos.getY() + 1.05,
                        pos.getZ() + 0.2 + mc.level.random.nextDouble() * 0.6,
                        0, 0.03, 0
                );
            }

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