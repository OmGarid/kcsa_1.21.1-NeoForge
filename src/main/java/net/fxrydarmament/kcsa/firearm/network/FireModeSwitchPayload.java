
package net.fxrydarmament.kcsa.firearm.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record FireModeSwitchPayload() implements CustomPacketPayload {
    public static final Type<FireModeSwitchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("fxrydarmament", "fire_mode_switch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireModeSwitchPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, FireModeSwitchPayload payload) {
            // Signal packet: Write nothing
        }

        @Override
        public FireModeSwitchPayload decode(RegistryFriendlyByteBuf buf) {
            // Signal packet: Return new instance
            return new FireModeSwitchPayload();
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}