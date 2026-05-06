package com.my.kaisen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.my.kaisen.MyKaisen;

public record SyncComboPayload(int combo) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncComboPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "sync_combo"));

    public static final StreamCodec<FriendlyByteBuf, SyncComboPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncComboPayload::combo,
            SyncComboPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
