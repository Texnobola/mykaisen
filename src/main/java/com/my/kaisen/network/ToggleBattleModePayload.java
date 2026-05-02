package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleBattleModePayload() implements CustomPacketPayload {
    public static final Type<ToggleBattleModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "toggle_battle_mode"));

    public static final StreamCodec<FriendlyByteBuf, ToggleBattleModePayload> STREAM_CODEC = StreamCodec.unit(new ToggleBattleModePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
