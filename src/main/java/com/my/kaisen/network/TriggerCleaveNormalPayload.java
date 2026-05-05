package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TriggerCleaveNormalPayload() implements CustomPacketPayload {
    public static final Type<TriggerCleaveNormalPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "trigger_cleave_normal"));

    public static final StreamCodec<FriendlyByteBuf, TriggerCleaveNormalPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {},
            buf -> new TriggerCleaveNormalPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
