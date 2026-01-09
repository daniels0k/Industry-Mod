package com.daniels0k.industry_mod.block.connector.copper;

import com.daniels0k.industry_mod.api.capabilities.EnergyCapabilities;
import com.daniels0k.industry_mod.api.energy.EnerTickStorage;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WireCopperConnectBlockEntity extends BlockEntity {
    public enum TypeConnect {INPUT, OUTPUT}
    public final EnerTickStorage energyET = new EnerTickStorage(100);
    public final List<BlockPos> parentsConnect = new ArrayList<>();

    public final ListMultimap<TypeConnect, BlockPos> connections = ArrayListMultimap.create();

    public WireCopperConnectBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WIRE_COPPER_CONNECT.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energyET.serialize(output);
        ValueOutput.ValueOutputList parentsConnectData = output.childrenList("parentConnect");
        for(BlockPos pos : parentsConnect) {
            ValueOutput child = parentsConnectData.addChild();
            child.putInt("x", pos.getX());
            child.putInt("y", pos.getY());
            child.putInt("z", pos.getZ());
        }

        if(getBlockState().getValue(WireCopperConnect.MODE_CONNECT) == EnumModeWireCopperConnect.MODE_INPUT) {
            ValueOutput connectionsData = output.child("connections");
            ValueOutput.ValueOutputList outputs = connectionsData.childrenList("outputs");
            ValueOutput.ValueOutputList inputs = connectionsData.childrenList("inputs");

            for(BlockPos connection : connections.get(TypeConnect.INPUT)) {
                ValueOutput child = inputs.addChild();
                child.putInt("x", connection.getX());
                child.putInt("y", connection.getY());
                child.putInt("z", connection.getZ());
            }

            for(BlockPos connection : connections.get(TypeConnect.OUTPUT)) {
                ValueOutput child = outputs.addChild();
                child.putInt("x", connection.getX());
                child.putInt("y", connection.getY());
                child.putInt("z", connection.getZ());
            }
        }
    }

    public void  addConnection(BlockPos pointB) {
        if(pointB.equals(this.worldPosition)) return;
        assert this.level != null;
        BlockEntity blockEntity = this.level.getBlockEntity(pointB);

        if(!(blockEntity instanceof WireCopperConnectBlockEntity wireCopperConnect)) return;
        BlockState blockStateWireCopper = wireCopperConnect.getBlockState();
        if(!(blockStateWireCopper.getBlock() instanceof WireCopperConnect)) return;

        EnumModeWireCopperConnect mode = blockStateWireCopper.getValue(WireCopperConnect.MODE_CONNECT);
        switch (mode) {
            case MODE_INPUT -> {
                if(connections.containsEntry(TypeConnect.INPUT, pointB)) return;
                connections.put(TypeConnect.INPUT, pointB);
            }
            case MODE_OUTPUT -> {
                if(connections.containsEntry(TypeConnect.OUTPUT, pointB)) return;
                connections.put(TypeConnect.OUTPUT, pointB);
            }
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyET.deserialize(input);

        ValueInput.ValueInputList parentsConnectData = input.childrenListOrEmpty("parentConnect");
        for(ValueInput child : parentsConnectData.stream().toList()) {
            int xParent = child.getIntOr("x", 0);
            int yParent = child.getIntOr("y", 0);
            int zParent = child.getIntOr("z", 0);
            BlockPos pos = new BlockPos(xParent, yParent, zParent);
            this.parentsConnect.add(pos);
        }

        if(getBlockState().getValue(WireCopperConnect.MODE_CONNECT) == EnumModeWireCopperConnect.MODE_INPUT) {
            connections.clear();
            ValueInput connectionsData = input.childOrEmpty("connections");
            ValueInput.ValueInputList inputs = connectionsData.childrenListOrEmpty("inputs");
            ValueInput.ValueInputList outputs = connectionsData.childrenListOrEmpty("outputs");

            for(ValueInput connection : inputs.stream().toList()) {
                int x = connection.getIntOr("x", 0);
                int y = connection.getIntOr("y", 0);
                int z = connection.getIntOr("z", 0);
                connections.put(TypeConnect.INPUT, new BlockPos(x, y, z));
            }

            for(ValueInput connection : outputs.stream().toList()) {
                int x = connection.getIntOr("x", 0);
                int y = connection.getIntOr("y", 0);
                int z = connection.getIntOr("z", 0);
                connections.put(TypeConnect.OUTPUT, new BlockPos(x, y, z));
            }
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        Direction facing = blockState.getValue(WireCopperConnect.FACING);
        EnumModeWireCopperConnect mode = blockState.getValue(WireCopperConnect.MODE_CONNECT);

        switch (mode) {
            case MODE_NONE -> {
                BlockPos targetPos = blockPos.relative(facing);

                EnerTickStorage targetEnertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK,
                        targetPos, facing.getOpposite());

                if(targetEnertick != null) {
                    targetEnertick.receiveEnergy(targetEnertick.getEnergyStored(), false);
                    energyET.extractEnergy(targetEnertick.getEnergyStored(), false);
                }
            }
            case MODE_INPUT -> {
                for(BlockPos input : connections.get(TypeConnect.INPUT)) {
                    EnerTickStorage targetEnertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK,
                            input, null);

                    if(targetEnertick != null) {
                    }
                }

                for(BlockPos output : connections.get(TypeConnect.OUTPUT)) {
                    EnerTickStorage targetEnertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK,
                            output, null);

                    if(targetEnertick != null) {
                    }
                }
            }
        }
    }
}
