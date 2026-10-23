package com.uwjsakdj.quartz_enhanced.recipe;

import com.uwjsakdj.quartz_enhanced.block.entity.ResonanceWorkbenchBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ResonanceRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient resonance;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;
    private final int time;
    private final int safety; // 新增：安全值 0-1000

    public ResonanceRecipe(ResourceLocation id, Ingredient resonance,
                           NonNullList<Ingredient> ingredients, ItemStack output, int time, int safety) {
        this.id = id;
        this.resonance = resonance;
        this.ingredients = ingredients;
        this.output = output;
        this.time = time;
        this.safety = safety;
    }

    public Ingredient getResonance() {
        return resonance;
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public int getTime() {
        return time;
    }

    public int getSafety() {
        return safety;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!resonance.test(container.getItem(ResonanceWorkbenchBlockEntity.RESONANCE_SLOT))) {
            return false;
        }

        List<Ingredient> remaining = new ArrayList<>(ingredients);
        for (int slot : ResonanceWorkbenchBlockEntity.WAITING_SLOTS) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                return false;
            }
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RESONANCE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RESONANCE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}