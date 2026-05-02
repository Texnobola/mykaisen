package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnAwakeningVfxPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SpawnAwakeningVfxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_awakening_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnAwakeningVfxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SpawnAwakeningVfxPayload::x,
            ByteBufCodecs.DOUBLE, SpawnAwakeningVfxPayload::y,
            ByteBufCodecs.DOUBLE, SpawnAwakeningVfxPayload::z,
            SpawnAwakeningVfxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
