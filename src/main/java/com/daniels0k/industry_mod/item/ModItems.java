package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.item.datacomponent.ModDataComponents;
import com.daniels0k.industry_mod.item.datacomponent.RouteDataComponent;
import com.daniels0k.industry_mod.item.wires.WireCopper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Optional;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IndustryMod.MOD_ID);

    public static final DeferredItem<Item> COPPER_WIRE = ITEMS.register("wire_copper",
            registryName -> new WireCopper(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(16)));

    public static final DeferredItem<Item> CONNECTION_CHANGER = ITEMS.register("connection_changer",
            registryName -> new ConnectionChanger(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .component(ModDataComponents.CHANGER_MODE, false)
                    .stacksTo(1)));

    public static final DeferredItem<Item> CABLE_ROLL = ITEMS.register("cable_roll", registryName -> new Item(
            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)));

    public static final DeferredItem<Item> CABLE_ROLL_COPPER = ITEMS.register("cable_roll_copper",
            registryName -> new CableRollCopper(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, registryName))
                    .stacksTo(1)
                    .component(ModDataComponents.ROUTE_DATA, new RouteDataComponent(Optional.empty(), Optional.empty(), "copper", 6, 1.0f, 3.5f))
                    .durability(1000)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
