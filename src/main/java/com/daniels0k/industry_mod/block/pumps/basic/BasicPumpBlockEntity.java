package com.daniels0k.industry_mod.block.pumps.basic;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BasicPumpBlockEntity extends BlockEntity {
    private final Set<Fluid> invalidFluids = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);
    public FluidTank tank = new FluidTank(4000) {
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (getLevel() != null) {
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

    private Direction inputDirection = Direction.DOWN;
    private int transferRate = 100;
    private int cooldown = 0;
    private static final int COOLDOWN_MAX = 10;

    public BasicPumpBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BASIC_PUMP.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output);
        output.putInt("inputDirection", inputDirection.ordinal());
        output.putInt("transferRate", transferRate);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input);

        inputDirection = Direction.values()[input.getIntOr("inputDirection", 0)];
        transferRate = input.getIntOr("transferRate", 100);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, BasicPumpBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Direction facing = blockState.getValue(BasicPump.FACING);
        if (blockEntity.tank.isEmpty()) {
            boolean extracted = tryExtractFromSource(level, pos, blockEntity);
            if (extracted) {
                cooldown = COOLDOWN_MAX;
                blockEntity.setChanged();
            }
        }

        if (!blockEntity.tank.isEmpty()) {
            boolean pushed = tryPushToOutput(level, pos, facing, blockEntity);
            if (pushed) {
                cooldown = COOLDOWN_MAX;
                blockEntity.setChanged();
            }
        }
    }

    private boolean tryExtractFromSource(Level level, BlockPos pos, BasicPumpBlockEntity blockEntity) {
        BlockPos sourcePos = pos.relative(inputDirection);

        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.getBlock() instanceof LiquidBlock fluidBlock) {
            if(sourceState.getValue(LiquidBlock.LEVEL) == 0) {
                Fluid fluid = fluidBlock.fluid;

                if (!invalidFluids.contains(fluid)) {
                    FluidStack fluidStack = new FluidStack(fluid, 1000);

                    if (blockEntity.tank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE) >= 1000) {
                        blockEntity.tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        level.setBlockAndUpdate(sourcePos, Blocks.AIR.defaultBlockState());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean tryPushToOutput(Level level, BlockPos pos, Direction facing, BasicPumpBlockEntity blockEntity) {
        Direction outputDir = facing.getOpposite();
        BlockPos outputPos = pos.relative(outputDir);

        var outputHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, outputPos, outputDir.getOpposite());
        if (outputHandler != null && !blockEntity.tank.isEmpty()) {
            FluidStack available = blockEntity.tank.getFluid();
            int toSend = Math.min(transferRate, available.getAmount());

            FluidStack toTransfer = new FluidStack(available.getFluid(), toSend);
            int canAccept = blockEntity.tank.fill(toTransfer, IFluidHandler.FluidAction.SIMULATE);

            if (canAccept > 0) {
                FluidStack drained = outputHandler.drain(canAccept, IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    int accepted = outputHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                    return accepted > 0;
                }
            }
        }

        return false;
    }

    public void setInputDirection(Direction dir) {
        this.inputDirection = dir;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public Direction getInputDirection() {
        return inputDirection;
    }

    public void setTransferRate(int rate) {
        this.transferRate = Math.min(rate, 1000);
        setChanged();
    }
}
