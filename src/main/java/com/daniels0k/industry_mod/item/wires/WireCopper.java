package com.daniels0k.industry_mod.item.wires;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WireCopper extends Item {

    public WireCopper(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.industry_mod.wire_copper.desc0"));

        if(Screen.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.industry_mod.wire_copper.desc1"));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
