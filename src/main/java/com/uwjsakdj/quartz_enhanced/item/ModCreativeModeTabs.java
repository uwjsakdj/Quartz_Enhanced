package com.uwjsakdj.quartz_enhanced.item;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Quartz_Enhanced.MOD_ID);

    public static final RegistryObject<CreativeModeTab> QUARTZ_ENHANCED_TAB =
            CREATIVE_MODE_TABS.register("quartz_enhanced_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.REINFORCED_QUARTZ_INGOT.get()))
                    .title(Component.translatable("itemGroup.quartz_enhanced_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        //第一部分
                        pOutput.accept(ModItems.UNSMELT_QUARTZ_MIXTURE.get());
                        pOutput.accept(ModBlocks.REINFORCED_QUARTZ_BLOCK.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_INGOT.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_NUGGET.get());
                        pOutput.accept(ModItems.QUARTZ_APPLE.get());
                        pOutput.accept(ModItems.QUARTZ_REFRESHING_SWEET_SOUP.get());
                        pOutput.accept(ModItems.QUARTZ_NETHER_FUNGUS_AROMATIC_STEW.get());
                        pOutput.accept(ModItems.QUARTZ_HONEY_PUDDING.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_SWORD.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_PICKAXE.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_AXE.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_HOE.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_SHOVEL.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_KNIFE.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_HELMET.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_CHESTPLATE.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_LEGGINGS.get());
                        pOutput.accept(ModItems.REINFORCED_QUARTZ_BOOTS.get());
                        pOutput.accept(ModItems.QUARTZ_JADE_MAGIC_WAND.get());
                        pOutput.accept(ModItems.QUARTZ_JADE.get());
                        pOutput.accept(ModItems.FINE_QUARTZ_JADE.get());
                        pOutput.accept(ModItems.QUARTZ_POWDER.get());
                        //第二部分
                        pOutput.accept(ModBlocks.RESONANCE_WORKBENCH.get());
                        pOutput.accept(ModItems.RESONANCE_STONE.get());
                        pOutput.accept(ModItems.NATURE_RESONANCE_STONE.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_INGOT.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_NUGGET.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_SWORD.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_AXE.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_PICKAXE.get());
                        pOutput.accept(ModItems.NATURE_QUARTZ_SHOVEL.get());

                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

