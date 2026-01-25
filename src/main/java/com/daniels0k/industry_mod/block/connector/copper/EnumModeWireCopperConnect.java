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

    public static EnumModeWireCopperConnect translate(WireCopperConnectBlockEntity.TypeConnect typeConnect) {
        return switch (typeConnect) {
            case INPUT -> MODE_INPUT;
            case OUTPUT -> MODE_OUTPUT;
        };
    }

    public static EnumModeWireCopperConnect translateInverse(WireCopperConnectBlockEntity.TypeConnect typeConnect) {
        return switch (typeConnect) {
            case OUTPUT -> MODE_INPUT;
            case INPUT -> MODE_OUTPUT;
        };
    }

    public EnumModeWireCopperConnect reversePut() {
        return this == MODE_INPUT ? MODE_OUTPUT : this == MODE_OUTPUT ? MODE_INPUT : null;
    }
}
