package com.daniels0k.industry_mod.block.connector.copper;

import net.minecraft.util.StringRepresentable;

public enum EnumModeWireCopperConnect implements StringRepresentable {
    MODE_NONE("none"),
    MODE_INPUT("input"),
    MODE_OUTPUT("output");


    private final String mode;

    EnumModeWireCopperConnect(String mode) {
        this.mode = mode;
    }

    @Override
    public String getSerializedName() {
        return mode;
    }
}
