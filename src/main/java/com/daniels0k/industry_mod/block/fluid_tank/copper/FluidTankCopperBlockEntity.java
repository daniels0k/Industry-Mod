package com.daniels0k.industry_mod.block.fluid_tank.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankCopperTankBlockEntity extends FluidTankBlockEntity {
    public FluidTankCopperTankBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.VAULT_FLUID_COPPER.get(), pos, blockState, 10000);
    }
}
