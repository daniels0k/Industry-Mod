package com.daniels0k.industry_mod.block;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.block.cable_winder.CableWinderBlockEntity;
import com.daniels0k.industry_mod.block.coal_generator.CoalGeneratorBlockEntity;
import com.daniels0k.industry_mod.block.connector.WireConnectBlockEntity;
import com.daniels0k.industry_mod.block.crusher.CrusherBlockEntity;
import com.daniels0k.industry_mod.block.fluid_pipe.copper.CopperPipeFluidBlockEntity;
import com.daniels0k.industry_mod.block.pumps.basic.BasicPumpBlockEntity;
import com.daniels0k.industry_mod.block.vault_energy.enertick.VaultEnertickBlockEntity;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlockEntity;
import com.daniels0k.industry_mod.block.fluid_tank.copper.FluidTankCopperBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
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

    public static final Supplier<BlockEntityType<BasicPumpBlockEntity>> BASIC_PUMP = BLOCK_ENTITIES.register("basic_pump",
            registryName -> new BlockEntityType<>(BasicPumpBlockEntity::new, ModBlocks.BASIC_PUMP.get()));

    //Fluids
    public static final Supplier<BlockEntityType<CopperPipeFluidBlockEntity>> COPPER_PIPE_FLUID = BLOCK_ENTITIES.register("copper_pipe_fluid",
            registryName -> new BlockEntityType<>(CopperPipeFluidBlockEntity::new, ModBlocks.COPPER_PIPE_FLUID.get()));

    public static final Supplier<BlockEntityType<FluidTankBlockEntity>> VAULT_FLUID_COPPER = BLOCK_ENTITIES.register("vault_fluid_copper",
            registryName -> new BlockEntityType<>(FluidTankCopperBlockEntity::new, ModBlocks.VAULT_FLUID_COPPER.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
