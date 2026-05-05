package com.my.kaisen.network;
 
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
 
public record SyncBlackFlashComboPayload(int combo) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncBlackFlashComboPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mykaisen", "sync_bf_combo"));
 
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBlackFlashComboPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncBlackFlashComboPayload::combo,
            SyncBlackFlashComboPayload::new
    );
 
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
