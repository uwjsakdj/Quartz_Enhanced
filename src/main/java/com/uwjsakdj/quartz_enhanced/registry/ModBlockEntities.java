package com.uwjsakdj.quartz_enhanced.registry;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import com.uwjsakdj.quartz_enhanced.block.entity.ResonanceWorkbenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Quartz_Enhanced.MOD_ID);

    public static final RegistryObject<BlockEntityType<ResonanceWorkbenchBlockEntity>> RESONANCE_WORKBENCH =
            BLOCK_ENTITIES.register("resonance_workbench",
                    () -> BlockEntityType.Builder.of(ResonanceWorkbenchBlockEntity::new,
                            ModBlocks.RESONANCE_WORKBENCH.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}