package net.fxrydarmament.kcsa.datagen;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.fxrydarmament.kcsa.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FXRYDArmament.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.BORON_CARBIDE_BLOCK);
        blockWithItem(ModBlocks.TUNGSTEN_CARBIDE_BLOCK);

        blockWithItem(ModBlocks.SCHEELITE_ORE);
        blockWithItem(ModBlocks.SCHEELITE_DEEPSLATE_ORE);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}
