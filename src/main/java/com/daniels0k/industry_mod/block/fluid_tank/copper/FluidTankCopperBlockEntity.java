package com.daniels0k.industry_mod.block.fluid_tank.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankCopperBlockEntity extends FluidTankBlockEntity {
    public FluidTankCopperBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FLUID_TANK_COPPER.get(), pos, blockState, 10000);
    }
}
