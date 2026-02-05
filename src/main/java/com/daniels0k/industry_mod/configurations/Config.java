package com.daniels0k.industry_mod.configurations;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HIT_BLOCK_PARTICLE_UP = BUILDER
            .define("hit_block_particle_up", false);

    public static final ModConfigSpec.BooleanValue RENDER_CONNECTION_CABLE = BUILDER
            .define("render_conneciton_cable", true);
    public static final ModConfigSpec.ConfigValue<Double> CABLE_RENDERING_ROTATION = BUILDER
            .defineInRange("cable_rendering_rotation", 32.0, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue VIEW_ENERGY_BAR = BUILDER
            .define("view_energy_bar", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
