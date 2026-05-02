package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client to spawn Lodestone VFX for Crushing Blow at a specific world position.
 *
 * @param x World X coordinate of the effect origin.
 * @param y World Y coordinate of the effect origin.
 * @param z World Z coordinate of the effect origin.
 */
public record SpawnCrushingBlowVfxPayload(double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<SpawnCrushingBlowVfxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_crushing_blow_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnCrushingBlowVfxPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SpawnCrushingBlowVfxPayload::x,
                    ByteBufCodecs.DOUBLE, SpawnCrushingBlowVfxPayload::y,
                    ByteBufCodecs.DOUBLE, SpawnCrushingBlowVfxPayload::z,
                    SpawnCrushingBlowVfxPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
