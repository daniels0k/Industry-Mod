package com.daniels0k.industry_mod.block.pipe_fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PipeFluid extends Block {
    public static BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static BooleanProperty WEST = BlockStateProperties.WEST;
    public static BooleanProperty EAST = BlockStateProperties.EAST;
    public static BooleanProperty UP = BlockStateProperties.UP;
    public static BooleanProperty DOWN = BlockStateProperties.DOWN;

    public PipeFluid(Properties properties) {
        super(properties);
    }

    
}
