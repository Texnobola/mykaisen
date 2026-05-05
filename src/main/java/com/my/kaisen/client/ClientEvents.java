package com.my.kaisen.client;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.registry.ModEffects;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    @EventBusSubscriber(modid = "mykaisen", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (net.minecraft.client.resources.PlayerSkin.Model skin : net.minecraft.client.resources.PlayerSkin.Model.values()) {
                PlayerRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new SukunaTattooLayer(renderer));
                }
            }
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(com.my.kaisen.registry.ModEntities.FUGA_PROJECTILE.get(), FugaProjectileRenderer::new);
            event.registerEntityRenderer(com.my.kaisen.registry.ModEntities.SHRINE.get(), ShrineRenderer::new);
        }
 
        @SubscribeEvent
        public static void onRegisterClientReloadListeners(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new com.my.kaisen.registry.VfxRegistry());
        }
 
        @SubscribeEvent
        public static void onRegisterClientCommands(net.neoforged.neoforge.client.event.RegisterClientCommandsEvent event) {
            event.getDispatcher().register(net.minecraft.commands.Commands.literal("vfxloop")
                    .then(net.minecraft.commands.Commands.argument("effect", com.mojang.brigadier.arguments.StringArgumentType.string())
                            .executes(context -> {
                                String effect = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "effect");
                                if (effect.equals("stop")) {
                                    VfxDebugCommand.stopLoop();
                                } else {
                                    VfxDebugCommand.setLoop(effect);
                                }
                                return 1;
                            }))
                    .then(net.minecraft.commands.Commands.literal("stop")
                            .executes(context -> {
                                VfxDebugCommand.stopLoop();
                                return 1;
                            }))
            );
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        if (event.getPlayer().hasEffect(ModEffects.STUN) || event.getPlayer().hasEffect(ModEffects.LOCK_IN)) {
            event.setNewFovModifier(1.0f);
        }
    }

    private static int domainChargeTicks = 0;

    @SubscribeEvent
    public static void onClientPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            net.minecraft.world.entity.player.Player player = event.getEntity();
            
            // Domain Charge Logic
            if (KeyBindings.DOMAIN_KEY.isDown()) {
                domainChargeTicks++;
                if (domainChargeTicks == 20) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerDomainPayload(player.isShiftKeyDown()));
                    player.level().playSound(player, player.blockPosition(), com.my.kaisen.registry.ModSounds.universal_awekekning_sound.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
                }
            } else {
                domainChargeTicks = 0;
            }

            if (player.getPersistentData().getBoolean("is_awakened")) {
                ClientVfxHandler.spawnMenacingAura(
                        player.level(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getBbWidth() * 0.8,
                        player.getBbHeight()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (player.getPersistentData().getInt("mykaisen_character") == 1) {
            boolean battleMode = !player.getPersistentData().contains("mykaisen_battle_mode") || player.getPersistentData().getBoolean("mykaisen_battle_mode");
            if (battleMode) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerM1Payload());
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (player.getPersistentData().getInt("mykaisen_character") == 1) {
            boolean battleMode = !player.getPersistentData().contains("mykaisen_battle_mode") || player.getPersistentData().getBoolean("mykaisen_battle_mode");
            if (battleMode) {
                event.setCanceled(true);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerM1Payload());
            }
        }
    }
}
