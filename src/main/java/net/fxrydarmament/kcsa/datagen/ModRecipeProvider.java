package net.fxrydarmament.kcsa.datagen;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.fxrydarmament.kcsa.block.ModBlocks;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {






        //Shaped Crafting - Boron Carbide Composite Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BORON_CARBIDE_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.BORON_CARBIDE_COMPOSITE.get())
                .unlockedBy("has_boron_carbide", has(ModItems.BORON_CARBIDE_COMPOSITE)).save(recipeOutput);

        //Shapeless Crafting - Boron Carbide Composite
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BORON_CARBIDE_COMPOSITE.get(), 9)
                .requires(ModBlocks.BORON_CARBIDE_BLOCK)
                .unlockedBy("has_boron_carbide_block", has(ModBlocks.BORON_CARBIDE_BLOCK)).save(recipeOutput);

        //Shaped Crafting - Tungsten Carbide Composite Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TUNGSTEN_CARBIDE_BLOCK.get())
                .pattern("TTT")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', ModItems.TUNGSTEN_CARBIDE_COMPOSITE.get())
                .unlockedBy("has_tungsten_carbide", has(ModItems.TUNGSTEN_CARBIDE_COMPOSITE)).save(recipeOutput);

        //Shapeless Crafting - Tungsten Carbide Composite
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.TUNGSTEN_CARBIDE_COMPOSITE.get(), 9)
                .requires(ModBlocks.TUNGSTEN_CARBIDE_BLOCK)
                .unlockedBy("has_tungsten_carbide_block", has(ModBlocks.TUNGSTEN_CARBIDE_BLOCK)).save(recipeOutput);






    }
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, FXRYDArmament.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
