package com.my.kaisen.client;
 
import com.my.kaisen.MyKaisen;
import com.my.kaisen.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
 
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
            
            // Domain Charge Logic (5 Seconds = 100 Ticks)
            // Domain Charge Logic (5 Seconds = 100 Ticks)
            if (KeyBindings.ABILITY_4_KEY.isDown()) {
                domainChargeTicks++;
                if (domainChargeTicks == 100) {
                    PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerDomainPayload(player.isShiftKeyDown()));
                    player.level().playSound(player, player.blockPosition(), com.my.kaisen.registry.ModSounds.universal_awekekning_sound.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
                    com.my.kaisen.client.ClientAnimationHandler.playAnimation((AbstractClientPlayer)player, "shrine_opening_domain");
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
 
    public static int getDomainChargeTicks() {
        return domainChargeTicks;
    }
 
    @SubscribeEvent
    public static void onMouseInput(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre event) {
        if (event.getButton() == 0 && event.getAction() == 1) { // Left Click Press (Action 1 = GLFW_PRESS)
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && mc.player != null) {
                // If character data is synced, this check will pass.
                int charId = mc.player.getPersistentData().getInt("mykaisen_character");
                if (charId == 1) {
                    boolean battleMode = !mc.player.getPersistentData().contains("mykaisen_battle_mode") || mc.player.getPersistentData().getBoolean("mykaisen_battle_mode");
                    if (battleMode) {
                        PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerM1Payload());
                        // Optional: Swing arm visually
                        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }
 
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            java.util.List<com.my.kaisen.entity.ShrineEntity> shrines = mc.level.getEntitiesOfClass(com.my.kaisen.entity.ShrineEntity.class, mc.player.getBoundingBox().inflate(250.0));
            if (!shrines.isEmpty()) {
                // Dark Red Sky Effect
                event.setRed(0.4f);
                event.setGreen(0.0f);
                event.setBlue(0.0f);
            }
        }
    }
}
