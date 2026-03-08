package com.daniels0k.industry_mod.block.fluid_tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class FluidTankBlockEntity extends BlockEntity {
    //Serializers
    private FluidTank tank;
    private BlockPos originBlock;
    private boolean isOriginBlock;

    //Tank properties
    protected Set<Fluid> invalidFluids;
    public static final int MAX_WIDTH = 3;
    public static final int MAX_HEIGHT = 6;
    private final int baseCapacity;

    //Multiblock cache
    private Set<BlockPos> cachedStructure;
    private int minX, maxX, minY, maxY, minZ, maxZ;

    public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int capacity) {
        super(type, pos, blockState);
        this.baseCapacity = capacity;
        this.tank = createTank(baseCapacity);
        this.isOriginBlock = true;
        this.originBlock = pos;
        this.invalidFluids = new HashSet<>();
        this.cachedStructure = new HashSet<>();
        this.cachedStructure.add(pos);
        updateBounds(pos);
    }

    private FluidTank createTank(int capacity) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
                if(level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (getLevel() != null && invalidFluids != null) {
                    if (invalidFluids.contains(resource.getFluid())) {
                        if (!getLevel().isClientSide() && action.execute()) {
                            getLevel().destroyBlock(worldPosition, false);
                            getLevel().explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1f, Level.ExplosionInteraction.NONE);
                        }
                        return 0;
                    }
                }
                return super.fill(resource, action);
            }
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("originPos", originBlock.asLong());
        output.putBoolean("isOriginBlock", isOriginBlock);
        if(isOriginBlock && tank != null) {
            tank.serialize(output);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        originBlock = BlockPos.of(input.getLongOr("originPos", 0));
        isOriginBlock = input.getBooleanOr("isOriginBlock", false);

        if(isOriginBlock) {
            if(tank == null) {
                tank = createTank(baseCapacity);
            }
            tank.deserialize(input);
        }
        cachedStructure = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidTankBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        if(!isOriginBlock) return;
        if(tank == null || tank.isEmpty()) return;

        BlockPos belowPos = pos.below();
        BlockEntity be = level.getBlockEntity(belowPos);
        if(be instanceof FluidTankBlockEntity belowTank) {
            if(belowTank.getTankClass() != this.getTankClass()) return;
            FluidTank belowOriginTank = belowTank.getTankOrigin(level);
            if(belowOriginTank != null && belowOriginTank != tank) {
                transferFluid(tank, belowOriginTank);
            }
        }
    }

    private void transferFluid(FluidTank sourceTank, FluidTank targetTank) {
        if(sourceTank.isEmpty()) return;
        FluidStack sourceFluid = sourceTank.getFluid();

        if(!targetTank.isEmpty() && !targetTank.getFluid().equals(sourceFluid)) return;
        int space = targetTank.getSpace();
        if(space <= 0) return;

        int toMove = Math.min(sourceTank.getFluidAmount(), space);
        FluidStack toDrain = new FluidStack(sourceFluid.getFluid(), toMove);
        int accepted = targetTank.fill(toDrain, IFluidHandler.FluidAction.SIMULATE);

        if(accepted > 0) {
            FluidStack drained = sourceTank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            if(!drained.isEmpty()) {
                targetTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                setChanged();

                if(level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        }
    }

    public void rebuildStructure(Level level) {
        if(!isOriginBlock) {
            BlockEntity be = level.getBlockEntity(originBlock);
            if(be instanceof FluidTankBlockEntity origin) {
                origin.rebuildStructure(level);
            }
            return;
        }

        Set<BlockPos> newStructure = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(worldPosition);
        newStructure.add(worldPosition);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            for(Direction dir : Direction.values()) {
                BlockPos neighbor = currentPos.relative(dir);
                if(newStructure.contains(neighbor)) continue;
                if(level.getBlockEntity(neighbor) instanceof FluidTankBlockEntity tankBE) {
                    if(tankBE.getTankClass() == this.getTankClass()) {
                        newStructure.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        this.cachedStructure = newStructure;
        recalcBounds();

        int totalCapacity = 0;
        for(BlockPos pos : cachedStructure) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof FluidTankBlockEntity tankBE) {
                totalCapacity += tankBE.getBaseCapacity();
            }
        }

        if(tank != null) {
            tank.setCapacity(totalCapacity);
            int currentAmount = tank.getFluidAmount();
            if(currentAmount > totalCapacity) {
                tank.drain(currentAmount - totalCapacity, IFluidHandler.FluidAction.EXECUTE);
                setChanged();
            }
        }

        for(BlockPos pos : cachedStructure) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof FluidTankBlockEntity tankBE) {
                tankBE.originBlock = this.worldPosition;
                tankBE.isOriginBlock = pos.equals(this.worldPosition);
                if(!tankBE.isOriginBlock) {
                    tankBE.tank = null;
                }
                tankBE.setChanged();
            }
        }

        updateAllVisuals(level);
    }

    private void updateAllVisuals(Level level) {
        for(BlockPos pos : cachedStructure) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof FluidTankBlockEntity tankBE) {
                tankBE.updateVisualConnections(level, pos);
            }
        }
    }

    private void updateVisualConnections(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        BlockState newState = currentState
                .setValue(FluidTankBlock.NORTH, isConnected(level, pos, pos.north()))
                .setValue(FluidTankBlock.SOUTH, isConnected(level, pos, pos.south()))
                .setValue(FluidTankBlock.WEST, isConnected(level, pos, pos.west()))
                .setValue(FluidTankBlock.EAST, isConnected(level, pos, pos.east()))
                .setValue(FluidTankBlock.UP, isConnected(level, pos, pos.above()))
                .setValue(FluidTankBlock.DOWN, isConnected(level, pos, pos.below()));

        if(!newState.equals(currentState)) {
            level.setBlock(pos, newState, 3);
        }
    }

    private void recalcBounds() {
        if(cachedStructure == null || cachedStructure.isEmpty()) return;
        minX = cachedStructure.stream().mapToInt(BlockPos::getX).min().getAsInt();
        maxX = cachedStructure.stream().mapToInt(BlockPos::getX).max().getAsInt();
        minY = cachedStructure.stream().mapToInt(BlockPos::getY).min().getAsInt();
        maxY = cachedStructure.stream().mapToInt(BlockPos::getY).max().getAsInt();
        minZ = cachedStructure.stream().mapToInt(BlockPos::getZ).min().getAsInt();
        maxZ = cachedStructure.stream().mapToInt(BlockPos::getZ).max().getAsInt();
    }

    private void updateBounds(BlockPos pos) {
        if(cachedStructure == null) {
            cachedStructure = new HashSet<>();
            minX = maxX = pos.getX();
            minY = maxY = pos.getY();
            minZ = maxZ = pos.getZ();
        } else {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        cachedStructure.add(pos);
    }

    private boolean isConnected(Level level, BlockPos current, BlockPos neighbor) {
        BlockEntity be = level.getBlockEntity(neighbor);
        if(!(be instanceof FluidTankBlockEntity tankNeighbor)) return false;
        return tankNeighbor.getTankClass() == this.getTankClass()
                && tankNeighbor.getOriginPos().equals(this.getOriginPos());
    }

    public boolean canConnectTo(Level level, BlockPos otherPos) {
        BlockEntity thisBE =  level.getBlockEntity(this.worldPosition);
        if(!(thisBE instanceof FluidTankBlockEntity thisTank)) return false;

        BlockEntity otherBE = level.getBlockEntity(otherPos);
        if(!(otherBE instanceof FluidTankBlockEntity otherTank)) return false;
        if(thisTank.getTankClass() != otherTank.getTankClass()) return false;

        if(thisTank.getOriginPos().equals(otherTank.getOriginPos())) return true;

        FluidTankBlockEntity thisOrigin = thisTank.getOrigin(level);
        FluidTankBlockEntity otherOrigin = otherTank.getOrigin(level);

        if(thisOrigin == null || otherOrigin == null) return false;

        Set<BlockPos> union = new HashSet<>(thisOrigin.getCachedStructure(level));
        union.addAll(otherOrigin.getCachedStructure(level));
        union.add(otherPos);

        int uMinX = union.stream().mapToInt(BlockPos::getX).min().getAsInt();
        int uMaxX = union.stream().mapToInt(BlockPos::getX).max().getAsInt();
        int uMinY = union.stream().mapToInt(BlockPos::getY).min().getAsInt();
        int uMaxY = union.stream().mapToInt(BlockPos::getY).max().getAsInt();
        int uMinZ = union.stream().mapToInt(BlockPos::getZ).min().getAsInt();
        int uMaxZ = union.stream().mapToInt(BlockPos::getZ).max().getAsInt();

        return (uMaxX - uMinX) < MAX_WIDTH &&
                (uMaxY - uMinY) < MAX_HEIGHT &&
                (uMaxZ - uMinZ) < MAX_WIDTH;
    }

    public Set<BlockPos> getConnectedTanks(Level level) {
        FluidTankBlockEntity origin = getOrigin(level);
        if(origin != null) {
            return origin.getCachedStructure(level);
        }
        return Collections.emptySet();
    }

    private Set<BlockPos> getCachedStructure(Level level) {
        if(cachedStructure == null) {
            if(isOriginBlock) {
                rebuildStructure(level);
            } else {
                BlockEntity be = level.getBlockEntity(this.originBlock);
                if(be instanceof FluidTankBlockEntity originTank) {
                    return originTank.getCachedStructure(level);
                }
            }
        }
        return cachedStructure != null ? cachedStructure : Collections.emptySet();
    }

    public FluidTankBlockEntity getOrigin(Level level) {
        if(isOriginBlock) return this;
        if(originBlock != null && level.getBlockEntity(originBlock) instanceof FluidTankBlockEntity origin) {
            return origin;
        }

        return null;
    }

    public FluidTank getTank() {
        return isOriginBlock ? tank : null;
    }

    public FluidTank getTankOrigin(Level level) {
        FluidTankBlockEntity origin = getOrigin(level);
        return origin != null ? origin.getTank() : null;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public BlockPos getOriginPos() {
        return originBlock;
    }

    public int getCountTanks(Level level) {
        return getConnectedTanks(level).size();
    }

    public int getTotalCapacity(Level level) {
        FluidTankBlockEntity origin = getOrigin(level);
        if (origin != null && origin.tank != null) {
            return origin.tank.getCapacity();
        }
        return baseCapacity;
    }

    public int getLightFluid() {
        FluidTank originTank = getTankOrigin(level);
        if (originTank != null && !originTank.isEmpty()) {
            Fluid fluid = originTank.getFluid().getFluid();
            return fluid.getFluidType().getLightLevel();
        }
        return 0;
    }

    public Class<? extends FluidTankBlockEntity> getTankClass() {
        return this.getClass();
    }
}