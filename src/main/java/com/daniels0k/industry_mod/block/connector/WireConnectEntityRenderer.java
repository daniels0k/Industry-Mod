package com.daniels0k.industry_mod.block.connector;

import com.daniels0k.industry_mod.configurations.Config;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class WireConnectEntityRenderer implements BlockEntityRenderer<WireConnectBlockEntity> {
    private static final ResourceLocation COPPER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/copper_block.png");
    public WireConnectEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WireConnectBlockEntity wireConnectBE, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPos) {
        if(wireConnectBE.connections.isEmpty() || Config.RENDER_CONNECTION_CABLE.isFalse()) return;

        VertexConsumer builder = bufferSource.getBuffer(RenderType.entityCutout(COPPER_TEXTURE));
        BlockPos posA = wireConnectBE.getBlockPos();
        Direction facingA = wireConnectBE.getBlockState().getValue(WireConnect.FACING);

        float offSet = 0.81f;
        float ax = 0.5f, ay = 0.5f, az = 0.5f;

        switch (facingA) {
            case DOWN -> ay = offSet;
            case UP -> ay = 1.0f - offSet;
            case SOUTH -> az = 1.0f - offSet;
            case NORTH -> az = offSet;
            case EAST -> ax = 1.0f - offSet;
            case WEST -> ax = offSet;
        }

        for(WireConnectBlockEntity.ConnectionData data : wireConnectBE.connections.values()) {
            BlockPos posB = data.pos();

            float bx = 0.5f, by = 0.5f, bz = 0.5f;
            BlockState blockStateB = wireConnectBE.getLevel().getBlockState(posB);
            if(blockStateB.hasProperty(WireConnect.FACING)) {
                Direction facingB = blockStateB.getValue(WireConnect.FACING);
                switch (facingB) {
                    case DOWN ->  by = offSet;
                    case UP -> by = 1.0f - offSet;
                    case SOUTH -> bz = 1.0f - offSet;
                    case NORTH ->  bz = offSet;
                    case EAST -> bx = 1.0f - offSet;
                    case WEST -> bx = offSet;
                }
            }

            float dx = (posB.getX() + bx) - (posA.getX() + ax);
            float dy = (posB.getY() + by) - (posA.getY() + ay);
            float dz = (posB.getZ() + bz) - (posA.getZ() + az);

            poseStack.pushPose();
            poseStack.translate(ax, ay, az);
            renderCable(poseStack, builder, dx, dy, dz, packedLight);
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
    public AABB getRenderBoundingBox(WireConnectBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(Config.CABLE_RENDERING_ROTATION.get());
    }
}
