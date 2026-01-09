package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IndustryMod.MOD_ID);

    public static final Supplier<CreativeModeTab> INDUSTRY_MOD_TAB = TABS.register("industry_mod",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.CASE_MACHINE_BASIC))
                    .title(Component.translatable("creativetab.industry_mod.industry_mod_tab"))
                    .displayItems(
                            ((itemDisplayParameters, output) -> {
                                output.accept(ModBlocks.CASE_MACHINE_BASIC);

                                output.accept(ModItems.CONNECTION_CHANGER);
                                output.accept(ModItems.COPPER_WIRE);
                                output.accept(ModItems.CABLE_ROLL);
                                output.accept(ModItems.CABLE_ROLL_COPPER);

                                output.accept(ModBlocks.WIRE_COPPER_CONNECT);
                                output.accept(ModBlocks.CABLE_WINDER);
                            }))
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
