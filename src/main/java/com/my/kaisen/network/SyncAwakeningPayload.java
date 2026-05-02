package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncAwakeningPayload(int playerId, boolean isAwakened) implements CustomPacketPayload {

    public static final Type<SyncAwakeningPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "sync_awakening"));

    public static final StreamCodec<FriendlyByteBuf, SyncAwakeningPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncAwakeningPayload::playerId,
            ByteBufCodecs.BOOL,
            SyncAwakeningPayload::isAwakened,
            SyncAwakeningPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
