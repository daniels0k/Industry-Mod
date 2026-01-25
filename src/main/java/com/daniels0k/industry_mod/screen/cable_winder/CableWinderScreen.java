package com.daniels0k.industry_mod.screen.cable_winder;

import com.daniels0k.industry_mod.IndustryMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;

public class CableWinderScreen extends AbstractContainerScreen<CableWinderMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(IndustryMod.MOD_ID, "textures/gui/cable_winder/cable_winder_gui.png");
    public CableWinderScreen(CableWinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int secondsRemaining = menu.blockEntity.data.get(0);
        int mins = secondsRemaining / 60;
        int secs = secondsRemaining % 60;
        String timeRoll = Component.translatable("gui.industry_mod.cable_winder.time_process", mins, secs).getString();
        graphics.drawString(font, timeRoll, x + 7, y + 62, CommonColors.DARK_GRAY, false);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
