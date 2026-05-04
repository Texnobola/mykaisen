package com.my.kaisen.network;
 
import com.my.kaisen.MyKaisen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
 
public record TriggerDomainPayload(boolean isOpenBarrier) implements CustomPacketPayload {
    public static final Type<TriggerDomainPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "trigger_domain"));
 
    public static final StreamCodec<FriendlyByteBuf, TriggerDomainPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TriggerDomainPayload::isOpenBarrier,
            TriggerDomainPayload::new
    );
 
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
