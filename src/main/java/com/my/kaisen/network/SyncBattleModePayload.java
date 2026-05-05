package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncBattleModePayload(boolean battleMode) implements CustomPacketPayload {
    public static final Type<SyncBattleModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "sync_battle_mode"));

    public static final StreamCodec<FriendlyByteBuf, SyncBattleModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncBattleModePayload::battleMode,
            SyncBattleModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
