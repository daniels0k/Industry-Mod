package com.daniels0k.industry_mod.block.pumps.basic;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class BasicPump extends BaseEntityBlock {
    public static MapCodec<BasicPump> CODEC = simpleCodec(BasicPump::new);

    public static EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static BooleanProperty EAST = BlockStateProperties.EAST;
    public static BooleanProperty WEST = BlockStateProperties.WEST;
    public static BooleanProperty UP = BlockStateProperties.UP;
    public static BooleanProperty DOWN = BlockStateProperties.DOWN;

    public BasicPump(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
        .setValue(NORTH, false)
        .setValue(EAST, false)
        .setValue(SOUTH, false)
        .setValue(WEST, false)
        .setValue(UP, false)
        .setValue(DOWN, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(DOWN, canConnectedTo(context.getLevel(), context.getClickedPos().below(), Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING,  NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    private boolean canConnectedTo(Level level, BlockPos pos, Direction side) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side) != null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BasicPumpBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.BASIC_PUMP.get(),
                (levelTick, blockPos, blockState, blockEntity) -> blockEntity.tick(levelTick, blockPos, blockState, blockEntity));
    }
}
