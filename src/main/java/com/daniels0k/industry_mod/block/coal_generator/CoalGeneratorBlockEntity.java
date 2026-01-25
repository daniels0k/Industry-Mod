package com.daniels0k.industry_mod.block.coal_generator;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CoalGeneratorBlockEntity extends BlockEntity {
    public CoalGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState) {

    }
}
