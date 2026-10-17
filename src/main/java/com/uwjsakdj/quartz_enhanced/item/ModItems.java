package com.uwjsakdj.quartz_enhanced.item;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.init.tool.NatureQuartzSword;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.uwjsakdj.quartz_enhanced.init.tool.*;
import com.uwjsakdj.quartz_enhanced.init.food.*;
import net.minecraft.world.item.ArmorItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Quartz_Enhanced.MOD_ID);

//第一部分
    /*public static final RegistryObject<Item> TEST =
            ITEMS.register("test", () -> new Item(new Item.Properties()));*/
    public static final RegistryObject<Item> REINFORCED_QUARTZ_INGOT =
            ITEMS.register("reinforced_quartz_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNSMELT_QUARTZ_MIXTURE =
            ITEMS.register("unsmelted_quartz_mixture", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUARTZ_APPLE =
            ITEMS.register("quartz_apple", () -> new Item(new Item.Properties().food(Quartz_Apple.QUARTZ_APPLE_FOOD)));
    public static final RegistryObject<Item> REINFORCED_QUARTZ_SWORD =
            ITEMS.register("reinforced_quartz_sword", ReinforcedQuartzSword::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_PICKAXE =
            ITEMS.register("reinforced_quartz_pickaxe", ReinforcedQuartzPickaxe::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_AXE =
            ITEMS.register("reinforced_quartz_axe", ReinforcedQuartzAxe::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_HOE =
            ITEMS.register("reinforced_quartz_hoe", ReinforcedQuartzHoe::new);
    public static final RegistryObject<Item> QUARTZ_JADE_MAGIC_WAND =
            ITEMS.register("quartz_jade_magic_wand", QuartzJadeMagicWand::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_SHOVEL =
            ITEMS.register("reinforced_quartz_shovel", ReinforcedQuartzShovel::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_NUGGET =
            ITEMS.register("reinforced_quartz_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUARTZ_JADE =
            ITEMS.register("quartz_jade", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FINE_QUARTZ_JADE =
            ITEMS.register("fine_quartz_jade", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> QUARTZ_REFRESHING_SWEET_SOUP =
            ITEMS.register("quartz_refreshing_sweet_soup", Quartz_Refreshing_Sweet_Soup::new);
    public static final RegistryObject<Item> QUARTZ_NETHER_FUNGUS_AROMATIC_STEW =
            ITEMS.register("quartz_nether_fungus_aromatic_stew", Quartz_Nether_Fungus_Aromatic_Stew::new);
    public static final RegistryObject<Item> QUARTZ_HONEY_PUDDING =
            ITEMS.register("quartz_honey_pudding", Quartz_Honey_Pudding::new);
    public static final RegistryObject<Item> REINFORCED_QUARTZ_HELMET =
            ITEMS.register("reinforced_quartz_helmet",
            () -> new ModArmorItem(ModArmorMaterials.REINFORCED_QUARTZ, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> REINFORCED_QUARTZ_CHESTPLATE =
            ITEMS.register("reinforced_quartz_chestplate",
            () -> new ModArmorItem(ModArmorMaterials.REINFORCED_QUARTZ, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> REINFORCED_QUARTZ_LEGGINGS =
            ITEMS.register("reinforced_quartz_leggings",
            () -> new ModArmorItem(ModArmorMaterials.REINFORCED_QUARTZ, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> REINFORCED_QUARTZ_BOOTS =
            ITEMS.register("reinforced_quartz_boots",
            () -> new ModArmorItem(ModArmorMaterials.REINFORCED_QUARTZ, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> REINFORCED_QUARTZ_KNIFE =
            ITEMS.register("reinforced_quartz_knife", ReinforcedQuartzKnife::new);
    public static final RegistryObject<Item> QUARTZ_POWDER =
            ITEMS.register("quartz_powder", () -> new Item(new Item.Properties()));

    //第二部分
    public static final RegistryObject<Item> RESONANCE_STONE =
            ITEMS.register("resonance_stone", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> NATURE_RESONANCE_STONE =
            ITEMS.register("nature_resonance_stone", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> NATURE_QUARTZ_INGOT =
            ITEMS.register("nature_quartz_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NATURE_QUARTZ_NUGGET =
            ITEMS.register("nature_quartz_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NATURE_QUARTZ_SWORD =
            ITEMS.register("nature_quartz_sword", NatureQuartzSword::new);
    public static final RegistryObject<Item> NATURE_QUARTZ_AXE =
            ITEMS.register("nature_quartz_axe", NatureQuartzAxe::new);
    public static final RegistryObject<Item> NATURE_QUARTZ_PICKAXE =
            ITEMS.register("nature_quartz_pickaxe", NatureQuartzPickaxe::new);
    public static final RegistryObject<Item> NATURE_QUARTZ_SHOVEL =
            ITEMS.register("nature_quartz_shovel", NatureQuartzShovel::new);
    public static final RegistryObject<Item> NATURE_QUARTZ_HOE =
            ITEMS.register("nature_quartz_hoe", NatureQuartzHoe::new);
    public static final RegistryObject<Item> NATURE_QUARTZ_KNIFE =
            ITEMS.register("nature_quartz_knife", NatureQuartzKnife::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}