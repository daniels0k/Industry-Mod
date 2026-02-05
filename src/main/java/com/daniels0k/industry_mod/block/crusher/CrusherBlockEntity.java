package com.daniels0k.industry_mod.block.crusher;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.api.energy.EnerTickStorage;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.recipe.ModRecipes;
import com.daniels0k.industry_mod.recipe.crushing.CrusherRecipe;
import com.daniels0k.industry_mod.screen.crusher.CrusherMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrusherBlockEntity extends BlockEntity implements MenuProvider {
    public final EnerTickStorage energyET = new EnerTickStorage(500);
    public final ItemStackHandler inventory = new ItemStackHandler(4);
    private int processTime;
    private int maxProcessTime;
    private int energyCost;
    public ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? CrusherBlockEntity.this.processTime : index == 1 ? CrusherBlockEntity.this.maxProcessTime : CrusherBlockEntity.this.energyCost;
        }

        @Override
        public void set(int index, int value) {
            if(index == 0) {
                CrusherBlockEntity.this.processTime = value;
            } else if (index == 1) {
                CrusherBlockEntity.this.maxProcessTime = value;
            } else if (index == 2) {
                CrusherBlockEntity.this.energyCost = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public CrusherBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CRUSHER.get(), pos, blockState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energyET.serialize(output);
        inventory.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyET.deserialize(input);
        inventory.deserialize(input);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industry_mod.crusher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CrusherMenu(i, inventory, this);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, CrusherBlockEntity blockEntity) {
        if(level.isClientSide()) return;

        ItemStack inputStack = inventory.getStackInSlot(0);
        SingleRecipeInput recipeInput = new SingleRecipeInput(inputStack);
        Optional<RecipeHolder<CrusherRecipe>> recipe = ((ServerLevel) level).recipeAccess().getRecipeFor(ModRecipes.CRUSHER_RECIPE.get(), recipeInput, level);

        if(blockState.getValue(Crusher.LIT) != (blockEntity.maxProcessTime > 0)) {
            BlockState newState = blockState.setValue(Crusher.LIT, blockEntity.maxProcessTime > 0);
            level.setBlock(pos, newState, 3);
        }

        if(recipe.isPresent()) {
            CrusherRecipe actualRecipe = recipe.get().value();
            blockEntity.maxProcessTime = actualRecipe.time();
            blockEntity.energyCost = actualRecipe.enertick();

            if(blockEntity.energyET.getEnergyStored() >= blockEntity.energyCost) {
                ItemStack result = actualRecipe.assemble(recipeInput, level.registryAccess());
                if(canInsertResult(result)) {
                    blockEntity.processTime++;

                    if(blockEntity.processTime >= blockEntity.maxProcessTime) {
                        ItemStack remaining = result.copy();
                        for(int slot = 1; slot < blockEntity.inventory.getSlots(); slot++) {
                            remaining = blockEntity.inventory.insertItem(slot, remaining, false);
                            if(remaining.isEmpty()) break;
                        }
                        blockEntity.inventory.getStackInSlot(0).shrink(1);
                        blockEntity.energyET.extractEnergy(blockEntity.energyCost, false);
                        blockEntity.processTime = 0;
                    }
                    blockEntity.setChanged();
                }
            }
        } else {
            blockEntity.maxProcessTime = 0;
            blockEntity.energyCost = 0;
        }
    }

    private boolean canInsertResult(ItemStack stack) {
        for(int slot = 1; slot < this.inventory.getSlots(); slot++) {
            ItemStack slotStack = this.inventory.getStackInSlot(slot);

            if(slotStack.isEmpty()) return true;

            if(ItemStack.isSameItemSameComponents(slotStack, stack)) {
                if(slotStack.getCount() + stack.getCount() <= slotStack.getMaxStackSize()) {
                    return true;
                }
            }
        }
        return false;
    }
}
