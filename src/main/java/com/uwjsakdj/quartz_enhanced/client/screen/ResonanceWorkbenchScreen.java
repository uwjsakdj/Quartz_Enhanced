package com.uwjsakdj.quartz_enhanced.client.screen;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.menu.ResonanceWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ResonanceWorkbenchScreen extends AbstractContainerScreen<ResonanceWorkbenchMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quartz_Enhanced.MOD_ID, "textures/gui/container/resonance_workbench.png");

    public ResonanceWorkbenchScreen(ResonanceWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 强制画全屏半透明黑色遮罩
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        // 2. 渲染原版 GUI（画背景图、物品图标等）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 3. 强制手动渲染 Tooltip
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) ((float) progress / maxProgress * 21);
            guiGraphics.blit(TEXTURE, x + 83, y + 35, 176, 0, progressWidth, 14);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }
}