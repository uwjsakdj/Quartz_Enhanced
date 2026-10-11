package com.uwjsakdj.quartz_enhanced.block;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.uwjsakdj.quartz_enhanced.block.ResonanceWorkbenchBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Quartz_Enhanced.MOD_ID);



    public  static final RegistryObject<Block> REINFORCED_QUARTZ_BLOCK =
            registerBlock("reinforced_quartz_block",
                    () -> new Block(BlockBehaviour.Properties.of().strength(5.0F,6.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> RESONANCE_WORKBENCH =
            registerBlock("resonance_workbench",
                    () -> new ResonanceWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));



    private static <T extends Block> void registerBlockItems(String name, RegistryObject<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends  Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> blocks = BLOCKS.register(name,block);
        registerBlockItems(name, blocks);
        return blocks;
    }
    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
