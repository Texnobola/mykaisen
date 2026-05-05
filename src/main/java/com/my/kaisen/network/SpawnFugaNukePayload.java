package com.my.kaisen.network;
 
import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
 
public record SpawnFugaNukePayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SpawnFugaNukePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spawn_fuga_nuke"));
 
    public static final StreamCodec<FriendlyByteBuf, SpawnFugaNukePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
            },
            buf -> new SpawnFugaNukePayload(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );
 
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
