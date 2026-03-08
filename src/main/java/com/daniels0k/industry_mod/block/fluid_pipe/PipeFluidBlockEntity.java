package com.daniels0k.industry_mod.block.fluid_pipe;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.block.fluid_pipe.copper.CopperFluidPipe;
import com.daniels0k.industry_mod.block.fluid_tank.FluidTankBlockEntity;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluidPipeBlockEntity extends BlockEntity {
    public final FluidTank tank;
    protected Set<Fluid> invalidFluids;

    private static final int TRANSFER_RATE = 100;
    private static final int PUSH_AMOUNT = 50;

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int capacity) {
        super(type, pos, blockState);
        this.tank = new FluidTank(capacity) {
            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if(getLevel() != null && invalidFluids != null) {
                    if(invalidFluids.contains(resource.getFluid())) {
                        if(!getLevel().isClientSide() && action.execute()) {
                            getLevel().destroyBlock(worldPosition, false);
                            getLevel().explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1f, Level.ExplosionInteraction.NONE);
                        }
                        return 0;
                    }
                }
                return super.fill(resource, action);
            }
        };
        this.invalidFluids = new HashSet<>();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidPipeBlockEntity blockEntity) {
        if(level.isClientSide())  return;

        pullFromInputs(level, pos, blockState, blockEntity);
        pushToOutputs(level, pos, blockState, blockEntity);
    }

    private void pullFromInputs(Level level, BlockPos pos, BlockState blockState, FluidPipeBlockEntity blockEntity) {
        if(blockEntity.tank.getFluidAmount() >= blockEntity.tank.getCapacity()) return;

        for(Direction dir : Direction.values()) {
            if(!FluidPipe.isInput(blockState, dir) || !blockState.getValue(FluidPipe.getDirectionProperty(dir))) continue;

            BlockPos neighborPos =  pos.relative(dir);
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());

            if(handler != null) {
                FluidStack fluidStack = handler.drain(TRANSFER_RATE, IFluidHandler.FluidAction.SIMULATE);

                if(!fluidStack.isEmpty()) {
                    int canAccept = blockEntity.tank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                    if(canAccept > 0) {
                        FluidStack drained = handler.drain(canAccept, IFluidHandler.FluidAction.EXECUTE);
                        blockEntity.tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        blockEntity.setChanged();
                    }
                }
            }
        }
    }

    private void pushToOutputs(Level level, BlockPos pos, BlockState blockState, FluidPipeBlockEntity blockEntity) {
        if(blockEntity.tank.isEmpty()) return;

        List<IFluidHandler> outputs = new ArrayList<>();
        for(Direction dir : Direction.values()) {
            if(!FluidPipe.isOutput(blockState, dir) || !blockState.getValue(FluidPipe.getDirectionProperty(dir))) continue;

            BlockPos neighborPos = pos.relative(dir);
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite());

            if(handler != null) {
                outputs.add(handler);
            }
        }

        if(outputs.isEmpty()) return;

        FluidStack available = blockEntity.tank.getFluid().copy();
        int remaining = available.getAmount();

        while(remaining > 0 && !outputs.isEmpty()) {
            int perTarget = Math.max(1, remaining / outputs.size());
            boolean anyAccepted = false;

            for(IFluidHandler handler : outputs) {
                if(remaining <= 0) break;
                int toSend = Math.min(perTarget, remaining);
                FluidStack toSendStack = new FluidStack(available.getFluid(), toSend);
                int accepted = handler.fill(toSendStack, IFluidHandler.FluidAction.SIMULATE);

                if(accepted > 0) {
                    FluidStack drained = blockEntity.tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);

                    if(!drained.isEmpty()) {
                        handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        remaining -= drained.getAmount();
                        anyAccepted = true;
                        blockEntity.setChanged();
                    }
                }
            }

            if(!anyAccepted) break;
        }
    }

    public void setDirectionMode(Direction dir, boolean isInput) {
        if(level == null || level.isClientSide()) return;

        BlockState currentState = getBlockState();

        if(currentState.getValue(FluidPipe.getDirectionProperty(dir))) {
            BlockState newState = currentState.setValue(FluidPipe.getInputDirectionProperty(dir), isInput);
            level.setBlock(worldPosition, newState, 3);
        }
    }

    public boolean isInput(Direction dir) {
        return FluidPipe.isInput(getBlockState(), dir);
    }
}
