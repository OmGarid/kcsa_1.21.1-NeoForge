package net.fxrydarmament.kcsa.firearm.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReloadRequestPayload() implements CustomPacketPayload {

    public static final Type<ReloadRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    "fxrydarmament",
                    "reload_request"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new ReloadRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}