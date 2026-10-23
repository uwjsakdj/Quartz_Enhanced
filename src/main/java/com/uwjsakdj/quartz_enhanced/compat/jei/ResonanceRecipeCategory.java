package com.uwjsakdj.quartz_enhanced.compat.jei;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import com.uwjsakdj.quartz_enhanced.recipe.ResonanceRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ResonanceRecipeCategory implements IRecipeCategory<ResonanceRecipe> {

    public static final RecipeType<ResonanceRecipe> RECIPE_TYPE =
            RecipeType.create(Quartz_Enhanced.MOD_ID, "resonance", ResonanceRecipe.class);

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Quartz_Enhanced.MOD_ID, "textures/gui/container/resonance_workbench.png");

    private final IDrawable background;
    private final IDrawable icon;

    public ResonanceRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 4, 4, 140, 70);
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.RESONANCE_WORKBENCH.get())
        );
    }

    @Override
    public @NotNull RecipeType<ResonanceRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, ResonanceRecipe recipe, @NotNull IFocusGroup focuses) {
        // 【修复重点】
        // 必须严格按照 Menu 里的索引顺序添加：0(左上), 1(右上), 2(中心), 3(左下), 4(右下)
        // 这样 JEI 转移时，物品才能一一对应进入正确的槽位。

        // 槽 0：左上
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 12)
                .addIngredients(recipe.getIngredients().get(0));

        // 槽 1：右上
        builder.addSlot(RecipeIngredientRole.INPUT, 50, 12)
                .addIngredients(recipe.getIngredients().get(1));

        // 槽 2：中心（共鸣格）
        builder.addSlot(RecipeIngredientRole.INPUT, 31, 31)
                .addIngredients(recipe.getResonance());

        // 槽 3：左下
        builder.addSlot(RecipeIngredientRole.INPUT, 12, 50)
                .addIngredients(recipe.getIngredients().get(2));

        // 槽 4：右下
        builder.addSlot(RecipeIngredientRole.INPUT, 50, 50)
                .addIngredients(recipe.getIngredients().get(3));

        // 槽 5：输出槽
        builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 30)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(ResonanceRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int safety = recipe.getSafety();
        float percent = safety / 10.0F;

        String text = Component.translatable("jei.quartz_enhanced.safety", String.format("%.1f", percent)).getString();

        int color;
        if (safety >= 1000) {
            color = 0x00AA00;
        } else if (safety >= 800) {
            color = 0xAAAA00;
        } else if (safety >= 500) {
            color = 0xFF8800;
        } else {
            color = 0xFF0000;
        }

        int x = 140 - Minecraft.getInstance().font.width(text) - 2;
        int y = 70 - Minecraft.getInstance().font.lineHeight - 2;

        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, color, true);
    }
}