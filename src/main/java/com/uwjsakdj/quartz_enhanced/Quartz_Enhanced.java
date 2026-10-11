package com.uwjsakdj.quartz_enhanced;

import com.mojang.logging.LogUtils;
import com.uwjsakdj.quartz_enhanced.block.ModBlocks;
import com.uwjsakdj.quartz_enhanced.init.tool.NatureQuartzPickaxe;
import com.uwjsakdj.quartz_enhanced.item.ModCreativeModeTabs;
import com.uwjsakdj.quartz_enhanced.item.ModItems;
import com.uwjsakdj.quartz_enhanced.recipe.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import com.uwjsakdj.quartz_enhanced.registry.ModBlockEntities;
import com.uwjsakdj.quartz_enhanced.registry.ModMenuTypes;
import com.uwjsakdj.quartz_enhanced.client.screen.ResonanceWorkbenchScreen;
import net.minecraft.client.gui.screens.MenuScreens;

@Mod(Quartz_Enhanced.MOD_ID)
public class Quartz_Enhanced {

    public static final String MOD_ID = "quartz_enhanced";
    private static final Logger LOGGER = LogUtils.getLogger();

    // ==================== 声音事件注册 ====================
    // ❶ 创建声音注册表
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    // ❷ 注册一个名叫 quartz_crush 的声音事件
    public static final RegistryObject<SoundEvent> QUARTZ_CRUSH =
            SOUNDS.register("quartz_crush",
                    () -> SoundEvent.createFixedRangeEvent(
                            new ResourceLocation(MOD_ID, "quartz_crush"), 16
                    ));
    // ====================================================

    public Quartz_Enhanced(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        SOUNDS.register(modEventBus);   // ❸ 把声音注册表也挂上总线

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(NatureQuartzPickaxe.Events.class);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Quartz Enhanced common setup complete");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenuTypes.RESONANCE_WORKBENCH.get(), ResonanceWorkbenchScreen::new);
            });
        }
    }
}
