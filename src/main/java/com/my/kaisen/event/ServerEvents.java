package com.my.kaisen.event;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.command.ToggleCooldownsCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import java.util.Random;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerJoin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean("mykaisen_first_join")) {
            player.getPersistentData().putBoolean("mykaisen_first_join", true);
            player.getInventory().add(new net.minecraft.world.item.ItemStack(com.my.kaisen.registry.ModItems.CHARACTER_CHOOSER.get()));
        }

        // Sync awakening status on join
        if (player instanceof ServerPlayer serverPlayer && player.getPersistentData().getBoolean("is_awakened")) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, 
                    new com.my.kaisen.network.SyncAwakeningPayload(player.getId(), true));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        net.minecraft.nbt.CompoundTag oldData = event.getOriginal().getPersistentData();
        net.minecraft.nbt.CompoundTag newData = event.getEntity().getPersistentData();

        if (oldData.contains("mykaisen_character")) {
            newData.putInt("mykaisen_character", oldData.getInt("mykaisen_character"));
        }
        if (oldData.contains("mykaisen_battle_mode")) {
            newData.putBoolean("mykaisen_battle_mode", oldData.getBoolean("mykaisen_battle_mode"));
        }
        if (oldData.contains("mykaisen_first_join")) {
            newData.putBoolean("mykaisen_first_join", oldData.getBoolean("mykaisen_first_join"));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getPersistentData().getInt("mykaisen_character") == 1) {
                // Vessel Passives
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 1, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 3, false, false, false)); // Strength 4
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false)); // Speed 2
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 0, false, false, false)); // Jump Boost 1
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20, 9, false, false, false)); // Health Boost 10
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0, false, false, false)); // Saturation infinite
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false, false)); // Durability 3 (Resistance 3)
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ToggleCooldownsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Prevent self-damage from abilities for Sorcerers
            if (player.getPersistentData().getInt("mykaisen_character") == 1) {
                if (event.getSource().getEntity() == player) {
                    event.setCanceled(true);
                    return;
                }
            }

            // Auto-dodge for Vessel (15% chance)
            if (player.getPersistentData().getInt("mykaisen_character") == 1) {
                if (new Random().nextFloat() < 0.15f) {
                    event.setCanceled(true);
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_NODAMAGE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
                    return;
                }
            }

            if (com.my.kaisen.network.CombatTickHandler.activeManjiKicks.containsKey(player.getUUID())) {
                event.setCanceled(true); // Negate all damage
                com.my.kaisen.network.CombatTickHandler.activeManjiKicks.remove(player.getUUID());

                net.minecraft.world.entity.Entity attacker = event.getSource().getEntity();
                if (attacker instanceof net.minecraft.world.entity.LivingEntity target) {
                    double distance = player.distanceTo(target);

                    if (distance > 3.0D) {
                        player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.manji_dash.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        net.minecraft.world.phys.Vec3 dir = target.position().subtract(player.position()).normalize().scale(4.5D).add(0, 0.2, 0);
                        player.setDeltaMovement(dir);
                        player.hurtMarked = true;
                    }

                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                            player, new com.my.kaisen.network.PlayAnimationPayload("manji_kick", player.getId())
                    );

                    target.hurt(player.damageSources().playerAttack(player), 8.5F);
                    
                    if (com.my.kaisen.network.CombatTickHandler.cooldownsEnabled) {
                        com.my.kaisen.network.CombatTickHandler.setCooldown(player.getUUID(), 4, 400); // 20-second cooldown
                    }
                    
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
