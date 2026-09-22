// net.fxrydarmament.testmod.network.ModNetworking.java
package net.fxrydarmament.kcsa.network;

import net.fxrydarmament.kcsa.firearm.network.FireArmNetworking;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "fxrydarmament")
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        FireArmNetworking.register(registrar);
        // ArmorNetworking.register(registrar);   <- nanti kalau perlu
        // MechaNetworking.register(registrar);   <- nanti kalau perlu
    }
}