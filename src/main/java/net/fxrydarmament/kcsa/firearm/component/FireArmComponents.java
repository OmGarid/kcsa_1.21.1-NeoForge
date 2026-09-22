package net.fxrydarmament.kcsa.firearm.component;

import net.fxrydarmament.kcsa.datacomponent.ModDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class FireArmComponents {

    public static final Supplier<DataComponentType<FireArmState>> FIREARM_STATE =
            ModDataComponents.DATA_COMPONENTS.registerComponentType("firearm_state", builder -> builder
                    .persistent(FireArmState.CODEC)
                    .networkSynchronized(FireArmState.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<ResourceLocation>> FIREARM_ID =
            ModDataComponents.DATA_COMPONENTS.registerComponentType("firearm_id", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
            );
}