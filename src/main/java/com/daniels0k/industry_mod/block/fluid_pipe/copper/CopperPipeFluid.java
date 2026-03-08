package com.daniels0k.industry_mod.block.fluid_pipe.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_pipe.PipeFluid;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CopperPipeFluid extends PipeFluid {
    public static final MapCodec<CopperPipeFluid> CODEC = simpleCodec(CopperPipeFluid::new);

    public CopperPipeFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends PipeFluid> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CopperPipeFluidBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.COPPER_PIPE_FLUID.get(),
                (levelTick, blockPos, blockState, copperFluidPipeBlockEntity) ->
                        copperFluidPipeBlockEntity.tick(levelTick, blockPos, blockState, copperFluidPipeBlockEntity));
    }
}
