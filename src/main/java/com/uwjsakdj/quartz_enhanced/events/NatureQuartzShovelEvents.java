package com.uwjsakdj.quartz_enhanced.events;

import com.uwjsakdj.quartz_enhanced.init.tool.NatureQuartzShovel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "quartz_enhanced", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NatureQuartzShovelEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof NatureQuartzShovel)) return;
        if (!stack.canPerformAction(ToolActions.SHOVEL_FLATTEN)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        BlockState modifiedState = null;

        // 1. Forge ToolAction
        if (event.getFace() != null) {
            UseOnContext context = new UseOnContext(
                    event.getEntity(), event.getHand(),
                    new BlockHitResult(Vec3.atCenterOf(pos), event.getFace(), pos, false)
            );
            modifiedState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, true);
        }

        // 2. 原版 FLATTENABLES 反射（通用回退）
        if (modifiedState == null) {
            modifiedState = NatureQuartzShovel.getVanillaFlattenedState(state);
        }

        // 3. 硬编码天境方块 → 天境土径 (aether:aether_dirt_path)
        if (modifiedState == null) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            if (id != null && "aether".equals(id.getNamespace())) {
                String path = id.getPath();
                // 修正：添加 _block 后缀，匹配准确 ID
                if ("aether_dirt".equals(path)
                        || "aether_grass_block".equals(path)
                        || "enchanted_aether_grass_block".equals(path)) {
                    Block pathBlock = ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("aether", "aether_dirt_path"));
                    if (pathBlock != null) {
                        modifiedState = pathBlock.defaultBlockState();
                    }
                }
            }
        }

        if (modifiedState == null) return;

        // 4. 抢先完成转化，取消事件，阻止后续一切耐久消耗
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        event.setCanceled(true);

        if (!level.isClientSide) {
            NatureQuartzShovel.setFlattening(true);
            try {
                level.setBlockAndUpdate(pos, modifiedState);
                level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            } finally {
                NatureQuartzShovel.setFlattening(false);
            }
        }
    }
}