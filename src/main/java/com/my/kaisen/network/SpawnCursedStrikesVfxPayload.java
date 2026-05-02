package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client to spawn Lodestone VFX for Cursed Strikes at a specific world position.
 *
 * @param x World X coordinate of the effect origin.
 * @param y World Y coordinate of the effect origin.
 * @param z World Z coordinate of the effect origin.
 */
public record SpawnCursedStrikesVfxPayload(double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<SpawnCursedStrikesVfxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_cursed_strikes_vfx"));

    public static final StreamCodec<FriendlyByteBuf, SpawnCursedStrikesVfxPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SpawnCursedStrikesVfxPayload::x,
                    ByteBufCodecs.DOUBLE, SpawnCursedStrikesVfxPayload::y,
                    ByteBufCodecs.DOUBLE, SpawnCursedStrikesVfxPayload::z,
                    SpawnCursedStrikesVfxPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
