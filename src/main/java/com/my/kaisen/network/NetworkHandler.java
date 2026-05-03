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

        // Register SelectCharacterPayload to be sent from Client to Server
        registrar.playToServer(
                SelectCharacterPayload.TYPE,
                SelectCharacterPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                            // Find the CharacterChooserItem in inventory and consume it
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                                if (stack.getItem() instanceof com.my.kaisen.item.CharacterChooserItem) {
                                    stack.shrink(1);
                                    break;
                                }
                            }

                            // Save chosen character ID to persistent data
                            player.getPersistentData().putInt("mykaisen_character", payload.characterId());

                            // Confirm choice via chat
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You have chosen the Sorcerer path."));
                        }
                    });
                }
        );

        // Register ToggleBattleModePayload to be sent from Client to Server
        registrar.playToServer(
                ToggleBattleModePayload.TYPE,
                ToggleBattleModePayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                            boolean currentMode = player.getPersistentData().getBoolean("mykaisen_battle_mode");
                            // Default to true if not set, then toggle
                            boolean newMode = !currentMode;
                            if (!player.getPersistentData().contains("mykaisen_battle_mode")) {
                                newMode = false; // If it was never set, assume we were in battle mode (default) and go to play mode? 
                                // Actually let's assume default is BATTLE MODE (true). So toggle makes it FALSE.
                            }
                            player.getPersistentData().putBoolean("mykaisen_battle_mode", newMode);
                            String modeName = newMode ? "BATTLE" : "PLAYING";
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Mode switched to: " + modeName));
                        }
                    });
                }
        );

        // Register AwakenPayload to be sent from Client to Server
        registrar.playToServer(
                AwakenPayload.TYPE,
                AwakenPayload.STREAM_CODEC,
                AbilityServerHandler::handleAwaken
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

        // Register SpawnCursedStrikesVfxPayload to be sent from Server to Client (Lodestone VFX trigger)
        registrar.playToClient(
                SpawnCursedStrikesVfxPayload.TYPE,
                SpawnCursedStrikesVfxPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleCursedStrikesVfx(payload, ctx)
        );

        // Register SpawnCrushingBlowVfxPayload to be sent from Server to Client (Lodestone VFX trigger)
        registrar.playToClient(
                SpawnCrushingBlowVfxPayload.TYPE,
                SpawnCrushingBlowVfxPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleCrushingBlowVfx(payload, ctx)
        );

        // Register CameraShakePayload to be sent from Server to the attacking Client only
        registrar.playToClient(
                CameraShakePayload.TYPE,
                CameraShakePayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleCameraShake(payload, ctx)
        );

        // Register SpawnAwakeningVfxPayload to be sent from Server to Client
        registrar.playToClient(
                SpawnAwakeningVfxPayload.TYPE,
                SpawnAwakeningVfxPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleAwakeningVfx(payload, ctx)
        );

        // Register SpawnDismantleVfxPayload to be sent from Server to Client
        registrar.playToClient(
                SpawnDismantleVfxPayload.TYPE,
                SpawnDismantleVfxPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleDismantleVfx(payload, ctx)
        );

        // Register SpawnCleaveRushVfxPayload to be sent from Server to Client
        registrar.playToClient(
                SpawnCleaveRushVfxPayload.TYPE,
                SpawnCleaveRushVfxPayload.STREAM_CODEC,
                (payload, ctx) -> com.my.kaisen.client.ClientVfxHandler.handleCleaveRushVfx(payload, ctx)
        );

        // Register SyncAwakeningPayload to be sent from Server to Client
        registrar.playToClient(
                SyncAwakeningPayload.TYPE,
                SyncAwakeningPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.level != null && mc.level.getEntity(payload.playerId()) instanceof net.minecraft.world.entity.player.Player target) {
                            target.getPersistentData().putBoolean("is_awakened", payload.isAwakened());
                        }
                    });
                }
        );
        
        // Register TriggerDomainPayload to be sent from Client to Server
        registrar.playToServer(
                TriggerDomainPayload.TYPE,
                TriggerDomainPayload.STREAM_CODEC,
                AbilityServerHandler::handleDomain
        );

        // Register TriggerFugaPayload to be sent from Client to Server
        registrar.playToServer(
                TriggerFugaPayload.TYPE,
                TriggerFugaPayload.STREAM_CODEC,
                AbilityServerHandler::handleFuga
        );

        // Register TriggerM1Payload to be sent from Client to Server
        registrar.playToServer(
                TriggerM1Payload.TYPE,
                TriggerM1Payload.STREAM_CODEC,
                AbilityServerHandler::handleM1
        );
    }
}
