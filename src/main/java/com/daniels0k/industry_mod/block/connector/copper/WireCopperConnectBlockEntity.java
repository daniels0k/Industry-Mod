package com.daniels0k.industry_mod.block.connector.copper;

import com.daniels0k.industry_mod.api.capabilities.EnergyCapabilities;
import com.daniels0k.industry_mod.api.energy.EnerTickStorage;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.item.ModItems;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WireCopperConnectBlockEntity extends BlockEntity {
    public enum TypeConnect {
        INPUT,
        OUTPUT;

        public static TypeConnect translate(EnumModeWireCopperConnect mode) {
            return switch (mode) {
                case MODE_INPUT -> INPUT;
                case MODE_OUTPUT -> OUTPUT;
                case MODE_NONE -> null;
            };
        }
        public TypeConnect reverse() {
            return this == INPUT ? OUTPUT : INPUT;
        }
    }
    public final EnerTickStorage energyET = new EnerTickStorage(100);
    public final List<BlockPos> parentsConnect = new ArrayList<>();
    public final ListMultimap<TypeConnect, ConnectionData> connections = ArrayListMultimap.create();
    public record ConnectionData(BlockPos pos, float efficiency, float lossFactor) {
        private static int totalWireUsed;

        public int getTotalWireUsed() {
            return totalWireUsed;
        }

        public void setTotalWireUsed(int count) {
            totalWireUsed = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConnectionData that)) return false;
            return pos.equals(that.pos);
        }
    }

    public WireCopperConnectBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WIRE_COPPER_CONNECT.get(), pos, blockState);
    }

    public void  addConnection(BlockPos pointB, float efficiency, float lossFactor, int countWireUsed) {
        if(this.level == null || pointB.equals(this.worldPosition)) return;
        BlockEntity blockEntity = this.level.getBlockEntity(pointB);

        if(!(blockEntity instanceof WireCopperConnectBlockEntity wireCopperConnect)) return;
        BlockState blockStateWireCopper = wireCopperConnect.getBlockState();
        if(!(blockStateWireCopper.getBlock() instanceof WireCopperConnect)) return;

        EnumModeWireCopperConnect modeB = blockStateWireCopper.getValue(WireCopperConnect.MODE_CONNECT);
        TypeConnect typeConnectB = TypeConnect.translate(modeB);

        boolean exist = connections.get(typeConnectB).stream().anyMatch(data -> data.pos.equals(pointB));

        if(!exist) {
            ConnectionData connectionData = new ConnectionData(pointB, efficiency, lossFactor);
            connectionData.setTotalWireUsed(countWireUsed);
            connections.put(typeConnectB, connectionData);
            if(level != null && level.getBlockEntity(pointB) instanceof WireCopperConnectBlockEntity targetBE) {
                if(!targetBE.parentsConnect.contains(this.worldPosition)) {
                    targetBE.parentsConnect.add(this.worldPosition);
                    targetBE.setChanged();
                }
            }
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean removeConnection(BlockPos pointB) {
        boolean remove = connections.values().removeIf(data -> data.pos.equals(pointB));
        if(remove) {
            setChanged();
            if(level != null && level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return remove;
    }

    public boolean removeLastConnection() {
        if(this.level == null || connections.isEmpty()) return false;
        List<TypeConnect> keys = new ArrayList<>(connections.keys());
        TypeConnect lastKey = keys.get(keys.size() - 1);
        List<ConnectionData> list = connections.get(lastKey);

        if(!list.isEmpty()) {
            ConnectionData lastConnection = list.get(list.size() - 1);
            BlockPos otherPos = lastConnection.pos;
            int cableToReturn = lastConnection.getTotalWireUsed();
            list.remove(list.size() - 1);
            this.parentsConnect.remove(otherPos);

            if(level.getBlockEntity(otherPos) instanceof WireCopperConnectBlockEntity wireCopperConnect) {
                wireCopperConnect.removeConnection(this.worldPosition);
                wireCopperConnect.parentsConnect.remove(this.worldPosition);
            }

            ItemStack stack = new ItemStack(ModItems.COPPER_WIRE.get(), cableToReturn);
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return true;
        }
        return false;
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

        ValueOutput connectionsData = output.child("connections");
        ValueOutput.ValueOutputList outputs = connectionsData.childrenList("outputs");
        ValueOutput.ValueOutputList inputs = connectionsData.childrenList("inputs");

        for(ConnectionData connection : connections.get(TypeConnect.INPUT)) {
            ValueOutput child = inputs.addChild();
            child.putFloat("efficiency", connection.efficiency);
            child.putFloat("lossFactor", connection.lossFactor);
            child.putInt("countWireUsed", connection.getTotalWireUsed());
            child.putInt("x", connection.pos.getX());
            child.putInt("y", connection.pos.getY());
            child.putInt("z", connection.pos.getZ());
        }

        for(ConnectionData connection : connections.get(TypeConnect.OUTPUT)) {
            ValueOutput child = outputs.addChild();
            child.putFloat("efficiency", connection.efficiency);
            child.putFloat("lossFactor", connection.lossFactor);
            child.putInt("countWireUsed", connection.getTotalWireUsed());
            child.putInt("x", connection.pos.getX());
            child.putInt("y", connection.pos.getY());
            child.putInt("z", connection.pos.getZ());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyET.deserialize(input);

        parentsConnect.clear();
        ValueInput.ValueInputList parentsConnectData = input.childrenListOrEmpty("parentConnect");
        for(ValueInput child : parentsConnectData.stream().toList()) {
            parentsConnect.add(new BlockPos(child.getIntOr("x", 0), child.getIntOr("y", 0), child.getIntOr("z", 0)));
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
                int wireUsed = connection.getIntOr("countWireUsed", 0);
                float efficiency = connection.getFloatOr("efficiency", 0.0f);
                float lossFactor = connection.getFloatOr("lossFactor", 0.0f);
                ConnectionData connectionData = new ConnectionData(new BlockPos(x, y, z), efficiency, lossFactor);
                connectionData.setTotalWireUsed(wireUsed);
                connections.put(TypeConnect.INPUT, connectionData);
            }

            for(ValueInput connection : outputs.stream().toList()) {
                int x = connection.getIntOr("x", 0);
                int y = connection.getIntOr("y", 0);
                int z = connection.getIntOr("z", 0);
                int wireUsed = connection.getIntOr("countWireUsed", 0);
                float efficiency = connection.getFloatOr("efficiency", 0.0f);
                float lossFactor = connection.getFloatOr("lossFactor", 0.0f);
                ConnectionData connectionData = new ConnectionData(new BlockPos(x, y, z), efficiency, lossFactor);
                connectionData.setTotalWireUsed(wireUsed);
                connections.put(TypeConnect.OUTPUT, connectionData);
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
                for(ConnectionData input : connections.get(TypeConnect.INPUT)) {
                    EnerTickStorage targetEnertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK,
                            input.pos, null);

                    if(targetEnertick != null) {
                    }
                }

                for(ConnectionData output : connections.get(TypeConnect.OUTPUT)) {
                    EnerTickStorage targetEnertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK,
                            output.pos, null);

                    if(targetEnertick != null) {
                    }
                }
            }
        }
    }

    public void drops() {
        if(this.level == null || this.level.isClientSide) return;
        int totalCablesToDrop = 0;
        for(ConnectionData data : connections.values()) {
            totalCablesToDrop += data.getTotalWireUsed();

            if(level.getBlockEntity(data.pos) instanceof WireCopperConnectBlockEntity wireCopperConnect) {
                wireCopperConnect.removeConnection(this.worldPosition);
            }
        }

        if(totalCablesToDrop > 0) {
            ItemStack stack = new ItemStack(ModItems.COPPER_WIRE.get(), totalCablesToDrop);
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
        }

        this.connections.clear();
        this.parentsConnect.clear();
        this.setChanged();
    }
}
