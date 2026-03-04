package com.daniels0k.industry_mod.block.fluid_tank;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;

public abstract class FluidTank extends BaseEntityBlock {
    public FluidTank(Properties properties) {
        super(properties);
    }

    protected abstract MapCodec<? extends FluidTank> codec();
}
