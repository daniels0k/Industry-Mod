package com.daniels0k.industry_mod.block.cable_winder;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CableWinder extends BaseEntityBlock {
    public static final MapCodec<CableWinder> CODEC = simpleCodec(CableWinder::new);
    public static final VoxelShape SHAPE = Shapes.or(
            box(0.0f, 0.0f, 0.0f, 16.0f, 4.0f, 16.0f),
            box(2.0f, 4.0f, 2.0f, 14.0f, 6.0f, 14.0f),
            box(2.0f, 6.0f, 7.0f, 4.0f, 16.0f, 9.0f),
            box(12.0f, 6.0f, 7.0f, 14.0f, 16.0f, 9.0f),
            box(4.0f, 15.0f, 7.0f, 6.0f, 16.0f, 9.0f),
            box(10.0f, 15.0f, 7.0f, 12.0f, 16.0f, 9.0f),
            box(4.0f, 9.0f, 7.0f, 12.0f, 11.0f, 9.0f));

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public CableWinder(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CableWinderBlockEntity(blockPos, blockState);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(blockEntity instanceof CableWinderBlockEntity cableWinder) {
            cableWinder.drops();
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof CableWinderBlockEntity cableWinder) {
            if(player.isCrouching() && !level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(cableWinder, Component.translatable("block.industry_mod.cable_winder")), pos);
                return InteractionResult.SUCCESS;
            }

            if(player.isCrouching()) return InteractionResult.PASS;
            if(cableWinder.inventory.getStackInSlot(0).isEmpty() && !stack.isEmpty() && cableWinder.inventory.isItemValid(0, stack)) {
                cableWinder.inventory.insertItem(0, stack.split(1), false);
                stack.shrink(1);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                return InteractionResult.SUCCESS;
            } else {
                ItemStack extracted = cableWinder.inventory.extractItem(0, 1, false);
                if(!extracted.isEmpty()) {
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                    if(!player.addItem(extracted)) {
                        player.drop(extracted, false);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
