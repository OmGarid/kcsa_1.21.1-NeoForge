package net.fxrydarmament.kcsa.datacomponent;

import net.fxrydarmament.kcsa.FXRYDArmament;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, FXRYDArmament.MOD_ID);
}