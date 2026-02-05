package com.daniels0k.industry_mod.block;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.api.capabilities.EnergyCapabilities;
import com.daniels0k.industry_mod.block.cable_winder.CableWinderBlockEntity;
import com.daniels0k.industry_mod.block.coal_generator.CoalGeneratorBlockEntity;
import com.daniels0k.industry_mod.block.connector.WireConnectBlockEntity;
import com.daniels0k.industry_mod.block.crusher.CrusherBlockEntity;
import com.daniels0k.industry_mod.block.vault_energy.enertick.VaultEnertickBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IndustryMod.MOD_ID);

    public static final Supplier<BlockEntityType<WireConnectBlockEntity>> WIRE_COPPER_CONNECT = BLOCK_ENTITIES.register("wire_copper_connect",
            registryName -> new BlockEntityType<>(WireConnectBlockEntity::new, ModBlocks.WIRE_COPPER_CONNECT.get()));

    public static final Supplier<BlockEntityType<CableWinderBlockEntity>> CABLE_WINDER = BLOCK_ENTITIES.register("cable_winder",
            registryName -> new BlockEntityType<>(CableWinderBlockEntity::new, ModBlocks.CABLE_WINDER.get()));

    public static final Supplier<BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR = BLOCK_ENTITIES.register("coal_generator",
            registryName -> new BlockEntityType<>(CoalGeneratorBlockEntity::new, ModBlocks.COAL_GENERATOR.get()));

    public static final Supplier<BlockEntityType<VaultEnertickBlockEntity>> VAULT_ENERTICK = BLOCK_ENTITIES.register("enertick_vault",
            registryName -> new BlockEntityType<>(VaultEnertickBlockEntity::new, ModBlocks.VAULT_ENERTICK.get()));

    public static final Supplier<BlockEntityType<CrusherBlockEntity>> CRUSHER = BLOCK_ENTITIES.register("crusher",
            registryName -> new BlockEntityType<>(CrusherBlockEntity::new, ModBlocks.CRUSHER.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
        eventBus.addListener(ModBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EnergyCapabilities.EnerTickStorage.BLOCK,
                WIRE_COPPER_CONNECT.get(),
                (blockEntity, side) -> blockEntity.energyET
        );
    }
}
