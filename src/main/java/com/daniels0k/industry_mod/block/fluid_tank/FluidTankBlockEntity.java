package com.daniels0k.industry_mod.block.vault_fluid;

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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VaultFluidBlockEntity extends BlockEntity {
    public FluidTank tank;

    public VaultFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int capacity) {
        super(type, pos, blockState);
        this.tank = new FluidTank(capacity);
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, VaultFluidBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        if(level.getGameTime() % 40 == 0) {
            int count = countConnectedTanks(level, pos);
            int newCapacity = count * 1000;

            if(blockEntity.tank.getCapacity() != newCapacity) {
                blockEntity.tank.setCapacity(newCapacity);
                blockEntity.setChanged();
            }
        }

        if(!blockEntity.tank.isEmpty()) {
            BlockPos belowPos = pos.below();
            if(level.getBlockEntity(belowPos) instanceof VaultFluidBlockEntity belowTank) {
                transferFluids(blockEntity, belowTank);
            }
        }
    }

    private int countConnectedTanks(Level level, BlockPos pos) {
        List<BlockPos> connected = new ArrayList<>();
        Stack<BlockPos> toCheck = new Stack<>();
        toCheck.push(pos);
        connected.add(pos);

        while(!toCheck.isEmpty()) {
            BlockPos current = toCheck.pop();

            for(Direction dir : Direction.values()) {
                BlockPos neighborPos = current.relative(dir);
                int distX = Math.abs(current.getX() - neighborPos.getX());
                int distY = Math.abs(current.getY() - neighborPos.getY());
                int distZ = Math.abs(current.getZ() - neighborPos.getZ());

                if(distX <= 1 && distZ <= 1 && distY <= 5) {
                    if(!connected.contains(neighborPos) && level.getBlockEntity(neighborPos) instanceof VaultFluidBlockEntity) {
                        connected.add(neighborPos);
                        toCheck.push(neighborPos);
                    }
                }
            }
        }
        return connected.size();
    }

    private void transferFluids(VaultFluidBlockEntity source,  VaultFluidBlockEntity target) {
        int spaceInTarget = target.tank.getSpace();
        if(spaceInTarget > 0 && !source.tank.isEmpty()) {
            int toMove = Math.min(source.tank.getFluidAmount(), spaceInTarget);
            FluidStack fluidToMove = source.tank.drain(toMove, IFluidHandler.FluidAction.SIMULATE);
            int accepted = target.tank.fill(fluidToMove, IFluidHandler.FluidAction.EXECUTE);

            if(accepted > 0) {
                source.tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                source.setChanged();
                target.setChanged();
            }
        }
    }
}
