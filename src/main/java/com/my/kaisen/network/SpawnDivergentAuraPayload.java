package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent Server -> Client to spawn a Lodestone-based Divergent Fist Aura at a specific world position.
 *
 * @param x World X coordinate of the effect origin.
 * @param y World Y coordinate of the effect origin.
 * @param z World Z coordinate of the effect origin.
 */
public record SpawnDivergentAuraPayload(double x, double y, double z)
        implements CustomPacketPayload {

    public static final Type<SpawnDivergentAuraPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_divergent_aura"));

    public static final StreamCodec<FriendlyByteBuf, SpawnDivergentAuraPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, SpawnDivergentAuraPayload::x,
                    ByteBufCodecs.DOUBLE, SpawnDivergentAuraPayload::y,
                    ByteBufCodecs.DOUBLE, SpawnDivergentAuraPayload::z,
                    SpawnDivergentAuraPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
