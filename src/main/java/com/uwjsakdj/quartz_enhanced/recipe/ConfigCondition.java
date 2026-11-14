package com.uwjsakdj.quartz_enhanced.recipe;

import com.google.gson.JsonObject;
import com.uwjsakdj.quartz_enhanced.Config;
import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class ConfigCondition implements ICondition {
    // 👈 改为 public static final
    public static final ResourceLocation ID = new ResourceLocation(Quartz_Enhanced.MOD_ID, "config");
    private final String recipeType;

    public ConfigCondition(String recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        try {
            if ("crushing".equals(recipeType)) {
                return Config.ENABLE_CREATE_CRUSHING.get();
            } else if ("milling".equals(recipeType)) {
                return Config.ENABLE_CREATE_MILLING.get();
            }
        } catch (Exception e) {
            return true; // 加载早期默认放行
        }
        return true;
    }

    public static class Serializer implements IConditionSerializer<ConfigCondition> {
        @Override
        public void write(JsonObject json, ConfigCondition value) {
            json.addProperty("recipe_type", value.recipeType);
        }

        @Override
        public ConfigCondition read(JsonObject json) {
            String type = GsonHelper.getAsString(json, "recipe_type", "unknown");
            return new ConfigCondition(type);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}