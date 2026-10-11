package com.uwjsakdj.quartz_enhanced.compat.jei;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import com.uwjsakdj.quartz_enhanced.recipe.ResonanceRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ResonanceRecipeCategory implements IRecipeCategory<ResonanceRecipe> {

    public static final RecipeType<ResonanceRecipe> RECIPE_TYPE =
            RecipeType.create(Quartz_Enhanced.MOD_ID, "resonance", ResonanceRecipe.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Quartz_Enhanced.MOD_ID, "textures/gui/container/resonance_workbench.png");

    private final IDrawable background;
    private final IDrawable icon;

    public ResonanceRecipeCategory(IGuiHelper guiHelper) {
        // 裁剪起点左上移 4 像素
        this.background = guiHelper.createDrawable(TEXTURE, 4, 4, 140, 70);
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.RESONANCE_WORKBENCH.get())
        );
    }

    @Override
    public RecipeType<ResonanceRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.quartz_enhanced.resonance");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ResonanceRecipe recipe, IFocusGroup focuses) {
        // 槽位坐标整体右下移 1 像素
        // 左上、右上、左下、右下
        int[][] waitingPositions = {{12, 12}, {50, 12}, {12, 50}, {50, 50}};
        for (int i = 0; i < recipe.getIngredients().size() && i < waitingPositions.length; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT,
                            waitingPositions[i][0], waitingPositions[i][1])
                    .addIngredients(recipe.getIngredients().get(i));
        }

        // 中心共鸣格
        builder.addSlot(RecipeIngredientRole.INPUT, 31, 31)
                .addIngredients(recipe.getResonance());

        // 输出槽
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 30)
                .addItemStack(recipe.getResultItem(null));
    }
}
