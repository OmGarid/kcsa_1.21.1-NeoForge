package net.fxrydarmament.kcsa.datagen;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FXRYDArmament.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        //Raw Materials
        basicItem(ModItems.SCHEELITE_CRYSTAL.get());
        basicItem(ModItems.COAL_POWDER.get());
        basicItem(ModItems.TUNGSTEN_POWDER.get());
        basicItem(ModItems.TUNGSTEN_CARBIDE_POWDER.get());

        //Ingots
        basicItem(ModItems.BORON_CARBIDE_COMPOSITE.get());
        basicItem(ModItems.TUNGSTEN_CARBIDE_COMPOSITE.get());

        //Armor Items
        basicItem(ModItems.HEAVY_PILOT_HELMET.get());
        basicItem(ModItems.HEAVY_PILOT_CHESTPLATE.get());
        basicItem(ModItems.HEAVY_PILOT_LEGGINGS.get());
        basicItem(ModItems.HEAVY_PILOT_BOOTS.get());
    }
}
