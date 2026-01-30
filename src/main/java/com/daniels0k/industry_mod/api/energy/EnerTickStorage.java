package com.daniels0k.industry_mod.api.energy;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class EnerTickStorage implements ValueIOSerializable {
    protected int capacity;
    protected int maxExtract;
    protected int maxReceive;
    protected int energy;

    public EnerTickStorage(int capacity) {
        this(capacity, capacity, capacity, 0);
    }

    public EnerTickStorage(int capacity, int maxExtract) {
        this(capacity, maxExtract, maxExtract, 0);
    }

    public EnerTickStorage(int capacity, int maxExtract, int maxReceive) {
        this(capacity, maxExtract, maxReceive, 0);
    }

    public EnerTickStorage(int capacity, int maxExtract, int maxReceive, int energy) {
        this.capacity = capacity;
        this.maxExtract = maxExtract;
        this.maxReceive = maxReceive;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("enertick", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.energy = input.getIntOr("enertick", 0);
    }

    public int receiveEnergy(int toReceive, boolean simulate) {
        if(this.canReceive() && toReceive > 0) {
            int energyReceived = Mth.clamp(this.capacity - this.energy, 0, Math.min(this.maxReceive, toReceive));
            if(!simulate) {
                this.energy += energyReceived;
            }

            return energyReceived;
        } else {
            return 0;
        }
    }

    public int extractEnergy(int toExtract, boolean simulate) {
        if(this.canExtract() && toExtract > 0) {
            int energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, toExtract));

            if(!simulate) {
                this.energy -= energyExtracted;
            }

            return energyExtracted;
        } else {
            return 0;
        }
    }

    public int getEnergyStored() {
        return this.energy;
    }

    public int getMaxEnergyStored() {
        return this.capacity;
    }

    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    public boolean isEmpty() {
        return energy <= 0;
    }

    public boolean isFull() {
        return energy >= capacity;
    }

    public boolean isVault() {
        return false;
    }
}
