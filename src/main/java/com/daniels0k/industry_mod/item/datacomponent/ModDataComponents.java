package com.daniels0k.industry_mod.item.datacomponent;

import com.daniels0k.industry_mod.IndustryMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IndustryMod.MOD_ID);

    public static final Supplier<DataComponentType<Boolean>> CHANGER_MODE = DATA_COMPONENTS.registerComponentType("changer_mode", builder ->
            builder.persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<RouteDataComponent>> ROUTE_DATA = DATA_COMPONENTS.registerComponentType("route_wire",  builder ->
            builder.persistent(RouteDataComponent.CODEC)
                    .networkSynchronized(RouteDataComponent.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
