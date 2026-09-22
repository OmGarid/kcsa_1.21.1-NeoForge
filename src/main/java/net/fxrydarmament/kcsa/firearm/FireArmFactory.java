package net.fxrydarmament.kcsa.firearm;

import net.fxrydarmament.kcsa.firearm.component.FireArmComponents;
import net.fxrydarmament.kcsa.firearm.component.FireArmState;
import net.fxrydarmament.kcsa.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FireArmFactory {

    private FireArmFactory() {
    }

    public static ItemStack create(ResourceLocation firearmId) {

        FireArmData data = FireArmDataLoader.get(firearmId);

        if (data == null) {
            throw new IllegalArgumentException(
                    "Unknown firearm: " + firearmId
            );
        }

        ItemStack stack = new ItemStack(ModItems.FIREARM.get());

        stack.set(
                FireArmComponents.FIREARM_ID.get(),
                firearmId
        );

        stack.set(
                FireArmComponents.FIREARM_STATE.get(),
                new FireArmState(
                        data.getMagazineCapacity(),
                        "semi"
                )
        );

        return stack;
    }
}