package com.daniels0k.industry_mod.block.connector.copper;

import net.minecraft.util.StringRepresentable;

public enum EnumModeWireConnect implements StringRepresentable {
    MODE_NONE("none"),
    MODE_INPUT("input"),
    MODE_OUTPUT("output");


    private final String mode;

    EnumModeWireConnect(String mode) {
        this.mode = mode;
    }

    @Override
    public String getSerializedName() {
        return mode;
    }

    public static EnumModeWireConnect translate(WireConnectBlockEntity.TypeConnect typeConnect) {
        return switch (typeConnect) {
            case INPUT -> MODE_INPUT;
            case OUTPUT -> MODE_OUTPUT;
        };
    }

    public static EnumModeWireConnect translateInverse(WireConnectBlockEntity.TypeConnect typeConnect) {
        return switch (typeConnect) {
            case OUTPUT -> MODE_INPUT;
            case INPUT -> MODE_OUTPUT;
        };
    }

    public EnumModeWireConnect reversePut() {
        return this == MODE_INPUT ? MODE_OUTPUT : this == MODE_OUTPUT ? MODE_INPUT : null;
    }
}
