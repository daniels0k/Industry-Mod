package com.daniels0k.industry_mod.block.connector.copper;

import com.daniels0k.industry_mod.configurations.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class WireCopperConnectEntityRenderer implements BlockEntityRenderer<WireCopperConnectBlockEntity> {
    private static final ResourceLocation COPPER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/copper_block.png");
    public WireCopperConnectEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WireCopperConnectBlockEntity wireCopperConnectBE, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
        if(wireCopperConnectBE.connections.isEmpty() || Config.RENDER_CONNECTION_CABLE.isFalse()) return;

        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(COPPER_TEXTURE));
        BlockPos posA = wireCopperConnectBE.getBlockPos();

        for(WireCopperConnectBlockEntity.ConnectionData data : wireCopperConnectBE.connections.values()) {
            BlockPos posB = data.pos();

            double dx = posB.getX() - posA.getX();
            double dy = posB.getY() - posA.getY();
            double dz = posB.getZ() - posA.getZ();

            poseStack.pushPose();
            poseStack.translate(0.5, 0.81, 0.5);
            renderCable(poseStack, builder, (float) dx, (float) dy, (float) dz, packedLight);
            poseStack.popPose();
        }
    }

    private void renderCable(PoseStack poseStack, VertexConsumer builder, float x, float y, float z, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        float g = 0.03f;

        addVertex(builder, matrix, -g, g, 0, packedLight);
        addVertex(builder, matrix, g, g, 0, packedLight);
        addVertex(builder, matrix, x + g, y + g, z, packedLight);
        addVertex(builder, matrix, x - g, y + g, z, packedLight);

        addVertex(builder, matrix, -g, -g, 0, packedLight);
        addVertex(builder, matrix, x - g, y - g, z, packedLight);
        addVertex(builder, matrix, x + g, y - g, z, packedLight);
        addVertex(builder, matrix, g, -g, 0, packedLight);

        addVertex(builder, matrix, -g, -g, 0, packedLight);
        addVertex(builder, matrix, -g, g, 0, packedLight);
        addVertex(builder, matrix, x - g, y + g, z, packedLight);
        addVertex(builder, matrix, x - g, y - g, z, packedLight);

        addVertex(builder, matrix, g, -g, 0, packedLight);
        addVertex(builder, matrix, x + g, y - g, z, packedLight);
        addVertex(builder, matrix, x + g, y + g, z, packedLight);
        addVertex(builder, matrix, g, g, 0, packedLight);
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, int packedLight) {
        builder.addVertex(matrix, x, y, z)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0, 1, 0);
    }

    @Override
    public AABB getRenderBoundingBox(WireCopperConnectBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(Config.CABLE_RENDERING_ROTATION.get());
    }
}
