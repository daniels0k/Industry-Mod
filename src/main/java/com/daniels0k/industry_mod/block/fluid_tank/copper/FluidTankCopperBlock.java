package com.daniels0k.industry_mod.block.fluid_tank.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlock;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidTankTankBlockCopper extends FluidTankBlock {
    public static MapCodec<FluidTankTankBlockCopper> CODEC = simpleCodec(FluidTankTankBlockCopper::new);

    public FluidTankTankBlockCopper(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FluidTankBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FluidTankCopperTankBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.VAULT_FLUID_COPPER.get(),
                (levelTick, blockPos, blockState, blockEntity) -> blockEntity.tick(levelTick, blockPos, blockState, blockEntity));
    }
}
