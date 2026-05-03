package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TriggerM1Payload() implements CustomPacketPayload {
    public static final Type<TriggerM1Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "trigger_m1"));
    public static final StreamCodec<FriendlyByteBuf, TriggerM1Payload> STREAM_CODEC = StreamCodec.unit(new TriggerM1Payload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
