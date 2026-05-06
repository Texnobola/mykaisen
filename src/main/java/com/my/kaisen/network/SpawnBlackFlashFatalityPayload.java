package com.my.kaisen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.my.kaisen.MyKaisen;

public record SpawnBlackFlashFatalityPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SpawnBlackFlashFatalityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_black_flash_fatality"));

    public static final StreamCodec<FriendlyByteBuf, SpawnBlackFlashFatalityPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.x);
                buf.writeDouble(payload.y);
                buf.writeDouble(payload.z);
            },
            buf -> new SpawnBlackFlashFatalityPayload(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
