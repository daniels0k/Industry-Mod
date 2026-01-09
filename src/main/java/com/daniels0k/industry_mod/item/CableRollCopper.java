package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.block.connector.copper.EnumModeWireCopperConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireCopperConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireCopperConnectBlockEntity;
import com.daniels0k.industry_mod.item.datacomponent.ModDataComponents;
import com.daniels0k.industry_mod.item.datacomponent.RouteDataComponent;
import com.daniels0k.industry_mod.item.datacomponent.WireDataComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CableRollCopper extends Item {
    public CableRollCopper(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.industry_mod.cable_roll.desc0"));
        RouteDataComponent routeData = stack.get(ModDataComponents.ROUTE_DATA);
        if(routeData != null && Screen.hasShiftDown()) {
            String infoDesc = Component.translatable("item.industry_mod.cable_roll.desc1").getString();
            infoDesc = infoDesc.replace("%wire_used%", String.valueOf(routeData.distanceMax()));
            infoDesc = infoDesc.replace("%efficiency%", String.valueOf(routeData.efficiency()));
            infoDesc = infoDesc.replace("%loss_factor%", String.valueOf(routeData.lossFactor()));
            infoDesc = infoDesc.replace("%type%", routeData.cableType());
            tooltip.accept(Component.literal(infoDesc));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();

        return InteractionResult.PASS;
    }

    private void spawnParticleRoute(Level level, BlockPos pointA, Vec3 lookPos, boolean valid) {
        if((level.getGameTime() % 25) != 0) return;
        BlockEntity blockEntity = level.getBlockEntity(new BlockPos((int)lookPos.x, (int)lookPos.y, (int)lookPos.z));

        double x1 = pointA.getX() + 0.5;
        double y1 = pointA.getY() + 0.5;
        double z1 = pointA.getZ() + 0.5;

        double x2 = lookPos.x;
        double y2 = lookPos.y + (blockEntity instanceof WireCopperConnectBlockEntity ? 0 : 1);
        double z2 = lookPos.z;

        int steps = 20;
        for(int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            int fromColor = valid ? 0x00FF00 : 0xFF0000;
            int toColor = 0xFFFFFF;
            float scale = 0.2F;
            ParticleOptions particle = new DustColorTransitionOptions(fromColor, toColor, scale);

            level.addParticle(particle, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if(slot == EquipmentSlot.MAINHAND) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if(player == null) return;

            if(!(stack.getItem() instanceof CableRollCopper)) return;

            RouteDataComponent route = stack.get(ModDataComponents.ROUTE_DATA);
            if(route == null) return;
            List<WireDataComponent> connections = route.connections();
            if(connections.isEmpty()) return;
            WireDataComponent wireData = connections.getFirst();
            if(wireData.pointA().isEmpty()) return;

            HitResult hit = mc.hitResult;
            if(hit instanceof BlockHitResult blockHit) {
                BlockPos lookPos = blockHit.getBlockPos();
                Vec3 lookVec = Vec3.atCenterOf(lookPos);

                boolean valid = false;
                if(player.level().getBlockEntity(lookPos) instanceof WireCopperConnectBlockEntity targetEntity) {
                    double distance = wireData.pointA().get().distManhattan(lookPos);
                    if(distance <= route.distanceMax()) {
                        EnumModeWireCopperConnect mode = player.level().getBlockState(lookPos).getValue(WireCopperConnect.MODE_CONNECT);
                        if(mode == EnumModeWireCopperConnect.MODE_INPUT || mode == EnumModeWireCopperConnect.MODE_OUTPUT) {
                            valid = true;
                        }
                    }
                }

                spawnParticleRoute(player.level(), wireData.pointA().get(), lookVec, valid);
            }
        }
    }
}
