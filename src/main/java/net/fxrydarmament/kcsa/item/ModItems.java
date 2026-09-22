package net.fxrydarmament.kcsa.item;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.fxrydarmament.kcsa.ammo.AmmoItem;
import net.fxrydarmament.kcsa.firearm.FireArmItem;
import net.fxrydarmament.kcsa.item.custom.HeavyPilotArmor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

//Item Register
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FXRYDArmament.MOD_ID);


    //Register TUNGSTEN CARBIDE COMPOSITE
    public static final DeferredItem<Item> TUNGSTEN_CARBIDE_COMPOSITE = ITEMS.register("tungsten_carbide",
            () -> new Item(new Item.Properties()));

    //Register BORON CARBIDE COMPOSITE
    public static final DeferredItem<Item> BORON_CARBIDE_COMPOSITE = ITEMS.register("boron_carbide",
            () -> new Item(new Item.Properties()));

    //Register Scheelite Crystal
    public static final DeferredItem<Item> SCHEELITE_CRYSTAL = ITEMS.register("scheelite_crystal",
            () -> new Item(new Item.Properties()));


    //Register TUNGSTEN POWDER
    public static final DeferredItem<Item> TUNGSTEN_POWDER = ITEMS.register("tungsten_powder",
            () -> new Item(new Item.Properties()));

    //Register COAL POWDER
    public static final DeferredItem<Item> COAL_POWDER = ITEMS.register("coal_powder",
            () -> new Item(new Item.Properties()));

    //Register TUNGSTEN CARBIDE POWDER
    public static final DeferredItem<Item> TUNGSTEN_CARBIDE_POWDER = ITEMS.register("tungsten_carbide_powder",
            () -> new Item(new Item.Properties()));



    // Firearms
    public static final DeferredItem<FireArmItem> FIREARM = ITEMS.registerItem(
            "firearm",
            properties -> new FireArmItem(
                    properties.stacksTo(1)
            )
    );

    // Ammo
    public static final DeferredItem<AmmoItem> AMMO = ITEMS.registerItem(
            "ammo",
            properties -> new AmmoItem(properties.stacksTo(99))
    );




    //ARMOR ITEMS

    //Register STANDARD INFANTRY HELMET
    public static final DeferredItem<Item> HEAVY_PILOT_HELMET = ITEMS.register("heavy_pilot_helmet",
            () -> new HeavyPilotArmor(ModArmorMaterials.HEAVY_PILOT_ARMOR, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    //Register STANDARD INFANTRY CHESTPLATE
    public static final DeferredItem<Item> HEAVY_PILOT_CHESTPLATE = ITEMS.register("heavy_pilot_chestplate",
            () -> new HeavyPilotArmor(ModArmorMaterials.HEAVY_PILOT_ARMOR, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    //Register STANDARD INFANTRY LEGGINGS
    public static final DeferredItem<Item> HEAVY_PILOT_LEGGINGS = ITEMS.register("heavy_pilot_leggings",
            () -> new HeavyPilotArmor(ModArmorMaterials.HEAVY_PILOT_ARMOR, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    //Register STANDARD INFANTRY BOOTS
    public static final DeferredItem<Item> HEAVY_PILOT_BOOTS = ITEMS.register("heavy_pilot_boots",
            () -> new HeavyPilotArmor(ModArmorMaterials.HEAVY_PILOT_ARMOR, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));





    //Item Registerer
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}