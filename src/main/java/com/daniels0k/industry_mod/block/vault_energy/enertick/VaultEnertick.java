package com.daniels0k.industry_mod.block.vault_energy.enertick;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class VaultEnertick extends BaseEntityBlock {
    public static final MapCodec<VaultEnertick> CODEC = simpleCodec(VaultEnertick::new);
    public static final IntegerProperty ENERGY_PROGRESS = IntegerProperty.create("progress", 0, 10);

    public VaultEnertick(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENERGY_PROGRESS);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(ENERGY_PROGRESS, 0);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VaultEnertickBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.VAULT_ENERTICK.get(),
                (levelTick, blockPos, blockState, blockEntity) -> blockEntity.tick(levelTick, blockPos, blockState));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData, player);
        BlockEntity be = level.getBlockEntity(pos);

        if(be instanceof VaultEnertickBlockEntity vaultBE) {
            CompoundTag nbt = vaultBE.saveWithFullMetadata(level.registryAccess());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
        }
        return stack;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(blockEntity instanceof VaultEnertickBlockEntity vaultBE) {
            ItemStack stack = new ItemStack(this);
            CompoundTag nbt = vaultBE.saveWithFullMetadata(level.registryAccess());

            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
