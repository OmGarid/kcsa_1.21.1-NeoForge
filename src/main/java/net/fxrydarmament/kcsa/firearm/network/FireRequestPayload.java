package net.fxrydarmament.kcsa.firearm.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FireRequestPayload() implements CustomPacketPayload {

    public static final Type<FireRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("fxrydarmament", "fire_request"));

    public static final StreamCodec<ByteBuf, FireRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new FireRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}