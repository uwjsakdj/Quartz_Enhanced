package com.uwjsakdj.quartz_enhanced.registry;

import com.uwjsakdj.quartz_enhanced.Quartz_Enhanced;
import com.uwjsakdj.quartz_enhanced.menu.ResonanceWorkbenchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Quartz_Enhanced.MOD_ID);

    public static final RegistryObject<MenuType<ResonanceWorkbenchMenu>> RESONANCE_WORKBENCH =
            MENUS.register("resonance_workbench",
                    () -> IForgeMenuType.create(ResonanceWorkbenchMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}