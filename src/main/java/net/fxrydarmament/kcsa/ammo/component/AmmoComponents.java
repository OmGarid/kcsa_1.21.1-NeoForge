package net.fxrydarmament.kcsa.ammo.component;

import net.fxrydarmament.kcsa.datacomponent.ModDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class AmmoComponents {

    public static final Supplier<DataComponentType<ResourceLocation>> AMMO_ID =
            ModDataComponents.DATA_COMPONENTS.registerComponentType("ammo_id", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
            );
}