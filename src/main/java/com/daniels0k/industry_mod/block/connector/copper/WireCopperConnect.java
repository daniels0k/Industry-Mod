package com.daniels0k.industry_mod.block.connector.copper;

import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WireCopperConnect extends BaseEntityBlock {
    public static final MapCodec<WireCopperConnect> CODEC = simpleCodec(WireCopperConnect::new);
    public static final VoxelShape SHAPES = Shapes.or(
            box(5.0f, 0.0f, 5.0f, 11.0f, 2.0f, 11.0f),
            box(5.0f, 4.0f, 5.0f, 11.0f, 5.0f, 11.0f),
            box(6.0f, 5.0f, 6.0f, 10.0f, 6.0f, 10.0f),
            box(6.0f, 7.0f, 6.0f, 10.0f, 8.0f, 10.0f),
            box(6.0f, 9.0f, 6.0f, 10.0f, 10.0f, 10.0f),
            box(7.0f, 6.0f, 7.0f, 9.0f, 7.0f, 9.0f),
            box(6.0f, 2.0f, 6.0f, 10.f, 4.0f, 10.0f),
            box(7.0f, 8.0f, 7.0f, 9.0f, 9.0f, 9.0f),
            box(7.0f, 10.0f, 7.0f, 9.0f, 11.0f, 9.0f),
            box(6.0f, 11.0f, 6.0f, 10.0f, 15.0f, 10.0f)).optimize();

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
    public static final EnumProperty<EnumModeWireCopperConnect> MODE_CONNECT = EnumProperty.create("mode", EnumModeWireCopperConnect.class);

    private static VoxelShape rotateShapeFromDown(VoxelShape shape, Direction dir) {
        List<VoxelShape> parts = new ArrayList<>();
        for (AABB box : shape.toAabbs()) {
            switch (dir) {
                case UP -> parts.add(Shapes.box(
                        box.minX, 1 - box.maxY, box.minZ,
                        box.maxX, 1 - box.minY, box.maxZ
                ));
                case NORTH -> parts.add(Shapes.box(
                        box.minX, 1 - box.maxZ, box.minY,
                        box.maxX, 1 - box.minZ, box.maxY
                ));
                case SOUTH -> parts.add(Shapes.box(
                        box.minX, box.minZ, 1 - box.maxY,
                        box.maxX, box.maxZ, 1 - box.minY
                ));
                case WEST -> parts.add(Shapes.box(
                        box.minY, box.minX, box.minZ,
                        box.maxY, box.maxX, box.maxZ
                ));
                case EAST -> parts.add(Shapes.box(
                        1 - box.maxY, box.minX, box.minZ,
                        1 - box.minY, box.maxX, box.maxZ
                ));
                default -> parts.add(Shapes.box(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                ));
            }
        }

        VoxelShape result = Shapes.empty();
        for (VoxelShape part : parts) {
            result = Shapes.or(result, part);
        }
        return result.optimize();
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rotateShapeFromDown(SHAPES, state.getValue(FACING));
    }

    public WireCopperConnect(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace().getOpposite())
                .setValue(MODE_CONNECT, EnumModeWireCopperConnect.MODE_NONE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE_CONNECT);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WireCopperConnectBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, ModBlockEntities.WIRE_COPPER_CONNECT.get(),
                (levelTick, blockPos, blockState, blockEntity) -> blockEntity.tick(levelTick, blockPos, blockState));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof WireCopperConnectBlockEntity blockEntity) {
            if(stack.isEmpty()) {

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
