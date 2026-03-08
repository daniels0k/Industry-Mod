package com.daniels0k.industry_mod.block.fluid_pipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public abstract class PipeFluid extends BaseEntityBlock {
    public static BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static BooleanProperty WEST = BlockStateProperties.WEST;
    public static BooleanProperty EAST = BlockStateProperties.EAST;
    public static BooleanProperty UP = BlockStateProperties.UP;
    public static BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static BooleanProperty NORTH_INPUT = BooleanProperty.create("north_in");
    public static BooleanProperty SOUTH_INPUT = BooleanProperty.create("south_in");
    public static BooleanProperty WEST_INPUT = BooleanProperty.create("west_in");
    public static BooleanProperty EAST_INPUT = BooleanProperty.create("east_in");
    public static BooleanProperty UP_INPUT = BooleanProperty.create("up_in");
    public static BooleanProperty DOWN_INPUT = BooleanProperty.create("down_in");

    protected static final VoxelShape SHAPE_CENTER = box(5.0d, 5.0d, 5.0d, 11.0d, 11.0d, 11.0d);
    protected static final VoxelShape SHAPE_UP = box(6.0d, 11.0d, 6.0d, 10.0d, 16.0d, 10.0d);
    protected static final VoxelShape SHAPE_DOWN = box(6.0d, 0.0d, 6.0d, 10.0d, 5.0d, 10.0d);
    protected static final VoxelShape SHAPE_NORTH = box(6.0d, 6.0d, 0.0d, 10.0d, 10.0d, 5.0d);
    protected static final VoxelShape SHAPE_SOUTH = box(6.0d, 6.0d, 11.0d, 10.0d, 10.0d, 16.0d);
    protected static final VoxelShape SHAPE_WEST = box(0.0d, 6.0d, 6.0d, 5.0d, 10.0d, 10.0d);
    protected static final VoxelShape SHAPE_EAST = box(11.0d, 6.0d, 6.0d, 16.0d, 10.0d, 10.0d);

    private static final VoxelShape[] SHAPES_BY_DIRECTION = new VoxelShape[] {SHAPE_DOWN, SHAPE_UP, SHAPE_NORTH, SHAPE_SOUTH, SHAPE_WEST, SHAPE_EAST};

    public PipeFluid(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false)
            .setValue(UP, false)
            .setValue(DOWN, false));
    }

    protected abstract MapCodec<? extends PipeFluid> codec();

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape combinedShape = SHAPE_CENTER;

        for(Direction dir : Direction.values()) {
            if(state.getValue(getDirectionProperty(dir))) {
                combinedShape = Shapes.or(combinedShape, SHAPES_BY_DIRECTION[dir.ordinal()]);
            }
        }

        return combinedShape;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
        builder.add(NORTH_INPUT, SOUTH_INPUT, WEST_INPUT, EAST_INPUT, UP_INPUT, DOWN_INPUT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = migrateConnections(context.getLevel(), context.getClickedPos());

        for(Direction dir : Direction.values()) {
            state = state.setValue(getInputDirectionProperty(dir), false);
        }

        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return migrateConnections(level, pos);
    }

    private BlockState migrateConnections(LevelReader level, BlockPos pos) {
        BlockState state = this.defaultBlockState()
                .setValue(NORTH, canConnection(level, pos.north(), Direction.NORTH))
                .setValue(SOUTH, canConnection(level, pos.south(), Direction.SOUTH))
                .setValue(WEST, canConnection(level, pos.west(), Direction.WEST))
                .setValue(EAST, canConnection(level, pos.east(), Direction.EAST))
                .setValue(UP, canConnection(level, pos.above(), Direction.UP))
                .setValue(DOWN, canConnection(level, pos.below(), Direction.DOWN));

        if(level.getBlockState(pos).getBlock() instanceof PipeFluid) {
            BlockState oldState = level.getBlockState(pos);
            for(Direction dir : Direction.values()) {
                BooleanProperty inputProp = getInputDirectionProperty(dir);
                boolean wasInput = oldState.getValue(inputProp);
                boolean hasConnection = state.getValue(getDirectionProperty(dir));

                state = state.setValue(inputProp, wasInput && hasConnection);
            }
        }

        return state;
    }

    public static BooleanProperty getInputDirectionProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH_INPUT;
            case SOUTH -> SOUTH_INPUT;
            case WEST -> WEST_INPUT;
            case EAST -> EAST_INPUT;
            case UP -> UP_INPUT;
            case DOWN -> DOWN_INPUT;
        };
    }

    public static BooleanProperty getDirectionProperty(Direction dir) {
        return switch (dir) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    private boolean canConnection(LevelReader levelReader, BlockPos neighborPos, Direction side) {
        if(levelReader.getBlockState(neighborPos).getBlock() instanceof PipeFluid) {
            return true;
        }

        if(levelReader instanceof Level level) {
            return level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, side.getOpposite()) != null;
        }

        return false;
    }

    public static boolean isInput(BlockState blockState, Direction dir) {
        return blockState.getValue(getInputDirectionProperty(dir));
    }

    public static boolean isOutput(BlockState blockState, Direction dir) {
        return blockState.getValue(getDirectionProperty(dir)) && !isInput(blockState, dir);
    }
}
