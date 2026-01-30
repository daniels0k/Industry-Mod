package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.block.connector.copper.EnumModeWireConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireConnectBlockEntity;
import com.daniels0k.industry_mod.item.datacomponent.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class ConnectionChanger extends Item {
    public ConnectionChanger(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        boolean isInputChange = stack.getOrDefault(ModDataComponents.CHANGER_MODE, false);
        tooltip.accept(Component.translatable("item.industry_mod.connection_changer.desc0"));
        String translateMode = Component.translatable("item.industry_mod.connection_changer.desc1").getString();
        tooltip.accept(Component.literal(translateMode.replaceAll("%mode%", isInputChange ? "§cInput" : "§9Output")));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState blockState = level.getBlockState(blockPos);
        if(player == null) return InteractionResult.FAIL;
        boolean isInputChange = stack.getOrDefault(ModDataComponents.CHANGER_MODE, false);

        if(player.isShiftKeyDown()) {
            stack.set(ModDataComponents.CHANGER_MODE, !isInputChange);
            return InteractionResult.SUCCESS;
        }

        if(!(blockState.getBlock() instanceof WireConnect)) return InteractionResult.FAIL;
        if(!(level.getBlockEntity(blockPos) instanceof WireConnectBlockEntity wireCopperConnectBE)) return InteractionResult.FAIL;
        if(!wireCopperConnectBE.connections.isEmpty() || !wireCopperConnectBE.parentsConnect.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.industry_mod.connection_changer.err_connection_or_parent_not_empty"), true);
            return InteractionResult.FAIL;
        }
        EnumModeWireConnect newMode = isInputChange ? EnumModeWireConnect.MODE_INPUT : EnumModeWireConnect.MODE_OUTPUT;

        BlockState setMode = blockState.setValue(WireConnect.MODE_CONNECT, newMode);
        level.setBlock(blockPos, setMode, 3);
        return InteractionResult.SUCCESS;
    }
}
