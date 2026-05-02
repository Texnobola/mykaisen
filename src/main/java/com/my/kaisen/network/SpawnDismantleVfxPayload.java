package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpawnDismantleVfxPayload(double x, double y, double z, float yRot) implements CustomPacketPayload {
    public static final Type<SpawnDismantleVfxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_dismantle_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnDismantleVfxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SpawnDismantleVfxPayload::x,
            ByteBufCodecs.DOUBLE, SpawnDismantleVfxPayload::y,
            ByteBufCodecs.DOUBLE, SpawnDismantleVfxPayload::z,
            ByteBufCodecs.FLOAT, SpawnDismantleVfxPayload::yRot,
            SpawnDismantleVfxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
