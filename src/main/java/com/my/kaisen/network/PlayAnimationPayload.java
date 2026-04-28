package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayAnimationPayload(String animationName, int playerId) implements CustomPacketPayload {

    public static final Type<PlayAnimationPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "play_animation"));

    public static final StreamCodec<FriendlyByteBuf, PlayAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PlayAnimationPayload::animationName,
            ByteBufCodecs.INT,
            PlayAnimationPayload::playerId,
            PlayAnimationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
