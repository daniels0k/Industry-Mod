package com.daniels0k.industry_mod.block.cable_winder;

import com.daniels0k.industry_mod.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class CableWinderEntityRenderer implements BlockEntityRenderer<CableWinderBlockEntity> {
    public CableWinderEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableWinderBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stack = blockEntity.inventory.getStackInSlot(0);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.95, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.ZP.rotation(89.58f));

        if(blockEntity.inventory.getStackInSlot(1).is(ModItems.COPPER_WIRE)) {
            poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.getRenderingRotation()));
        }
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, bufferSource, blockEntity.getLevel(), 1);
        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
