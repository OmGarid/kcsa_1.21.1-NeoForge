package net.fxrydarmament.kcsa.firearm.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UnloadRequestPayload() implements CustomPacketPayload {

    public static final Type<UnloadRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("fxrydarmament", "unload_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnloadRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new UnloadRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}