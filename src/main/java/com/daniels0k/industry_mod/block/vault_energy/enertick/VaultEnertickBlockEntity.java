package com.daniels0k.industry_mod.block.vault_energy.enertick;

import com.daniels0k.industry_mod.api.energy.EnerTickStorage;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
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

public class VaultEnertickBlockEntity extends BlockEntity {
    public final EnerTickStorage energyET = new EnerTickStorage(10000) {
        @Override
        public boolean isVault() {
            return true;
        }
    };

    public VaultEnertickBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.VAULT_ENERTICK.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energyET.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyET.deserialize(input);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState) {
        int currentLevel = (energyET.getEnergyStored() * 10) / energyET.getMaxEnergyStored();

        if(blockState.getValue(VaultEnertick.ENERGY_PROGRESS) != currentLevel) {
            BlockState newState = blockState.setValue(VaultEnertick.ENERGY_PROGRESS, currentLevel);
            level.setBlock(pos, newState, 3);
        }
    }
}
