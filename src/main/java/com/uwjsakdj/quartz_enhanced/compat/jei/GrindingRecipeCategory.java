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

    public GrindingRecipeCategory(IGuiHelper guiHelper) {
        //背景宽度扩大到 160，给最右侧的信息图标留出空间
        this.background = guiHelper.createBlankDrawable(160, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.OBSIDIAN));
        this.arrow = guiHelper.getRecipeArrow();
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
        // 左侧：石英输入框 (x=15, y=21)
        builder.addSlot(RecipeIngredientRole.INPUT, 15, 21)
                .addItemStack(new ItemStack(Items.QUARTZ));

        // 中间：黑曜石 (x=65, y=21)
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 65, 21)
                .addItemStack(new ItemStack(Items.OBSIDIAN));

        // 右侧：石英粉输出框 (x=115, y=21)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 115, 21)
                .addItemStack(new ItemStack(ModItems.QUARTZ_POWDER.get()));
    }

    @Override
    public void draw(@NotNull GrindingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 画原版大箭头
        this.arrow.draw(guiGraphics, 38, 22);
        this.arrow.draw(guiGraphics, 88, 22);

        //把 "2s" 居中画在黑曜石上方
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "2s", 73, 10, 0xFFFFFF);

        // 在石英粉右侧渲染一个知识之书作为“信息”图标 (x=140, y=21)
        guiGraphics.renderItem(new ItemStack(Items.KNOWLEDGE_BOOK), 140, 21);
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull GrindingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        //仅当鼠标悬停在知识之书图标上时 (x: 140~156, y: 21~37) 才显示概率
        if (mouseX >= 140 && mouseX <= 156 && mouseY >= 21 && mouseY <= 37) {
            return List.of(
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.title").withStyle(ChatFormatting.YELLOW),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.4", "15%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.3", "25%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.2", "40%").withStyle(ChatFormatting.GRAY),
                    Component.translatable("jei.quartz_enhanced.grinding.tooltip.1", "20%").withStyle(ChatFormatting.GRAY)
            );
        }
        return List.of();
    }
}