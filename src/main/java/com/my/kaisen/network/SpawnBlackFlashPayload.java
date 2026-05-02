package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client to spawn a Lodestone-based Black Flash VFX at a specific world position.
 *
 * @param x World X coordinate of the effect origin.
 * @param y World Y coordinate of the effect origin.
 * @param z World Z coordinate of the effect origin.
 */
public record SpawnBlackFlashPayload(double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<SpawnBlackFlashPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_black_flash"));

    public static final StreamCodec<FriendlyByteBuf, SpawnBlackFlashPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SpawnBlackFlashPayload::x,
                    ByteBufCodecs.DOUBLE, SpawnBlackFlashPayload::y,
                    ByteBufCodecs.DOUBLE, SpawnBlackFlashPayload::z,
                    SpawnBlackFlashPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
