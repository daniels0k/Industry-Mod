package com.daniels0k.industry_mod.screen.crusher;

import com.daniels0k.industry_mod.IndustryMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;

public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(IndustryMod.MOD_ID, "textures/gui/crusher_gui.png");
    public CrusherScreen(CrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int secondsRemaining = menu.blockEntity.data.get(0) / 20;
        int mins = secondsRemaining / 60;
        int secs = secondsRemaining % 60;
        String timeBurn = Component.translatable("gui.industry_mod.time_process", mins, secs).getString();
        graphics.drawString(font, timeBurn, x + 7, y + 62, CommonColors.DARK_GRAY, false);
        String enertickCount = Component.translatable("gui.ingustry_mod.time_enertick_used", menu.blockEntity.data.get(2)).getString();
        graphics.drawString(font, enertickCount, x + 7, y + 54, CommonColors.SOFT_YELLOW, false);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
