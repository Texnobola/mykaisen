package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client to spawn a Photon VFX effect at a specific world position.
 *
 * @param effectName The ResourceLocation path (under "mykaisen" namespace) of the .fxproj effect.
 * @param x          World X coordinate of the effect origin.
 * @param y          World Y coordinate of the effect origin.
 * @param z          World Z coordinate of the effect origin.
 */
public record SpawnVfxPayload(String effectName, double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<SpawnVfxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnVfxPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SpawnVfxPayload::effectName,
                    ByteBufCodecs.DOUBLE,      SpawnVfxPayload::x,
                    ByteBufCodecs.DOUBLE,      SpawnVfxPayload::y,
                    ByteBufCodecs.DOUBLE,      SpawnVfxPayload::z,
                    SpawnVfxPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
