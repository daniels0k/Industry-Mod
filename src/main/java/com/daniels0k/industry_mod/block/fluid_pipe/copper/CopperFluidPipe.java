package com.daniels0k.industry_mod.block.fluid_pipe.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_pipe.FluidPipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CopperFluidPipe extends FluidPipe {
    public static final MapCodec<CopperFluidPipe> CODEC = simpleCodec(CopperFluidPipe::new);

    public CopperFluidPipe(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FluidPipe> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CopperFluidPipeBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.COPPER_PIPE_FLUID.get(),
                (levelTick, blockPos, blockState, copperFluidPipeBlockEntity) ->
                        copperFluidPipeBlockEntity.tick(levelTick, blockPos, blockState, copperFluidPipeBlockEntity));
    }
}
