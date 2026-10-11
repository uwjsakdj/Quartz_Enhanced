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

        GrindingData(BlockPos pos) {
            this.obsidianPos = pos;
            this.ticksElapsed = 0;
        }
    }

    private static final Map<UUID, GrindingData> GRINDING = new HashMap<>();
    private static final int GRINDING_DURATION = 40;
    private static final int PARTICLE_INTERVAL = 3;

    // ========== 客户端动画数据 ==========
    private static final Map<UUID, BlockPos> CLIENT_GRINDING = new HashMap<>();
    private static final Map<UUID, Integer> CLIENT_TICKS = new HashMap<>();

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
                CLIENT_TICKS.put(player.getUUID(), 0);
            } else {
                // ★★★ 右键点下去立刻播放声音 ★★★
                level.playSound(
                        null,
                        pos,
                        Quartz_Enhanced.QUARTZ_CRUSH.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
                GRINDING.put(player.getUUID(), new GrindingData(pos));
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

        if (!player.level().getBlockState(pos).is(Blocks.OBSIDIAN)
                || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 16.0
                || !player.getMainHandItem().is(Items.QUARTZ)) {
            GRINDING.remove(player.getUUID());
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            return;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 3, 5, false, false, true
        ));

        data.ticksElapsed++;

        // ========== 研磨完成 ==========
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
            if (roll < 0.15) {
                amount = 4;
            } else if (roll < 0.40) {
                amount = 3;
            } else if (roll < 0.80) {
                amount = 2;
            }

            ItemStack quartzPowder = new ItemStack(ModItems.QUARTZ_POWDER.get(), amount);
            if (!player.getInventory().add(quartzPowder)) {
                player.drop(quartzPowder, false);
            }
        }
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
                return;
            }

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
            }
        }
    }
}
