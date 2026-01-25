package com.daniels0k.industry_mod.item;

import com.daniels0k.industry_mod.configurations.Config;
import com.daniels0k.industry_mod.block.connector.copper.EnumModeWireCopperConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireCopperConnect;
import com.daniels0k.industry_mod.block.connector.copper.WireCopperConnectBlockEntity;
import com.daniels0k.industry_mod.item.datacomponent.ModDataComponents;
import com.daniels0k.industry_mod.item.datacomponent.RouteDataComponent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
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
            String infoDesc = Component.translatable("item.industry_mod.cable_roll.desc1", routeData.distanceMax(),
                    routeData.efficiency(), routeData.lossFactor(), routeData.cableType()).getString();
            tooltip.accept(Component.literal(infoDesc));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos blockPos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        if(!(blockEntity instanceof WireCopperConnectBlockEntity copperWire)) return InteractionResult.PASS;
        BlockState blockState = copperWire.getBlockState();
        EnumModeWireCopperConnect mode = blockState.getValue(WireCopperConnect.MODE_CONNECT);
        if(mode == EnumModeWireCopperConnect.MODE_NONE) return InteractionResult.FAIL;

        RouteDataComponent route = stack.get(ModDataComponents.ROUTE_DATA);
        if(route == null) return InteractionResult.FAIL;

        if(route.connectionA().isEmpty()) {
            stack.set(ModDataComponents.ROUTE_DATA, new RouteDataComponent(Optional.of(blockPos), Optional.empty(),
                     route.cableType(), route.distanceMax(), route.efficiency(), route.lossFactor()));
            return InteractionResult.SUCCESS;
        }

        if(player == null) return InteractionResult.PASS;
        if(player.isShiftKeyDown()) {
            stack.set(ModDataComponents.ROUTE_DATA, new RouteDataComponent(Optional.empty(), Optional.empty(),
                    route.cableType(), route.distanceMax(), route.efficiency(), route.lossFactor()));
            return InteractionResult.SUCCESS;
        }

        BlockPos posA = route.connectionA().get();
        if(!(posA.distManhattan(blockPos) >= 1)) return InteractionResult.PASS;
        BlockEntity blockEntityA = level.getBlockEntity(posA);

        if(!(blockEntityA instanceof WireCopperConnectBlockEntity copperWireA)) return InteractionResult.PASS;
        if(copperWireA.parentsConnect.contains(blockPos) || copperWire.parentsConnect.contains(posA)) {
            player.displayClientMessage(Component.translatable("item.industry_mod.cable_roll.err_loop"), true);
            return InteractionResult.FAIL;
        }
        double distance = Math.sqrt(posA.distSqr(blockPos));
        int wireUsed = (int) Math.ceil(distance / 6.0);
        int wireCount = stack.getMaxDamage() - stack.getDamageValue();

        if(!(wireCount >= wireUsed)) {
            player.displayClientMessage(Component.translatable("item.industry_mod.cable_roll.err_wire", wireCount, wireUsed), true);
            return InteractionResult.FAIL;
        }

        stack.setDamageValue(stack.getDamageValue() + wireUsed);
        copperWireA.addConnection(blockPos, route.efficiency(), route.lossFactor(), wireUsed);
        stack.set(ModDataComponents.ROUTE_DATA, new RouteDataComponent(Optional.empty(), Optional.empty(),
                route.cableType(), route.distanceMax(), route.efficiency(), route.lossFactor()));
        return InteractionResult.SUCCESS;
    }

    private void spawnParticleRoute(Level level, BlockPos pointA, Vec3 lookPos, boolean valid, boolean isInsufficientWire) {
        if(level.getGameTime() % 25 != 0) return;

        double x1 = pointA.getX() + 0.5;
        double y1 = pointA.getY() + 0.5;
        double z1 = pointA.getZ() + 0.5;

        boolean isOptionHitBlockParticleUp = Config.HIT_BLOCK_PARTICLE_UP.isTrue();
        double upOffSet = valid ? 0 : isOptionHitBlockParticleUp ? 1 : 0;
        double x2 = lookPos.x;
        double y2 = lookPos.y + upOffSet;
        double z2 = lookPos.z;

        int steps = 20;
        for(int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            int fromColor = valid ? isInsufficientWire ? 0xFFAA00 : 0x00FF00 : 0xFF0000;
            int toColor = 0xFFFFFF;
            float scale = 0.5F;
            ParticleOptions particle = new DustColorTransitionOptions(fromColor, toColor, scale);

            level.addParticle(particle, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if(!(entity instanceof Player player)) return;
        dropGetItem(level, player, stack);

        if(slot == EquipmentSlot.MAINHAND) {
            Minecraft mc = Minecraft.getInstance();
            if(!(stack.getItem() instanceof CableRollCopper)) return;

            RouteDataComponent route = stack.get(ModDataComponents.ROUTE_DATA);
            if(route == null) return;
            if(route.connectionA().isEmpty()) return;

            HitResult hit = mc.hitResult;
            Player mcPlayer = mc.player;
            if(hit instanceof BlockHitResult blockHit) {
                BlockPos lookPos = blockHit.getBlockPos();
                Vec3 lookVec = Vec3.atCenterOf(lookPos);

                boolean valid = false;
                boolean isInsufficientWire = false;
                if(mcPlayer.level().getBlockEntity(lookPos) instanceof WireCopperConnectBlockEntity) {
                    EnumModeWireCopperConnect mode = mcPlayer.level().getBlockState(lookPos).getValue(WireCopperConnect.MODE_CONNECT);
                    if(mode == EnumModeWireCopperConnect.MODE_INPUT || mode == EnumModeWireCopperConnect.MODE_OUTPUT) {
                        valid = true;
                    }
                }
                int wireCount = stack.getMaxDamage() - stack.getDamageValue();
                double distance = Math.sqrt(route.connectionA().get().distSqr(lookPos));
                int wireUsed = (int) Math.ceil(distance / 6.0);
                if(!(wireCount >= wireUsed)) {
                    isInsufficientWire = true;
                }

                spawnParticleRoute(mcPlayer.level(), route.connectionA().get(), lookVec, valid, isInsufficientWire);
            }
        }
    }

    private void dropGetItem(Level level, Player player, ItemStack stack) {
        if(!level.isClientSide()) {
            if(!stack.isDamaged()) return;
            if(stack.getDamageValue() == stack.getMaxDamage()) {
                stack.shrink(1);
                if(!player.addItem(ModItems.CABLE_ROLL.toStack())) {
                    player.drop(ModItems.CABLE_ROLL.toStack(), false);
                }
            }
        }
    }
}
