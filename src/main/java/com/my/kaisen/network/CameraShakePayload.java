package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client (to the attacker only) to trigger a camera shake.
 *
 * @param intensity     Max angular offset in degrees (e.g. 5.0f for heavy, 2.0f for light).
 * @param durationTicks How many ticks the shake lasts (e.g. 8 for a short heavy punch).
 */
public record CameraShakePayload(float intensity, int durationTicks)
        implements CustomPacketPayload {

    public static final Type<CameraShakePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "camera_shake"));

    public static final StreamCodec<FriendlyByteBuf, CameraShakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, CameraShakePayload::intensity,
                    ByteBufCodecs.INT,   CameraShakePayload::durationTicks,
                    CameraShakePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
