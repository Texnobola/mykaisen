package com.my.kaisen.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AbilityServerHandler {

    public static void handle(final AbilityPayload payload, final IPayloadContext context) {
        // Enqueue work onto the main server thread safely
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                int abilityId = payload.abilityId();
                
                if (player.getPersistentData().getInt("mykaisen_character") != 1) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You must choose the Sorcerer path to use these abilities."));
                    return;
                }

                // Battle Mode check (Defaults to true if not set)
                boolean battleMode = !player.getPersistentData().contains("mykaisen_battle_mode") || player.getPersistentData().getBoolean("mykaisen_battle_mode");
                if (!battleMode) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Special attacks are disabled in Play Mode. Press H to switch to Battle Mode."));
                    return;
                }
                
                boolean isAwakened = player.getPersistentData().getBoolean("is_awakened");

                if (abilityId == 1) {
                    if (!isAwakened) {
                        // Ability 1: Cursed Strikes (Forward Dash)
                        executeCursedStrikesDash(player);
                    } else {
                        // Dismantle: Fast hitscan attack
                        if (CombatTickHandler.cooldownsEnabled && CombatTickHandler.abilityCooldowns.containsKey(player.getUUID())) return;

                        // Animation — grounded vs airborne variant
                        boolean onGround = player.onGround();
                        String dismantleAnim = onGround ? "dismantle" : "dismantle_air";
                        
                        if (!onGround) {
                            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.4D, 0));
                            player.hurtMarked = true;
                            CombatTickHandler.suspendedPlayers.put(player.getUUID(), 15);
                        }

                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                player, new PlayAnimationPayload(dismantleAnim, player.getId())
                        );

                        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
                        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                        net.minecraft.world.phys.Vec3 endPos = eyePos.add(look.scale(15.0));
                        net.minecraft.world.phys.AABB searchBox = player.getBoundingBox().expandTowards(look.scale(15.0)).inflate(1.0);

                        net.minecraft.world.phys.EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                                player.level(), player, eyePos, endPos, searchBox, e -> e instanceof net.minecraft.world.entity.LivingEntity && e.isAlive() && e != player
                        );

                        if (hitResult != null && hitResult.getEntity() instanceof net.minecraft.world.entity.LivingEntity target) {
                            target.hurt(player.damageSources().playerAttack(player), 6.0F);
                            
                            // Backward knockback
                            net.minecraft.world.phys.Vec3 push = look.scale(0.5).add(0, 0.2, 0);
                            target.setDeltaMovement(target.getDeltaMovement().add(push));
                            target.hurtMarked = true;

                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                    player, new SpawnDismantleVfxPayload(target.getX(), target.getY(0.5), target.getZ(), player.getYRot())
                            );
                        } else {
                            // Miss VFX at max range
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                    player, new SpawnDismantleVfxPayload(endPos.x, endPos.y, endPos.z, player.getYRot())
                            );
                        }

                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);

                        if (CombatTickHandler.cooldownsEnabled) {
                            CombatTickHandler.abilityCooldowns.put(player.getUUID(), 60); // 3 seconds
                        }
                    }
                } else if (abilityId == 2) {
                    if (!isAwakened) {
                        // Ability 2: Crushing Blow
                        executeCrushingBlow(player);
                    } else {
                        // TODO: Open (Fuga)
                    }
                } else if (abilityId == 3) {
                    if (!isAwakened) {
                        // Ability 3: Divergent Fist / Black Flash
                        executeDivergentFist(player);
                    } else {
                        // Rush: 3-stage combo (Dash → Kick Up → Slam Down)
                        if (CombatTickHandler.cooldownsEnabled && CombatTickHandler.abilityCooldowns.containsKey(player.getUUID())) return;
                        if (CombatTickHandler.activeRushes.containsKey(player.getUUID())) return;

                        CombatTickHandler.activeRushes.put(player.getUUID(), new CombatTickHandler.RushState());

                        if (CombatTickHandler.cooldownsEnabled) {
                            CombatTickHandler.abilityCooldowns.put(player.getUUID(), 300); // 15 seconds
                        }

                        player.level().playSound(null, player.blockPosition(),
                                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
                    }
                } else if (abilityId == 4) {
                    if (!isAwakened) {
                        // Ability 4: Manji Kick (Universal Counter)
                        CombatTickHandler.executeManjiKick(player);
                    } else {
                        // TODO: Malevolent Shrine
                    }
                }
            }
        });
    }

    public static void handleAwaken(final AwakenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                float meter = CombatTickHandler.awakeningMeter.getOrDefault(player.getUUID(), 0.0f);
                if (meter >= 100.0f) {
                    CombatTickHandler.awakeningMeter.put(player.getUUID(), 0.0f);
                    player.getPersistentData().putBoolean("is_awakened", true);
                    
                    // Sync to client
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new SyncAwakeningPayload(player.getId(), true));

                    // Heal the player
                    player.heal(45.0f);
                    
                    // Buffs for 60 seconds
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 1200, 2));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 1200, 2));
                    
                    // Cinematic Sequence
                    // 1. Send animation payload
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new PlayAnimationPayload("KingofCursesAwekeningAnimation", player.getId()));

                    // 2. Play global sound
                    player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.universal_awekekning_sound.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

                    // 3. Play voice line specifically to the player (or globally)
                    player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.sukuna_awekening.get(), net.minecraft.sounds.SoundSource.VOICE, 1.0f, 1.0f);

                    // 4. Halt movement for 25 ticks
                    CombatTickHandler.awakeningSequences.put(player.getUUID(), 25);
                    
                    // VFX Burst
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new SpawnAwakeningVfxPayload(player.getX(), player.getY() + 1.0, player.getZ()));
                    
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4You have AWAKENED! Your moveset has changed."));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYour Awakening meter is not full (" + (int)meter + "/100)."));
                }
            }
        });
    }

    private static void executeCursedStrikesDash(ServerPlayer player) {
        java.util.UUID playerId = player.getUUID();
        // Combo chaining must happen before this if applicable
        if (CombatTickHandler.cooldownsEnabled && CombatTickHandler.abilityCooldowns.containsKey(playerId)) return;

        if (CombatTickHandler.cooldownsEnabled) {
            CombatTickHandler.abilityCooldowns.put(playerId, 160); // 8 seconds
        }

        if (player.onGround()) {
            // Grounded logic: Propel player forward roughly 3 blocks
            Vec3 lookVec = player.getLookAngle();
            double dashMultiplier = 2.0;
            
            Vec3 dashMotion = new Vec3(
                    lookVec.x * dashMultiplier,
                    0.2, // Small hop
                    lookVec.z * dashMultiplier
            );

            player.setDeltaMovement(dashMotion);
            
            // Play Dash SFX
            player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.DASH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // CRITICAL: Tells the server to forcefully update the client with this new movement vector immediately
            player.hurtMarked = true; 

            // Register player in active dashes with a 10-tick timer
            CombatTickHandler.activeDashes.put(player.getUUID(), 10);
        } else {
            // Aerial logic: Launch upward slightly and start dropkick sequence
            player.setDeltaMovement(new Vec3(0, 0.8, 0));
            player.hurtMarked = true;
            
            // Register player in active dropkicks
            CombatTickHandler.dropkickStates.put(player.getUUID(), 0);
            
            // Play spin SFX
            player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SPIN_MID_AIR.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // Send dropkick animation
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload("cursed_dropkick", player.getId()));
        }
    }

    private static void executeCrushingBlow(ServerPlayer player) {
        java.util.UUID playerId = player.getUUID();
        if (CombatTickHandler.cooldownsEnabled && CombatTickHandler.abilityCooldowns.containsKey(playerId)) return;

        if (player.onGround()) {
            // Grounded logic
            Vec3 lookVec = player.getLookAngle();
            Vec3 frontCenter = player.position().add(lookVec.scale(1.5));
            net.minecraft.world.phys.AABB hitBox = new net.minecraft.world.phys.AABB(
                    frontCenter.x - 0.75, frontCenter.y - 0.75, frontCenter.z - 0.75,
                    frontCenter.x + 0.75, frontCenter.y + 0.75, frontCenter.z + 0.75
            );
            
            java.util.List<net.minecraft.world.entity.LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    hitBox,
                    e -> e != player && e.isAlive()
            );
            
            if (!hitEntities.isEmpty()) {
                net.minecraft.world.entity.LivingEntity target = hitEntities.get(0);
                
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.my.kaisen.registry.ModEffects.LOCK_IN, 40, 0, false, false, false));
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.my.kaisen.registry.ModEffects.STUN, 40, 0, false, false, false));
                
                CombatTickHandler.activeCrushingBlows.put(player.getUUID(), new CombatTickHandler.CrushingBlowState(target, 0));
                
                if (CombatTickHandler.cooldownsEnabled) {
                    CombatTickHandler.abilityCooldowns.put(playerId, 200); // 10 seconds
                }

                player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SWING_BACK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload("crushing_blow", player.getId()));
            } else {
                CombatTickHandler.crushingBlowMisses.put(player.getUUID(), 0);
                player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SWING_BACK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload("crushing_blow_miss", player.getId()));
            }
        } else {
            // Airborne logic (Missile Drop)
            player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SWING_BACK_AIR.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload("crushing_blow_air", player.getId()));
            
            // Launch straight UP instead of homing immediately
            player.setDeltaMovement(new Vec3(0, 1.5, 0));
            player.hurtMarked = true;
            
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 endPos = eyePos.add(look.scale(15.0));
            net.minecraft.world.phys.AABB searchBox = player.getBoundingBox().expandTowards(look.scale(15.0)).inflate(1.0);
            
            net.minecraft.world.phys.EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                    player.level(), player, eyePos, endPos, searchBox, e -> e instanceof net.minecraft.world.entity.LivingEntity && e.isAlive() && e != player
            );
            
            net.minecraft.world.entity.LivingEntity target = null;
            if (hitResult != null && hitResult.getEntity() instanceof net.minecraft.world.entity.LivingEntity hitTarget) {
                target = hitTarget;
                
                if (CombatTickHandler.cooldownsEnabled) {
                    CombatTickHandler.abilityCooldowns.put(playerId, 200); // 10 seconds
                }
            }
            
            CombatTickHandler.airCrushingBlows.put(player.getUUID(), new CombatTickHandler.AirCrushingState(target, 0));
        }
    }

    private static void executeDivergentFist(ServerPlayer player) {
        if (CombatTickHandler.activeDivergentFists.containsKey(player.getUUID())) {
            CombatTickHandler.DivergentFistState state = CombatTickHandler.activeDivergentFists.get(player.getUUID());
            
            if (state.ticks >= 6 && state.ticks <= 10 && !state.isBlackFlash) {
                state.isBlackFlash = true;
                player.level().playSound(null, player.blockPosition(),
                        com.my.kaisen.registry.ModSounds.CHARGING_DIVERGENT_FIST_2.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new PlayAnimationPayload("black_flash", player.getId())
                );
            }
            return;
        }
        
        java.util.UUID playerId = player.getUUID();
        if (CombatTickHandler.cooldownsEnabled && CombatTickHandler.abilityCooldowns.containsKey(playerId)) return;

        Vec3 lookVec = player.getLookAngle();
        Vec3 frontCenter = player.position().add(lookVec.scale(1.5));
        net.minecraft.world.phys.AABB hitBox = new net.minecraft.world.phys.AABB(
                frontCenter.x - 0.75, frontCenter.y - 0.75, frontCenter.z - 0.75,
                frontCenter.x + 0.75, frontCenter.y + 0.75, frontCenter.z + 0.75
        );
        
        java.util.List<net.minecraft.world.entity.LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                hitBox,
                e -> e != player && e.isAlive()
        );
        
        if (!hitEntities.isEmpty()) {
            net.minecraft.world.entity.LivingEntity target = hitEntities.get(0);
            CombatTickHandler.DivergentFistState newState = new CombatTickHandler.DivergentFistState(target);
            CombatTickHandler.activeDivergentFists.put(player.getUUID(), newState);
            
            if (CombatTickHandler.cooldownsEnabled) {
                CombatTickHandler.abilityCooldowns.put(playerId, 100); // 5 seconds
            }

            player.level().playSound(null, player.blockPosition(),
                    com.my.kaisen.registry.ModSounds.CHARGING_DIVERGENT_FIST.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, new PlayAnimationPayload("divergent_fist", player.getId())
            );
        }
    }
}
