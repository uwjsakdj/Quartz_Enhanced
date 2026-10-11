package com.uwjsakdj.quartz_enhanced.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

public class ResonanceRecipeSerializer implements RecipeSerializer<ResonanceRecipe> {

    @Override
    public ResonanceRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredient resonance = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "resonance"));

        JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < ingredientsArray.size(); i++) {
            ingredients.add(Ingredient.fromJson(ingredientsArray.get(i)));
        }

        ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
        int time = GsonHelper.getAsInt(json, "time", 200);

        return new ResonanceRecipe(id, resonance, ingredients, output, time);
    }

    @Nullable
    @Override
    public ResonanceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Ingredient resonance = Ingredient.fromNetwork(buf);

        int size = buf.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
        for (int i = 0; i < size; i++) {
            ingredients.set(i, Ingredient.fromNetwork(buf));
        }

        ItemStack output = buf.readItem();
        int time = buf.readVarInt();

        return new ResonanceRecipe(id, resonance, ingredients, output, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ResonanceRecipe recipe) {
        recipe.getResonance().toNetwork(buf);

        buf.writeVarInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.toNetwork(buf);
        }

        buf.writeItem(recipe.getResultItem(null));
        buf.writeVarInt(recipe.getTime());
    }
}