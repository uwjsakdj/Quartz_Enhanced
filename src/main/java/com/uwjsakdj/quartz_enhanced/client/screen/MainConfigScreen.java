package com.uwjsakdj.quartz_enhanced.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MainConfigScreen extends Screen {
    private final Screen parent;

    private static final ResourceLocation QUARTZ_BG = new ResourceLocation("minecraft:textures/block/quartz_block_side.png");

    public MainConfigScreen(Screen parent) {
        super(Component.translatable("config.quartz_enhanced.main.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;
        int spacing = 25;

        // 1. 模组版本
        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.main.version"),
                (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new VersionScreen(this));
                    }
                }
        ).bounds(centerX - 75, startY, 150, 20).build());

        // 2. 模组配置
        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.main.settings"),
                (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ModSettingsScreen(this));
                    }
                }
        ).bounds(centerX - 75, startY + spacing, 150, 20).build());

        // 3. 问题报告
        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.main.issue"),
                (button) -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new IssueReportScreen(this));
                    }
                }
        ).bounds(centerX - 75, startY + spacing * 2, 150, 20).build());

        // 4. 返回按钮
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
        // 1. 暴力循环平铺石英块背景
        for (int x = 0; x < this.width; x += 16) {
            for (int y = 0; y < this.height; y += 16) {
                guiGraphics.blit(QUARTZ_BG, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        // 2. 覆盖一层半透明黑色（0x99代表透明度），让文字清晰可见
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // 3. 渲染标题和控件
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}