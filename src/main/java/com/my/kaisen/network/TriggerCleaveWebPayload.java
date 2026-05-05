package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TriggerCleaveWebPayload() implements CustomPacketPayload {
    public static final Type<TriggerCleaveWebPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "trigger_cleave_web"));

    public static final StreamCodec<FriendlyByteBuf, TriggerCleaveWebPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> new TriggerCleaveWebPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
