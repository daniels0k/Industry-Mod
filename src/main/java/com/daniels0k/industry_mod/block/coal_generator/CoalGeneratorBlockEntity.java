package com.daniels0k.industry_mod.block.coal_generator;

import com.daniels0k.industry_mod.api.capabilities.EnergyCapabilities;
import com.daniels0k.industry_mod.api.energy.EnerTickStorage;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.screen.coal_generator.CoalGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CoalGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private final Item[] itemsValid = {
            Items.COAL, Items.CHARCOAL, Items.COAL_BLOCK
    };
    public ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return Arrays.stream(itemsValid).anyMatch(stack::is);
        }
    };
    private int burnTime;
    private int maxBurnTime;
    private int energyCount;
    public ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? CoalGeneratorBlockEntity.this.burnTime : index == 1 ? CoalGeneratorBlockEntity.this.maxBurnTime : CoalGeneratorBlockEntity.this.energyCount;
        }

        @Override
        public void set(int index, int value) {
            if(index == 0) {
                CoalGeneratorBlockEntity.this.burnTime = value;
            } else if (index == 1) {
                CoalGeneratorBlockEntity.this.maxBurnTime = value;
            } else if (index == 2) {
                CoalGeneratorBlockEntity.this.energyCount = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        inventory.serialize(output);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        inventory.deserialize(input);
        super.loadAdditional(input);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, CoalGeneratorBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        boolean wasLit = blockState.getValue(CoalGenerator.LIT);
        Direction facing = blockState.getValue(CoalGenerator.FACING);
        BlockPos backBlock = pos.relative(facing.getOpposite());
        boolean dirty = false;

        EnerTickStorage enertick = level.getCapability(EnergyCapabilities.EnerTickStorage.BLOCK, backBlock, facing.getOpposite());
        if(enertick == null) return;

        if(blockEntity.burnTime > 0) {
            blockEntity.burnTime--;
            dirty = true;
        }

        if(blockEntity.burnTime <= 0 && enertick.getEnergyStored() < enertick.getMaxEnergyStored()) {
            ItemStack fuel = inventory.getStackInSlot(0);
            if(!fuel.isEmpty()) {
                blockEntity.burnTime = getBurnTimeItem(fuel);
                fuel.shrink(1);
                blockEntity.setChanged();
            }
        }

        if(wasLit && level.getGameTime() % 20 == 0) {
            enertick.receiveEnergy(energyCount, false);
            dirty = true;
        }

        if(wasLit != (blockEntity.burnTime > 0)) {
            BlockState newState = blockState.setValue(CoalGenerator.LIT, blockEntity.burnTime > 0);
            level.setBlock(pos, newState, 3);
        }

        if(dirty) blockEntity.setChanged();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithFullMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industry_mod.coal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CoalGeneratorMenu(i, inventory, this);
    }

    private int getBurnTimeItem(ItemStack itemStack) {
        if(itemStack.is(Items.COAL)) {
            energyCount = 8;
            return 1600;
        } else if (itemStack.is(Items.CHARCOAL)) {
            energyCount = 4;
            return 1000;
        } else if (itemStack.is(Items.COAL_BLOCK)) {
            energyCount = 72;
            return 14400;
        }

        energyCount = 0;
        return 0;
    }
}
