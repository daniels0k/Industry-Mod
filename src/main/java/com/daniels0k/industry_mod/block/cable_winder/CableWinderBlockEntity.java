package com.daniels0k.industry_mod.block.cable_winder;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.item.ModItems;
import com.daniels0k.industry_mod.screen.cable_winder.CableWinderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CableWinderBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler inventory = new ItemStackHandler(3) {

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            if (slot == 0) {
                return 1;
            }

            return stack.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if(slot == 0) {
                if(stack.is(ModItems.CABLE_ROLL_COPPER)) {
                    Integer damage = stack.get(DataComponents.DAMAGE);
                    if(damage != null && damage > 0) {
                        return true;
                    }
                }

                return stack.is(ModItems.CABLE_ROLL);
            }

            if(slot == 1) {
                return stack.is(ModItems.COPPER_WIRE);
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private float rotation;
    private int progress;
    private int maxProgress;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? CableWinderBlockEntity.this.progress : CableWinderBlockEntity.this.maxProgress;
        }

        @Override
        public void set(int index, int value) {
            if(index == 0) {
                CableWinderBlockEntity.this.progress = value;
            } else if(index == 1) {
                CableWinderBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CableWinderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CABLE_WINDER.get(), pos, blockState);
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

    public float getRenderingRotation() {
        rotation += 0.2f;
        if(rotation >= 360f) {
            rotation = 0f;
        }
        return rotation;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industry_mod.cable_winder");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CableWinderMenu(i, inventory, this);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CableWinderBlockEntity blockEntity) {
        if(level.isClientSide()) return;
        ItemStack roll = blockEntity.inventory.getStackInSlot(0);
        ItemStack wire = blockEntity.inventory.getStackInSlot(1);

        if(wire.isEmpty() && roll.isEmpty()) {
            blockEntity.progress = 0;
            blockEntity.maxProgress = 0;
            return;
        }

        if(roll.is(ModItems.CABLE_ROLL)) {
            if(wire.is(ModItems.COPPER_WIRE)) {
                if(level.getGameTime() % 30 != 0) return;
                ItemStack roll_copper = ModItems.CABLE_ROLL_COPPER.toStack();
                roll_copper.set(DataComponents.DAMAGE, 1000);
                blockEntity.inventory.setStackInSlot(0, roll_copper);
                blockEntity.setChanged();
                return;
            }
        }

        if(roll.isEmpty() || !roll.isDamaged()) return;

        int currentDamage = roll.getDamageValue();
        blockEntity.progress = currentDamage;
        blockEntity.maxProgress = roll.getMaxDamage();

        if(!wire.isEmpty()) {
            if(currentDamage > 0) {
                if(level.getGameTime() % 20 == 0) {
                    roll.set(DataComponents.DAMAGE, currentDamage - 1);
                    wire.shrink(1);
                    blockEntity.setChanged();
                }
            }
        }
    }
}
