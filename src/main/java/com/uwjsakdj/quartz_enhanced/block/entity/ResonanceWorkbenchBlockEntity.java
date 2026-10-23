package com.uwjsakdj.quartz_enhanced.block.entity;

import com.uwjsakdj.quartz_enhanced.recipe.ModRecipes;
import com.uwjsakdj.quartz_enhanced.recipe.ResonanceRecipe;
import com.uwjsakdj.quartz_enhanced.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ResonanceWorkbenchBlockEntity extends BlockEntity implements WorldlyContainer {

    public static final int SLOT_COUNT = 6;
    public static final int RESONANCE_SLOT = 2;
    public static final int OUTPUT_SLOT = 5;
    public static final int[] WAITING_SLOTS = {0, 1, 3, 4};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 0;
    private ResonanceRecipe currentRecipe = null;

    // 爆炸判定相关状态
    private boolean explosionChecked = false;
    private boolean willExplode = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ResonanceWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_WORKBENCH.get(), pos, state);
    }

    public ContainerData getContainerData() {
        return data;
    }

    // ===================== tick =====================

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        boolean changed = false;

        if (currentRecipe != null && currentRecipe.matches(this, level) && canOutput(currentRecipe)) {

            // 1. 合成开始时，掷骰子判定是否爆炸（只判定一次）
            if (progress == 0 && !explosionChecked) {
                float safetyChance = currentRecipe.getSafety() / 1000.0F;
                // 生成 0.0 到 1.0 的随机数，如果大于安全概率，则必定爆炸
                this.willExplode = level.random.nextFloat() >= safetyChance;
                this.explosionChecked = true;
                changed = true;
            }

            progress++;
            changed = true;

            // 2. 如果判定会爆炸，且进度达到 50%
            if (willExplode && progress >= maxProgress / 2) {
                // 引发爆炸（威力 4.0F 类似 TNT，破坏方块）
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.BLOCK);

                // 清空状态
                progress = 0;
                maxProgress = 0;
                currentRecipe = null;
                explosionChecked = false;
                willExplode = false;
                setChanged();
                return; // 结束 tick
            }

            // 3. 正常完成合成
            if (progress >= maxProgress) {
                craftItem(currentRecipe);
                progress = 0;
                maxProgress = 0;
                currentRecipe = null;
                explosionChecked = false;
                willExplode = false;
            }
        } else {
            // 尝试匹配新配方或中断合成
            ResonanceRecipe recipe = findMatchingRecipe(level);
            if (recipe != null && canOutput(recipe)) {
                currentRecipe = recipe;
                maxProgress = recipe.getTime();
                progress = 0;
                explosionChecked = false; // 重置判定
                willExplode = false;
                changed = true;
            } else if (progress != 0 || maxProgress != 0 || currentRecipe != null) {
                // 中断合成
                progress = 0;
                maxProgress = 0;
                currentRecipe = null;
                explosionChecked = false;
                willExplode = false;
                changed = true;
            }
        }

        if (changed) {
            setChanged();
        }
    }

    private ResonanceRecipe findMatchingRecipe(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.RESONANCE_TYPE.get())
                .stream()
                .filter(r -> r.matches(this, level))
                .findFirst()
                .orElse(null);
    }

    private boolean canOutput(ResonanceRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!output.is(result.getItem())) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craftItem(ResonanceRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        items.get(RESONANCE_SLOT).shrink(1);
        for (int slot : WAITING_SLOTS) {
            items.get(slot).shrink(1);
        }
        setChanged();
    }

    // ===================== NBT =====================

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        // 读取爆炸判定状态
        explosionChecked = tag.getBoolean("ExplosionChecked");
        willExplode = tag.getBoolean("WillExplode");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        // 保存爆炸判定状态
        tag.putBoolean("ExplosionChecked", explosionChecked);
        tag.putBoolean("WillExplode", willExplode);
    }

    // ===================== Container =====================

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index < OUTPUT_SLOT;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == OUTPUT_SLOT;
    }
}