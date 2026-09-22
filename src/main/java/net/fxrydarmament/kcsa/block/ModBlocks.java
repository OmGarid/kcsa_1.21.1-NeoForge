package net.fxrydarmament.kcsa.block;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FXRYDArmament.MOD_ID);



    //Register TUNGSTEN CARBIDE BLOCK
    public static final DeferredBlock<Block> TUNGSTEN_CARBIDE_BLOCK = registerBlock("tungsten_carbide_block", () -> new Block(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));

    //Register BORON CARBIDE BLOCK
    public static final DeferredBlock<Block> BORON_CARBIDE_BLOCK = registerBlock("boron_carbide_block", () -> new Block(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    //Register SCHEELITE ORE
    public static final DeferredBlock<Block> SCHEELITE_ORE = registerBlock("scheelite_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    //Register SCHEELITE DEEPSLATE ORE
    public static final DeferredBlock<Block> SCHEELITE_DEEPSLATE_ORE = registerBlock("scheelite_deepslate_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)));




    //Block Registerer
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    //Create Block Items
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
