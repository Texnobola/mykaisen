package com.my.kaisen.network;
 
import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
 
public record SpawnDomainAshPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SpawnDomainAshPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_domain_ash"));
 
    public static final StreamCodec<FriendlyByteBuf, SpawnDomainAshPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SpawnDomainAshPayload::x,
            ByteBufCodecs.DOUBLE, SpawnDomainAshPayload::y,
            ByteBufCodecs.DOUBLE, SpawnDomainAshPayload::z,
            SpawnDomainAshPayload::new
    );
 
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
