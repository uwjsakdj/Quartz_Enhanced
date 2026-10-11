package com.uwjsakdj.quartz_enhanced.menu;

import com.uwjsakdj.quartz_enhanced.block.entity.ResonanceWorkbenchBlockEntity;
import com.uwjsakdj.quartz_enhanced.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ResonanceWorkbenchMenu extends AbstractContainerMenu {

    private final ResonanceWorkbenchBlockEntity blockEntity;
    private final ContainerData data;

    // 客户端构造：从网络包读取方块位置
    public ResonanceWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, resolveBlockEntity(playerInventory, extraData.readBlockPos()));
    }

    // 服务端构造
    public ResonanceWorkbenchMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, resolveBlockEntity(playerInventory, pos));
    }

    private ResonanceWorkbenchMenu(int containerId, Inventory playerInventory, ResonanceWorkbenchBlockEntity blockEntity) {
        super(ModMenuTypes.RESONANCE_WORKBENCH.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getContainerData();
        this.addSlots(playerInventory);
        this.addDataSlots(data);
    }

    private static ResonanceWorkbenchBlockEntity resolveBlockEntity(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ResonanceWorkbenchBlockEntity be) {
            return be;
        }
        throw new IllegalStateException("No ResonanceWorkbenchBlockEntity at " + pos);
    }

    private void addSlots(Inventory playerInventory) {
        // 输入槽 0~4
        this.addSlot(new Slot(blockEntity, 0, 16, 16)); // 左上
        this.addSlot(new Slot(blockEntity, 1, 54, 16)); // 右上
        this.addSlot(new Slot(blockEntity, 2, 35, 35)); // 中心
        this.addSlot(new Slot(blockEntity, 3, 16, 54)); // 左下
        this.addSlot(new Slot(blockEntity, 4, 54, 54)); // 右下

        // 输出槽 5：不允许放入
        this.addSlot(new Slot(blockEntity, 5, 112, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家主背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            int containerSlots = ResonanceWorkbenchBlockEntity.SLOT_COUNT;

            if (index < containerSlots) {
                // 从工作台移回玩家背包
                if (!this.moveItemStackTo(stackInSlot, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包移入输入槽（不往输出槽放）
                if (!this.moveItemStackTo(stackInSlot, 0, ResonanceWorkbenchBlockEntity.OUTPUT_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    // ========== 进度条数据 ==========

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }
}