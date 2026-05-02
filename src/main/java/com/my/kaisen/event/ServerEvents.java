package com.my.kaisen.event;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.command.ToggleCooldownsCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ToggleCooldownsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (com.my.kaisen.network.CombatTickHandler.activeManjiKicks.containsKey(player.getUUID())) {
                event.setCanceled(true); // Negate all damage
                com.my.kaisen.network.CombatTickHandler.activeManjiKicks.remove(player.getUUID());

                net.minecraft.world.entity.Entity attacker = event.getSource().getEntity();
                if (attacker instanceof net.minecraft.world.entity.LivingEntity target) {
                    double distance = player.distanceTo(target);

                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                            player, new com.my.kaisen.network.PlayAnimationPayload("manji_kick", player.getId())
                    );

                    if (distance > 3.0D) {
                        player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.manji_dash.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        net.minecraft.world.phys.Vec3 dir = target.position().subtract(player.position()).normalize().scale(1.5D);
                        player.setDeltaMovement(dir);
                        player.hurtMarked = true;
                    }

                    target.hurt(player.damageSources().playerAttack(player), 8.5F);
                    
                    // Leftward horizontal knockback
                    net.minecraft.world.phys.Vec3 lookVec = player.getLookAngle();
                    net.minecraft.world.phys.Vec3 leftVec = lookVec.yRot((float) Math.PI / 2).normalize().scale(1.2D);
                    target.setDeltaMovement(target.getDeltaMovement().add(leftVec));
                    target.hurtMarked = true;

                    player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.manji_kick.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                            player, new com.my.kaisen.network.CameraShakePayload(0.5f, 15)
                    );
                }
            }
        }
    }
}
