package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AbilityPayload(int abilityId) implements CustomPacketPayload {

    // Unique identifier for the packet
    public static final Type<AbilityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "ability_payload"));

    // Codec to serialize and deserialize the payload
    public static final StreamCodec<FriendlyByteBuf, AbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AbilityPayload::abilityId,
            AbilityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
