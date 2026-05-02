package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnCleaveRushVfxPayload(double x, double y, double z, boolean isFinalHit) implements CustomPacketPayload {
    public static final Type<SpawnCleaveRushVfxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_cleave_rush_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnCleaveRushVfxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SpawnCleaveRushVfxPayload::x,
            ByteBufCodecs.DOUBLE, SpawnCleaveRushVfxPayload::y,
            ByteBufCodecs.DOUBLE, SpawnCleaveRushVfxPayload::z,
            ByteBufCodecs.BOOL, SpawnCleaveRushVfxPayload::isFinalHit,
            SpawnCleaveRushVfxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
