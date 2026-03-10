package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.block.fluid_pipe.PipeFluid;
import com.daniels0k.industry_mod.block.fluid_pipe.PipeFluidBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class Wrench extends Item {
    public Wrench(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if(customData == null) return;
        CompoundTag compoundTag = customData.copyTag();

        int currentIndex = compoundTag.getIntOr("dirSetMode", 0);
        Direction[] dirs = Direction.values();
        tooltip.accept(Component.translatable("item.industry_mod.wrench.desc0", "§6§l" + dirs[currentIndex].getName()));

        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if(player == null) return InteractionResult.PASS;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if(customData == null) return InteractionResult.PASS;
        CompoundTag compoundTag = customData.copyTag();

        int currentIndex = compoundTag.getIntOr("dirSetMode", 0);
        Direction[] dirs = Direction.values();

        if(!player.isCrouching()) {
            int newDirIndex = (currentIndex + 1) % dirs.length;
            compoundTag.putInt("dirSetMode", newDirIndex);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
            player.displayClientMessage(Component.literal("§6§lDir: " + dirs[newDirIndex].getName()), true);
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(!(blockEntity instanceof PipeFluidBlockEntity pipeFluid)) return InteractionResult.PASS;

            Direction selectedDir = dirs[currentIndex];
            BlockState state = level.getBlockState(pos);

            if(!state.getValue(PipeFluid.getDirectionProperty(selectedDir))) {
                return InteractionResult.PASS;
            }

            boolean newMode = !pipeFluid.isInput(selectedDir);
            pipeFluid.setDirectionMode(selectedDir, newMode);
            return InteractionResult.SUCCESS;
        }
    }
}
