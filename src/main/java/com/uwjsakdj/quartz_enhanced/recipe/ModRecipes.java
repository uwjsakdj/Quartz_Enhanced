package com.uwjsakdj.quartz_enhanced.recipe;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Quartz_Enhanced.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Quartz_Enhanced.MOD_ID);

    public static final RegistryObject<RecipeType<ResonanceRecipe>> RESONANCE_TYPE =
            RECIPE_TYPES.register("resonance",
                    () -> RecipeType.simple(new ResourceLocation(Quartz_Enhanced.MOD_ID, "resonance")));

    public static final RegistryObject<RecipeSerializer<ResonanceRecipe>> RESONANCE_SERIALIZER =
            RECIPE_SERIALIZERS.register("resonance", ResonanceRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}