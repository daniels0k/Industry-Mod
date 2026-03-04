package com.daniels0k.industry_mod.block.fluid_pipe.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_pipe.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Set;

public class CopperFluidPipeBlockEntity extends FluidPipeBlockEntity {
    public CopperFluidPipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COPPER_PIPE_FLUID.get(), pos, blockState, 100);
        this.invalidFluids = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }
}
