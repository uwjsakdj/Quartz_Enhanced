package com.uwjsakdj.quartz_enhanced.compat.jei;

import com.uwjsakdj.quartz_enhanced.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GrindingRecipeCategory implements IRecipeCategory<GrindingRecipe> {

    public static final RecipeType<GrindingRecipe> TYPE =
            RecipeType.create("quartz_enhanced", "grinding", GrindingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable slot; // 👈 把原版格子加回来

    public GrindingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(180, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.OBSIDIAN));
        this.arrow = guiHelper.getRecipeArrow();
        this.slot = guiHelper.getSlotDrawable(); // 获取原版物品格子
    }

    @Override
    public @NotNull RecipeType<GrindingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.quartz_enhanced.grinding.title");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull GrindingRecipe recipe, @NotNull IFocusGroup focuses) {
        // 左侧：石英输入框
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 23)
                .addItemStack(new ItemStack(Items.QUARTZ));

        // 右侧：石英粉输出框
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 23)
                .addItemStack(new ItemStack(ModItems.QUARTZ_POWDER.get()));

        // 右侧：石英碎刺输出框
        builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 23)
                .addItemStack(new ItemStack(ModItems.QUARTZ_SPLINTER.get()));
    }

    @Override
    public void draw(@NotNull GrindingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 手动绘制原版物品格子 (坐标与 addSlot 保持一致)
        this.slot.draw(guiGraphics, 16, 22);  // 石英
        this.slot.draw(guiGraphics, 116, 22); // 石英粉
        this.slot.draw(guiGraphics, 136, 22); // 石英碎刺

        // 画原版大箭头
        this.arrow.draw(guiGraphics, 38, 22);
        this.arrow.draw(guiGraphics, 88, 22);

        // 把 "2s" 居中画在黑曜石上方
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "2s", 74, 11, 0xFFFFFF);

        // 直接渲染黑曜石 (无外框悬浮在中间)
        guiGraphics.renderItem(new ItemStack(Items.OBSIDIAN), 66, 22);

        // 知识之书在最右侧 (无外框)
        guiGraphics.renderItem(new ItemStack(Items.KNOWLEDGE_BOOK), 161, 22);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull GrindingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 161 && mouseX <= 177 && mouseY >= 22 && mouseY <= 38) {
            return List.of(
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.title").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.4", "15%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.3", "25%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.2", "40%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.1", "20%").withStyle(ChatFormatting.GRAY),
                    Component.literal(""),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.splinter_2", "0.5%").withStyle(ChatFormatting.AQUA),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.splinter_1", "2%").withStyle(ChatFormatting.AQUA)
            );
        }
        return List.of();
    }
}