package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnCleaveVfxPayload(double x, double y, double z, float rotation) implements CustomPacketPayload {
    public static final Type<SpawnCleaveVfxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_cleave_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnCleaveVfxPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
                buf.writeFloat(payload.rotation());
            },
            buf -> new SpawnCleaveVfxPayload(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
