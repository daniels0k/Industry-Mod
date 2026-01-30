package com.daniels0k.industry_mod;

import com.daniels0k.industry_mod.api.capabilities.EnergyCapabilities;
import com.daniels0k.industry_mod.block.ModBlockEntities;
import com.daniels0k.industry_mod.block.ModBlocks;
import com.daniels0k.industry_mod.block.cable_winder.CableWinderEntityRenderer;
import com.daniels0k.industry_mod.block.connector.copper.WireConnectEntityRenderer;
import com.daniels0k.industry_mod.screen.ModMenuTypes;
import com.daniels0k.industry_mod.screen.cable_winder.CableWinderScreen;
import com.daniels0k.industry_mod.screen.coal_generator.CoalGeneratorScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = IndustryMod.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = IndustryMod.MOD_ID, value = Dist.CLIENT)
public class IndustryModClient {
    public IndustryModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CABLE_WINDER.get(), CableWinderEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WIRE_COPPER_CONNECT.get(), WireConnectEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CABLE_WINDER_MENU.get(), CableWinderScreen::new);
        event.register(ModMenuTypes.COAL_GENERATOR_MENU.get(), CoalGeneratorScreen::new);
    }

    @SubscribeEvent
    public static void onItemToolTip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String text;

        if(stack.is(ModBlocks.VAULT_ENERTICK.asItem())) {
            text = Component.translatable("block.industry_mod.vault_enertick.desc0").getString();
            event.getToolTip().add(1, Component.literal(text));
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EnergyCapabilities.EnerTickStorage.BLOCK,
                ModBlockEntities.VAULT_ENERTICK.get(), (be, side) -> be.energyET);

        event.registerBlockEntity(EnergyCapabilities.EnerTickStorage.BLOCK,
                ModBlockEntities.WIRE_COPPER_CONNECT.get(), (be, side) -> be.energyET);
    }
}
