package com.uwjsakdj.quartz_enhanced.client.screen;

import com.uwjsakdj.quartz_enhanced.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModSettingsScreen extends Screen {
    private final Screen parent;
    private static final ResourceLocation QUARTZ_BG = new ResourceLocation("minecraft:textures/block/quartz_block_side.png");

    // 界面状态
    private boolean expandedWand = true;
    private boolean expandedRecipe = true;
    private boolean dataLoaded = false;

    // 暂存配置
    private boolean hostile, neutral, friendly, players;
    private boolean enableCrushing, enableMilling;
    private String radiusStr;

    // 记录打开界面时的初始配方状态，用于判断是否修改过
    private boolean initEnableCrushing, initEnableMilling;

    // 记录左侧标签文字及其对应的 Y 坐标
    private final List<Component> rowLabels = new ArrayList<>();
    private final List<Integer> rowYs = new ArrayList<>();
    private int labelLeftX;

    // 👈 新增：记录特定行的 Y 坐标
    private int crushingRowY = -1;
    private int millingRowY = -1;

    public ModSettingsScreen(Screen parent) {
        super(Component.translatable("config.quartz_enhanced.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // 只在第一次打开时读取配置
        if (!dataLoaded) {
            this.hostile = Config.WAND_ATTACK_HOSTILE.get();
            this.neutral = Config.WAND_ATTACK_NEUTRAL.get();
            this.friendly = Config.WAND_ATTACK_FRIENDLY.get();
            this.players = Config.WAND_ATTACK_PLAYERS.get();
            this.radiusStr = String.valueOf(Config.WAND_DAMAGE_RADIUS.get());

            this.enableCrushing = Config.ENABLE_CREATE_CRUSHING.get();
            this.enableMilling = Config.ENABLE_CREATE_MILLING.get();

            // 记录初始状态
            this.initEnableCrushing = this.enableCrushing;
            this.initEnableMilling = this.enableMilling;
            dataLoaded = true;
        }

        rowLabels.clear();
        rowYs.clear();
        crushingRowY = -1;
        millingRowY = -1;

        int centerX = this.width / 2;
        labelLeftX = centerX - 200;
        int rightX = centerX + 100;
        int spacing = 24;

        int wandItemsCount = expandedWand ? 5 : 0;
        int recipeItemsCount = expandedRecipe ? 2 : 0;
        int totalHeight = 20 + (wandItemsCount * spacing) + 15 + 20 + (recipeItemsCount * spacing);
        int currentY = Math.max(40, (this.height - totalHeight) / 2);

        // 1. 石英玉魔杖面板
        this.addRenderableWidget(Button.builder(
                Component.literal(expandedWand ? "▼ " : "▶ ").append(Component.translatable("config.quartz_enhanced.wand.section")),
                (btn) -> {
                    this.expandedWand = !this.expandedWand;
                    this.rebuildWidgets();
                }
        ).bounds(centerX - 100, currentY, 200, 20).build());
        currentY += 30;

        if (expandedWand) {
            addToggleRow(rightX, currentY, "config.quartz_enhanced.wand.hostile", this.hostile, val -> this.hostile = val);
            currentY += spacing;
            addToggleRow(rightX, currentY, "config.quartz_enhanced.wand.neutral", this.neutral, val -> this.neutral = val);
            currentY += spacing;
            addToggleRow(rightX, currentY, "config.quartz_enhanced.wand.friendly", this.friendly, val -> this.friendly = val);
            currentY += spacing;
            addToggleRow(rightX, currentY, "config.quartz_enhanced.wand.players", this.players, val -> this.players = val);
            currentY += spacing;

            rowLabels.add(Component.translatable("config.quartz_enhanced.wand.radius"));
            rowYs.add(currentY);

            EditBox radiusBox = new EditBox(this.font, rightX, currentY, 80, 20, Component.empty());
            radiusBox.setValue(radiusStr);
            radiusBox.setFilter(s -> s.matches("\\d*") && s.length() <= 4);
            radiusBox.setResponder(s -> this.radiusStr = s);
            this.addRenderableWidget(radiusBox);
            currentY += spacing;
        }

        // 2. 配方控制面板
        currentY += 5;

        this.addRenderableWidget(Button.builder(
                Component.literal(expandedRecipe ? "▼ " : "▶ ").append(Component.translatable("config.quartz_enhanced.recipe.section")),
                (btn) -> {
                    this.expandedRecipe = !this.expandedRecipe;
                    this.rebuildWidgets();
                }
        ).bounds(centerX - 100, currentY, 200, 20).build());
        currentY += 30;

        if (expandedRecipe) {
            addToggleRow(rightX, currentY, "config.quartz_enhanced.recipe.crushing", this.enableCrushing, val -> this.enableCrushing = val);
            this.crushingRowY = currentY; // 👈 记录粉碎轮行 Y 坐标
            currentY += spacing;

            addToggleRow(rightX, currentY, "config.quartz_enhanced.recipe.milling", this.enableMilling, val -> this.enableMilling = val);
            this.millingRowY = currentY;  // 👈 记录石磨行 Y 坐标
            currentY += spacing;
        }

        // 3. 底部按钮
        int bottomY = this.height - 30;
        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.cancel"),
                (btn) -> {
                    if (this.minecraft != null) this.minecraft.setScreen(this.parent);
                }
        ).bounds(centerX - 105, bottomY, 100, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("config.quartz_enhanced.save_and_exit"),
                (btn) -> {
                    Config.WAND_ATTACK_HOSTILE.set(this.hostile);
                    Config.WAND_ATTACK_NEUTRAL.set(this.neutral);
                    Config.WAND_ATTACK_FRIENDLY.set(this.friendly);
                    Config.WAND_ATTACK_PLAYERS.set(this.players);
                    try {
                        int val = Integer.parseInt(radiusStr);
                        if (val >= 1 && val <= 1000) {
                            Config.WAND_DAMAGE_RADIUS.set(val);
                        }
                    } catch (NumberFormatException ignored) {}

                    Config.ENABLE_CREATE_CRUSHING.set(this.enableCrushing);
                    Config.ENABLE_CREATE_MILLING.set(this.enableMilling);

                    Config.save();
                    if (this.minecraft != null) this.minecraft.setScreen(this.parent);
                }
        ).bounds(centerX + 5, bottomY, 100, 20).build());
    }

    private void addToggleRow(int rightX, int y, String translationKey, boolean initialState, Consumer<Boolean> onValueChange) {
        rowLabels.add(Component.translatable(translationKey));
        rowYs.add(y);

        boolean[] state = { initialState };

        Button btn = Button.builder(
                getToggleText(state[0]),
                (b) -> {
                    state[0] = !state[0];
                    b.setMessage(getToggleText(state[0]));
                    onValueChange.accept(state[0]);
                }
        ).bounds(rightX, y, 50, 20).build();
        this.addRenderableWidget(btn);
    }

    private Component getToggleText(boolean state) {
        return state ? Component.translatable("config.quartz_enhanced.state.on").withStyle(ChatFormatting.GREEN) :
                Component.translatable("config.quartz_enhanced.state.off").withStyle(ChatFormatting.RED);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 平铺石英块背景
        for (int x = 0; x < this.width; x += 16) {
            for (int y = 0; y < this.height; y += 16) {
                guiGraphics.blit(QUARTZ_BG, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        // 2. 暗色遮罩
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // 3. 渲染标题
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // 4. 渲染左侧文本标签
        for (int i = 0; i < rowLabels.size(); i++) {
            guiGraphics.drawString(this.font, rowLabels.get(i), labelLeftX, rowYs.get(i) + 6, 0xFFFFFF);
        }

        // 5. 检查配方开关是否被修改，如果是，则在对应的文字旁边渲染红字提示
        Component notice = Component.translatable("config.quartz_enhanced.recipe.restart_notice").withStyle(ChatFormatting.RED);

        // 如果粉碎轮开关状态变了，在它文字旁边画红字
        if (expandedRecipe && this.enableCrushing != this.initEnableCrushing && crushingRowY != -1) {
            int textWidth = this.font.width(Component.translatable("config.quartz_enhanced.recipe.crushing"));
            guiGraphics.drawString(this.font, notice, labelLeftX + textWidth + 5, crushingRowY + 6, 0xFFFFFF);
        }

        // 如果石磨开关状态变了，在它文字旁边画红字
        if (expandedRecipe && this.enableMilling != this.initEnableMilling && millingRowY != -1) {
            int textWidth = this.font.width(Component.translatable("config.quartz_enhanced.recipe.milling"));
            guiGraphics.drawString(this.font, notice, labelLeftX + textWidth + 5, millingRowY + 6, 0xFFFFFF);
        }

        // 6. 渲染所有控件
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}