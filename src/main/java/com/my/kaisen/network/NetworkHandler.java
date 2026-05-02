package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Register the payload handler on protocol version "1"
        final PayloadRegistrar registrar = event.registrar("1");

        // Register our AbilityPayload to be sent from Client to Server, and handled by AbilityServerHandler
        registrar.playToServer(
                AbilityPayload.TYPE,
                AbilityPayload.STREAM_CODEC,
                AbilityServerHandler::handle
        );

        // Register our PlayAnimationPayload to be sent from Server to Client
        registrar.playToClient(
                PlayAnimationPayload.TYPE,
                PlayAnimationPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientAnimationHandler.handleAnimation(payload, ctx)
        );

        // Register SpawnBlackFlashPayload to be sent from Server to Client (Lodestone VFX trigger)
        registrar.playToClient(
                SpawnBlackFlashPayload.TYPE,
                SpawnBlackFlashPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleBlackFlashVfx(payload, ctx)
        );

        // Register SpawnDivergentAuraPayload to be sent from Server to Client (Lodestone VFX trigger)
        registrar.playToClient(
                SpawnDivergentAuraPayload.TYPE,
                SpawnDivergentAuraPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleDivergentAuraVfx(payload, ctx)
        );

        // Register CameraShakePayload to be sent from Server to the attacking Client only
        registrar.playToClient(
                CameraShakePayload.TYPE,
                CameraShakePayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleCameraShake(payload, ctx)
        );
    }
}
