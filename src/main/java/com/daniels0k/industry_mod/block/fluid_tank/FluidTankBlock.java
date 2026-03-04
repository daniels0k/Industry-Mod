package com.daniels0k.industry_mod.block.fluid_tank;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public abstract class FluidTankBlock extends BaseEntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public FluidTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();

        BlockState state = defaultBlockState();
        for(Direction dir : Direction.values()) {
            BlockPos neighbor = blockPos.relative(dir);
            boolean connected = false;
            BlockEntity blockEntity = level.getBlockEntity(neighbor);
            if(blockEntity instanceof FluidTankBlockEntity tank && tank.getTankClass() == this.getTankClass()) {
                connected = true;
            }
            state = state.setValue(getDirectionProperty(dir), connected);
        }

        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if(level.isClientSide()) return;

        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof FluidTankBlockEntity thisTank)) return;

        Set<FluidTankBlockEntity> neighborOrigins = new HashSet<>();
        for(Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockEntity neighborBE = level.getBlockEntity(neighbor);
            if(neighborBE instanceof FluidTankBlockEntity neighborTank) {
                FluidTankBlockEntity origin = neighborTank.getOrigin(level);
                if(origin != null) {
                    neighborOrigins.add(origin);
                }
            }
        }

        if(neighborOrigins.isEmpty()) {
            thisTank.rebuildStructure(level);
        } else if(neighborOrigins.size() == 1) {
            FluidTankBlockEntity origin = neighborOrigins.iterator().next();
            if(origin.canConnectTo(level, pos)) {
                origin.rebuildStructure(level);
            } else {
                level.destroyBlock(pos, true);
            }
        } else {
            boolean compatible = true;
            Fluid commonFluid = null;

            for(FluidTankBlockEntity origin : neighborOrigins) {
                FluidTank tank = origin.getTank();
                if(tank != null && !tank.isEmpty()) {
                    if(commonFluid == null) {
                        commonFluid = tank.getFluid().getFluid();
                    } else if(commonFluid != tank.getFluid().getFluid()) {
                        compatible = false;
                        break;
                    }
                }
            }

            if(compatible) {
                FluidTankBlockEntity newOrigin = neighborOrigins.stream()
                        .min(Comparator.comparing((FluidTankBlockEntity o) -> o.getBlockPos().getX())
                                .thenComparing((FluidTankBlockEntity o) -> o.getBlockPos().getY())
                                .thenComparing((FluidTankBlockEntity o) -> o.getBlockPos().getZ()))
                        .orElse(thisTank);

                newOrigin.rebuildStructure(level);
            } else {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if(!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof FluidTankBlockEntity tank) {
                BlockPos oringinPos = tank.getOriginPos();
                if(oringinPos != null && level.getBlockEntity(oringinPos) instanceof FluidTankBlockEntity origin) {
                    origin.rebuildStructure(level);
                }
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    public static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH ->  NORTH;
            case SOUTH ->  SOUTH;
            case EAST ->  EAST;
            case WEST ->  WEST;
            case UP ->  UP;
            case DOWN ->  DOWN;
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if(level instanceof Level realLevel) {
            boolean connected = false;
            BlockEntity be = realLevel.getBlockEntity(pos);
            BlockEntity neighborBE = realLevel.getBlockEntity(neighborPos);
            if(be instanceof FluidTankBlockEntity thisTank && neighborBE instanceof FluidTankBlockEntity otherTank) {
                connected = thisTank.getOriginPos().equals(otherTank.getOriginPos())
                        && thisTank.getTankClass() == otherTank.getTankClass();
            }
            return state.setValue(getDirectionProperty(direction), connected);
        }

        return state;
    }

    protected abstract MapCodec<? extends FluidTankBlock> codec();

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state) {
        boolean hasGlass = state.getValue(NORTH) || state.getValue(SOUTH) || state.getValue(EAST) || state.getValue(WEST)
                || state.getValue(UP) || state.getValue(DOWN);
        return hasGlass ? 1 : 3;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
            return tank.getLightFluid();
        }
        return 0;
    }

    protected abstract Class<? extends FluidTankBlockEntity> getTankClass();
}
