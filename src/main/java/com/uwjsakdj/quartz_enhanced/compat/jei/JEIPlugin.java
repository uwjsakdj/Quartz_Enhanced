package com.uwjsakdj.quartz_enhanced.compat.jei;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import com.uwjsakdj.quartz_enhanced.menu.ResonanceWorkbenchMenu;
import com.uwjsakdj.quartz_enhanced.recipe.ModRecipes;
import com.uwjsakdj.quartz_enhanced.recipe.ResonanceRecipe;
import com.uwjsakdj.quartz_enhanced.registry.ModMenuTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(Quartz_Enhanced.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

    // ========== 配方类别 ==========

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new ResonanceRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    // ========== 配方本体 + 信息页 ==========

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 信息页面：左键石英粉时显示
        registration.addIngredientInfo(
                new ItemStack(ModItems.QUARTZ_POWDER.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.quartz_enhanced.quartz_powder.info")
        );

        // 共鸣配方：从客户端配方管理器拉取
        if (Minecraft.getInstance().level != null) {
            List<ResonanceRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.RESONANCE_TYPE.get());
            registration.addRecipes(ResonanceRecipeCategory.RECIPE_TYPE, recipes);
        }
    }

    // ========== 催化剂：把工作台和配方类别绑定 ==========

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.RESONANCE_WORKBENCH.get()),
                ResonanceRecipeCategory.RECIPE_TYPE
        );
    }

    // ========== 转移处理器：JEI 的 "+" 按钮 ==========

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ResonanceWorkbenchMenu.class,
                ModMenuTypes.RESONANCE_WORKBENCH.get(),   // 添加 MenuType 参数
                ResonanceRecipeCategory.RECIPE_TYPE,
                0, 5,       // 工作台输入槽：菜单索引 0~4
                5, 36       // 玩家背包槽：菜单索引 5~40
        );
    }
}