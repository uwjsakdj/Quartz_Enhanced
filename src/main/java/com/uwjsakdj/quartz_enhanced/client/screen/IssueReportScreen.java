package com.uwjsakdj.quartz_enhanced.client.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IssueReportScreen extends Screen {
    private final Screen parent;
    private static final String GITHUB_ISSUES_URL = "https://github.com/uwjsakdj/Quartz_Enhanced/issues";

    private static final ResourceLocation QUARTZ_BG = new ResourceLocation("minecraft:textures/block/quartz_block_side.png");

    public IssueReportScreen(Screen parent) {
        super(Component.translatable("config.quartz_enhanced.issue.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 20;

        // 1. 打开 GitHub Issues 的按钮
        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.issue.open_github"),
                (button) -> Util.getPlatform().openUri(GITHUB_ISSUES_URL)
        ).bounds(centerX - 100, startY, 200, 20).build());

        // 2. 返回按钮
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(this.parent);
                    }
                }
        ).bounds(centerX - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 平铺石英块
        for (int x = 0; x < this.width; x += 16) {
            for (int y = 0; y < this.height; y += 16) {
                guiGraphics.blit(QUARTZ_BG, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        // 2. 暗色遮罩
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // 3. 原本的文字渲染
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.quartz_enhanced.issue.description"),
                this.width / 2, this.height / 2 - 50, 0xAAAAAA);

        // 4. 渲染控件
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}