package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TriggerFugaPayload() implements CustomPacketPayload {
    public static final Type<TriggerFugaPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "trigger_fuga"));
    public static final StreamCodec<FriendlyByteBuf, TriggerFugaPayload> STREAM_CODEC = StreamCodec.unit(new TriggerFugaPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
