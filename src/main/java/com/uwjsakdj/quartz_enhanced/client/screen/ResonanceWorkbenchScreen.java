package com.uwjsakdj.quartz_enhanced.client.screen;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.menu.ResonanceWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ResonanceWorkbenchScreen extends AbstractContainerScreen<ResonanceWorkbenchMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Quartz_Enhanced.MOD_ID, "textures/gui/container/resonance_workbench.png");

    public ResonanceWorkbenchScreen(ResonanceWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    // 这个方法可以删掉，或者只保留 super 调用
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先画变暗遮罩
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        // 再调用 super.render 来画你的 GUI 界面
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int progress = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        if (maxProgress > 0 && progress > 0) {
            int progressWidth = (int) ((float) progress / maxProgress * 21);
            guiGraphics.blit(TEXTURE, x + 83, y + 35, 176, 0, progressWidth, 14);
        }
    }
}