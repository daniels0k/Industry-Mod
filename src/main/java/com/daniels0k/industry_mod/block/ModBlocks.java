package com.daniels0k.industry_mod.block;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.block.cable_winder.CableWinder;
import com.daniels0k.industry_mod.block.coal_generator.CoalGenerator;
import com.daniels0k.industry_mod.block.connector.WireConnect;
import com.daniels0k.industry_mod.block.crusher.Crusher;
import com.daniels0k.industry_mod.block.vault_energy.enertick.VaultEnertick;
import com.daniels0k.industry_mod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IndustryMod.MOD_ID);

    public static final DeferredBlock<Block> CASE_MACHINE_BASIC = registerBlock("case_machine_basic",
            registryName -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.IRON)), true);

    public static final DeferredBlock<Block> WIRE_COPPER_CONNECT = registerBlock("wire_copper_connect",
            registryName -> new WireConnect(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .sound(SoundType.COPPER)
                    .strength(4.0f, 3.5f)
                    .requiresCorrectToolForDrops()), true);

    public static final DeferredBlock<Block> CABLE_WINDER = registerBlock("cable_winder",
            registryName -> new CableWinder(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(4.0f, 3.0f)
                    .sound(SoundType.IRON)
                    .noOcclusion()), true);

    public static final DeferredBlock<Block> COAL_GENERATOR = registerBlock("coal_generator",
            registryName -> new CoalGenerator(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(3.0f, 3.5f)
                    .sound(SoundType.IRON)
                    .requiresCorrectToolForDrops()), true);

    public static final DeferredBlock<VaultEnertick> VAULT_ENERTICK = registerBlock("vault_enertick",
            registryName -> new VaultEnertick(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(3.0f, 3.0f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()), true);

    public static final DeferredBlock<Crusher> CRUSHER = registerBlock("crusher",
            registryName -> new Crusher(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .strength(2.0f, 2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()), true);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<ResourceLocation, T> block, boolean isItemBlcok) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        if(isItemBlcok) {
            registerBlockItem(name, toReturn);
        }
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerSimpleBlockItem(name, block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
