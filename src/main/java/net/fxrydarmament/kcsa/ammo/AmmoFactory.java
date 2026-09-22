package net.fxrydarmament.kcsa.ammo;

import net.fxrydarmament.kcsa.ammo.component.AmmoComponents;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class AmmoFactory {

    private AmmoFactory() {
    }

    public static ItemStack create(ResourceLocation ammoId) {
        return create(ammoId, 1);
    }

    public static ItemStack create(ResourceLocation ammoId, int count) {
        AmmoData data = AmmoDataLoader.get(ammoId);

        if (data == null) {
            throw new IllegalArgumentException("Unknown ammo: " + ammoId);
        }

        ItemStack stack = new ItemStack(ModItems.AMMO.get(), count);

        stack.set(
                AmmoComponents.AMMO_ID.get(),
                ammoId
        );

        return stack;
    }
}