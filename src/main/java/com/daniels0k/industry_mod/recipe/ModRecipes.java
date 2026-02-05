package com.daniels0k.industry_mod.recipe;

import com.daniels0k.industry_mod.IndustryMod;
import com.daniels0k.industry_mod.recipe.crushing.CrusherRecipe;
import com.daniels0k.industry_mod.recipe.crushing.CrusherRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
            Registries.RECIPE_TYPE, IndustryMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(
            Registries.RECIPE_SERIALIZER, IndustryMod.MOD_ID);
    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORY = DeferredRegister.create(
            Registries.RECIPE_BOOK_CATEGORY, IndustryMod.MOD_ID);

    public static final Supplier<RecipeBookCategory> CRUSHER_BOOK_CATEGORY = RECIPE_BOOK_CATEGORY.register(
            "crushing", RecipeBookCategory::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrusherRecipe>> CRUSHER_RECIPE_SERIALIZER = RECIPE_SERIALIZER.register("crushing",
            CrusherRecipeSerializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrusherRecipe>> CRUSHER_RECIPE = RECIPE_TYPES.register("crushing",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "industry_mod:crushing";
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZER.register(eventBus);
        RECIPE_BOOK_CATEGORY.register(eventBus);
    }
}
