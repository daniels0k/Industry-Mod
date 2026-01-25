package com.daniels0k.industry_mod.api.capabilities;

import com.daniels0k.industry_mod.IndustryMod;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.energy.EnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyCapabilities {
    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath(IndustryMod.MOD_ID, path);
    }

    public static class EnerTickStorage {
        public static final BlockCapability<com.daniels0k.industry_mod.api.energy.EnerTickStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(create("enertick"), com.daniels0k.industry_mod.api.energy.EnerTickStorage.class);
    }

    public static EnergyStorage enerTickToEnergyNeoForge(com.daniels0k.industry_mod.api.energy.EnerTickStorage enertick) {
        return new EnergyStorage(enertick.getCapacity(), enertick.getMaxReceive(), enertick.getMaxExtract(), enertick.getEnergy());
    }
}
