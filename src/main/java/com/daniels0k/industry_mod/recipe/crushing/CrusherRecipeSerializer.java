package com.daniels0k.industry_mod.recipe.crushing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CrusherRecipeSerializer implements RecipeSerializer<CrusherRecipe> {
    public static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrusherRecipe::ingredient),
            Codec.INT.optionalFieldOf("inputCount", 1).forGetter(CrusherRecipe::inputCount),
            Codec.INT.fieldOf("enertick").forGetter(CrusherRecipe::enertick),
            Codec.INT.fieldOf("time").forGetter(CrusherRecipe::time),
            ItemStack.CODEC.listOf().fieldOf("results").forGetter(CrusherRecipe::results)
    ).apply(inst, CrusherRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrusherRecipe::ingredient,
            ByteBufCodecs.INT, CrusherRecipe::inputCount,
            ByteBufCodecs.INT, CrusherRecipe::enertick,
            ByteBufCodecs.INT, CrusherRecipe::time,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), CrusherRecipe::results,
            CrusherRecipe::new);

    @Override
    public MapCodec<CrusherRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
