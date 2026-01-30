package com.daniels0k.industry_mod.screen;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.screen.cable_winder.CableWinderMenu;
import com.daniels0k.industry_mod.screen.coal_generator.CoalGeneratorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IndustryMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CableWinderMenu>> CABLE_WINDER_MENU = registerMenuType("cable_winder_menu", CableWinderMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<CoalGeneratorMenu>> COAL_GENERATOR_MENU = registerMenuType("coal_generator_menu", CoalGeneratorMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
