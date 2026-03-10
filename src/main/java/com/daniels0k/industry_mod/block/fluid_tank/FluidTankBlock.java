package com.daniels0k.industry_mod.block.fluid_tank;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 14, 0, 16, 16, 16),
            Block.box(0, 0, 0, 2, 16, 16),
            Block.box(14, 0, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 2),
            Block.box(0, 0, 14, 16, 16, 16));

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
            int totalSize = 1;

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

                Set<BlockPos> struct = origin.getConnectedTanks(level);
                totalSize += struct.size();
            }

            if(!compatible) {
                level.destroyBlock(pos, true);
                return;
            }

            Set<BlockPos> union = new HashSet<>();
            for(FluidTankBlockEntity origin : neighborOrigins) {
                union.addAll(origin.getConnectedTanks(level));
            }
            union.add(pos);

            int uMinX = union.stream().mapToInt(BlockPos::getX).min().getAsInt();
            int uMaxX = union.stream().mapToInt(BlockPos::getX).max().getAsInt();
            int uMinY = union.stream().mapToInt(BlockPos::getY).min().getAsInt();
            int uMaxY = union.stream().mapToInt(BlockPos::getY).max().getAsInt();
            int uMinZ = union.stream().mapToInt(BlockPos::getZ).min().getAsInt();
            int uMaxZ = union.stream().mapToInt(BlockPos::getZ).max().getAsInt();

            boolean withinLimits = (uMaxX - uMinX) < FluidTankBlockEntity.MAX_WIDTH
                    && (uMaxY - uMinY) < FluidTankBlockEntity.MAX_HEIGHT
                    && (uMaxZ - uMinZ) < FluidTankBlockEntity.MAX_WIDTH;

            if(!withinLimits) {
                level.destroyBlock(pos, true);
                return;
            }

            FluidTankBlockEntity newOrigin = neighborOrigins.stream()
                    .min(Comparator.comparing((FluidTankBlockEntity o) -> o.getBlockPos().getX())
                            .thenComparing(o -> o.getBlockPos().getY())
                            .thenComparing(o -> o.getBlockPos().getZ()))
                    .orElse(thisTank);

            FluidTank mainTank = newOrigin.getTank();
            if(mainTank != null) {
                int totalFluid = mainTank.getFluidAmount();
                for(FluidTankBlockEntity origin : neighborOrigins) {
                    if(origin != newOrigin && origin.getTank() != null) {
                        totalFluid += origin.getTank().getFluidAmount();
                    }
                }

                for(FluidTankBlockEntity origin : neighborOrigins) {
                    if(origin != newOrigin && origin.getTank() != null) {
                        origin.getTank().setFluid(FluidStack.EMPTY);
                        origin.setChanged();
                    }
                }

                if(totalFluid > 0 && commonFluid != null) {
                    mainTank.setFluid(new FluidStack(commonFluid, totalFluid));
                }
            }

            newOrigin.rebuildStructure(level);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);

            if(be instanceof FluidTankBlockEntity tank && tank.isOriginBlock()) {
                // Obtener toda la estructura actual (incluyendo este bloque)
                Set<BlockPos> structure = tank.getConnectedTanks(level);
                if(structure.size() > 1) {
                    BlockPos newOriginPos = structure.stream()
                            .filter(p -> !p.equals(pos))
                            .min(Comparator.comparing(BlockPos::asLong))
                            .orElse(null);

                    if (newOriginPos != null) {
                        BlockEntity newBe = level.getBlockEntity(newOriginPos);

                        if (newBe instanceof FluidTankBlockEntity newOriginTank) {
                            if (newOriginTank.getTank() == null) {
                                newOriginTank.setTank(newOriginTank.getBaseCapacity());
                            }

                            FluidStack fluidStack = tank.getTank().getFluid().copy();
                            newOriginTank.getTank().setFluid(fluidStack);
                            tank.getTank().setFluid(FluidStack.EMPTY);

                            for (BlockPos p : structure) {
                                if (!p.equals(newOriginPos) && !p.equals(pos)) {
                                    BlockEntity be2 = level.getBlockEntity(p);
                                    if (be2 instanceof FluidTankBlockEntity te) {
                                        te.setOriginPos(newOriginPos);
                                        te.setOriginBlock(false);
                                        te.setTank(null);
                                        te.setChanged();
                                    }
                                }
                            }

                            newOriginTank.setOriginBlock(true);
                            newOriginTank.setOriginPos(newOriginPos);
                            newOriginTank.setChanged();
                        }
                    }
                }
            }

            Set<BlockPos> neighbors = new HashSet<>();
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);

                if (level.getBlockEntity(neighborPos) instanceof FluidTankBlockEntity) {
                    neighbors.add(neighborPos);
                }
            }

            boolean result = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);

            for(BlockPos neighborPos : neighbors) {
                BlockEntity beAfter = level.getBlockEntity(neighborPos);

                if (beAfter instanceof FluidTankBlockEntity tankAfter) {
                    tankAfter.rebuildStructure(level);
                }
            }
            return result;
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
    protected int getLightBlock(BlockState state) {
        super.getLightBlock(state);
        boolean hasGlass = state.getValue(NORTH) || state.getValue(SOUTH) || state.getValue(EAST) || state.getValue(WEST)
                || state.getValue(UP) || state.getValue(DOWN);
        return hasGlass ? 0 : 1;
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
