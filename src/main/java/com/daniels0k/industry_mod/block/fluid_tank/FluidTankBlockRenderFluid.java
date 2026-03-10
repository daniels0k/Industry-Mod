package com.daniels0k.industry_mod.block.fluid_tank;

import com.daniels0k.industry_mod.IndustryMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;

import java.util.Set;

public class FluidTankBlockRenderFluid implements BlockEntityRenderer<FluidTankBlockEntity> {
    public FluidTankBlockRenderFluid(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(FluidTankBlockEntity tankBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
        FluidTank tank = tankBlockEntity.getTankOrigin(tankBlockEntity.getLevel());
        if (tank == null) return;

        FluidStack fluidStack = tank.getFluid();
        if (fluidStack.isEmpty()) return;

        FluidTankBlockEntity origin = tankBlockEntity.getOrigin(tankBlockEntity.getLevel());
        if (origin == null) return;

        Set<BlockPos> structure = origin.getConnectedTanks(tankBlockEntity.getLevel());
        if (structure.isEmpty()) return;

        int minY = structure.stream().mapToInt(BlockPos::getY).min().getAsInt();

        int thisY = tankBlockEntity.getBlockPos().getY();
        int blockIndex = thisY - minY;

        int capacityPerBlock = tankBlockEntity.getBaseCapacity();
        int fluidAmount = fluidStack.getAmount();

        int fullBlocks = fluidAmount / capacityPerBlock;
        int remainder = fluidAmount % capacityPerBlock;

        float minYRel = 0.025f;
        float maxYRel = 0.975f;
        float fluidHeight;

        if (blockIndex < fullBlocks) {
            fluidHeight = maxYRel;
        } else if (blockIndex == fullBlocks && remainder > 0) {
            float fillRatio = (float) remainder / capacityPerBlock;
            fluidHeight = minYRel + (maxYRel - minYRel) * fillRatio;
        } else {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite stillTexture = FluidSpriteCache.getSprite(fluidExtensions.getStillTexture());
        int color = fluidExtensions.getTintColor();

        int alpha = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        poseStack.pushPose();
        float minX = 0.01f;
        float maxX = 0.98f;
        float minZ = 0.01f;
        float maxZ = 0.98f;

        renderFluidCuboid(poseStack, bufferSource, stillTexture, r, g, b, alpha,
                minX, minYRel, minZ, maxX, fluidHeight, maxZ, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderFluidCuboid(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite, int r, int g, int b, int alpha,
                                   float x1, float y1, float z1, float x2, float y2, float z2, int packedLight, int packedOverlay) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        addVertex(builder, matrix, x1, y2, z1, r, g, b, alpha, 0, -1, 0, packedLight, u0, v0);
        addVertex(builder, matrix, x1, y2, z2, r, g, b, alpha, 0, 1, 0, packedLight, u0, v1);
        addVertex(builder, matrix, x2, y2, z2, r, g, b, alpha, 0, 1, 0, packedLight, u1, v1);
        addVertex(builder, matrix, x2, y2, z1, r, g, b, alpha, 0, 1, 0, packedLight, u1, v0);

        addVertex(builder, matrix, x1, y1, z1, r, g, b, alpha, 0, -1, 0, packedLight, u0, v0);
        addVertex(builder, matrix, x2, y1, z1, r, g, b, alpha, 0, -1, 0, packedLight, u1, v0);
        addVertex(builder, matrix, x2, y1, z2, r, g, b, alpha, 0, -1, 0, packedLight, u1, v1);
        addVertex(builder, matrix, x1, y1, z2, r, g, b, alpha, 0, -1, 0, packedLight, u0, v1);

        addVertex(builder, matrix, x1, y1, z1, r, g, b, alpha, 0, 0, -1, packedLight, u0, v0);
        addVertex(builder, matrix, x1, y2, z1, r, g, b, alpha, 0, 0, -1, packedLight, u0, v1);
        addVertex(builder, matrix, x2, y2, z1, r, g, b, alpha, 0, 0, -1, packedLight, u1, v1);
        addVertex(builder, matrix, x2, y1, z1, r, g, b, alpha, 0, 0, -1, packedLight, u1, v0);

        addVertex(builder, matrix, x1, y1, z2, r, g, b, alpha, 0, 0, 1, packedLight, u0, v0);
        addVertex(builder, matrix, x2, y1, z2, r, g, b, alpha, 0, 0, 1, packedLight, u1, v0);
        addVertex(builder, matrix, x2, y2, z2, r, g, b, alpha, 0, 0, 1, packedLight, u1, v1);
        addVertex(builder, matrix, x1, y2, z2, r, g, b, alpha, 0, 0, 1, packedLight, u0, v1);

        addVertex(builder, matrix, x1, y1, z1, r, g, b, alpha, -1, 0, 0, packedLight, u0, v0);
        addVertex(builder, matrix, x1, y1, z2, r, g, b, alpha, -1, 0, 0, packedLight, u0, v1);
        addVertex(builder, matrix, x1, y2, z2, r, g, b, alpha, -1, 0, 0, packedLight, u1, v1);
        addVertex(builder, matrix, x1, y2, z1, r, g, b, alpha, -1, 0, 0, packedLight, u1, v0);

        addVertex(builder, matrix, x2, y1, z1, r, g, b, alpha, 1, 0, 0, packedLight, u0, v0);
        addVertex(builder, matrix, x2, y2, z1, r, g, b, alpha, 1, 0, 0, packedLight, u1, v0);
        addVertex(builder, matrix, x2, y2, z2, r, g, b, alpha, 1, 0, 0, packedLight, u1, v1);
        addVertex(builder, matrix, x2, y1, z2, r, g, b, alpha, 1, 0, 0, packedLight, u0, v1);
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix,
                           float x, float y, float z,
                           int r, int g, int b, int alpha,
                           float nx, float ny, float nz,
                           int packedLight,
                           float u, float v) {
        builder.addVertex(matrix, x, y, z)
                .setColor(r, g, b, alpha)
                .setUv(u, v)
                .setUv2(packedLight, packedLight)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }
}
