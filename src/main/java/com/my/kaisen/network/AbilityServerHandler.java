package com.my.kaisen.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
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
                    if (CombatTickHandler.isOnCooldown(player.getUUID(), abilityId)) return;

                        // Animation — grounded vs airborne variant
                        boolean onGround = player.onGround();
                        String dismantleAnim = onGround ? "dismantle" : "dismantle_air";
                        
                        if (!onGround) {
                            player.setNoGravity(true);
                            player.setDeltaMovement(0, 0.4D, 0);
                            player.hurtMarked = true;
                            CombatTickHandler.airDismantlePlayers.put(player.getUUID(), 20);
                        }

                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                player, new PlayAnimationPayload(dismantleAnim, player.getId())
                        );
                        
                        // Start 5-shot barrage (Z Key)
                        M1ComboHandler.comboShots.put(player.getUUID(), 5);
                        M1ComboHandler.comboTicks.put(player.getUUID(), 0);

                            CombatTickHandler.setCooldown(player.getUUID(), abilityId, 60); // 3 seconds
                    }
                } else if (abilityId == 2) {
                    if (!isAwakened) {
                        // Ability 2: Crushing Blow
                        executeCrushingBlow(player);
                    } else {
                        // Ability 2 (Awakened): Open (Fuga)
                        if (CombatTickHandler.isOnCooldown(player.getUUID(), abilityId)) return;

                        // Play Fuga Animation
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                                player, new PlayAnimationPayload("fuga", player.getId())
                        );

                        // Start Charge-Up (65 ticks = 3.25 seconds)
                        CombatTickHandler.fugaChargeTicks.put(player.getUUID(), 65);

                            CombatTickHandler.setCooldown(player.getUUID(), abilityId, 200); // 10 seconds cooldown
                    }
                } else if (abilityId == 3) {
                    if (!isAwakened) {
                        // Ability 3: Divergent Fist / Black Flash
                        executeDivergentFist(player);
                    } else {
                        // Ability 3 (Awakened): Rush (Kick-up Slam-down)
                        // Triggered by R during Phase 3 for Cleave transition
                        if (CombatTickHandler.isOnCooldown(player.getUUID(), abilityId)) return;
                        if (CombatTickHandler.activeRushes.containsKey(player.getUUID())) return;

                        CombatTickHandler.activeRushes.put(player.getUUID(), new CombatTickHandler.RushState());

                            CombatTickHandler.setCooldown(player.getUUID(), abilityId, 160); // 8 seconds

                        player.level().playSound(null, player.blockPosition(),
                                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
                    }
                } else if (abilityId == 4) {
                    if (!isAwakened) {
                        // Ability 4: Manji Kick (Universal Counter)
                        CombatTickHandler.executeManjiKick(player);
                    } else {
                        // Ability 4 (Awakened): Handled via 5s hold (TriggerDomainPayload)
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eHold Ability 4 (V) to manifest Malevolent Shrine..."));
                    }
                }
            }
        });
    }

    public static void handleDomain(final com.my.kaisen.network.TriggerDomainPayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                // Toggle Logic: Check if a shrine already exists for this owner
                java.util.List<com.my.kaisen.entity.ShrineEntity> existingShrines = player.level().getEntitiesOfClass(com.my.kaisen.entity.ShrineEntity.class, player.getBoundingBox().inflate(300.0),
                        (e) -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID()));

                if (CombatTickHandler.isOnCooldown(player.getUUID(), 6)) return;

                if (!existingShrines.isEmpty()) {
                    for (com.my.kaisen.entity.ShrineEntity existing : existingShrines) {
                        if (existing.getCurrentState() != com.my.kaisen.entity.ShrineEntity.DomainState.COLLAPSING) {
                            existing.setState(com.my.kaisen.entity.ShrineEntity.DomainState.COLLAPSING);
                            CombatTickHandler.setCooldown(player.getUUID(), 6, 200); // 10 seconds
                        }
                    }
                } else {
                    // Spawn new Shrine
                    com.my.kaisen.entity.ShrineEntity shrine = com.my.kaisen.util.DomainHandler.spawnShrine(player.level(), player.blockPosition(), player, payload.isOpenBarrier());
                    
                    CombatTickHandler.setCooldown(player.getUUID(), 6, 400); // 20 seconds for activation
                    
                    // Play Animation
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new com.my.kaisen.network.PlayAnimationPayload("shrine_opening_domain", player.getId()));

                    // Cinematic Effects
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.my.kaisen.network.CameraShakePayload(3.0f, 40));
                    player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(30.0)).forEach(e -> {
                        if (e != player) {
                            e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0, false, false));
                        }
                    });
                }
            }
        });
    }

    public static void handleAwaken(final AwakenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                float meter = CombatTickHandler.awakeningMeter.getOrDefault(player.getUUID(), 0.0f);
                if (meter >= 100.0f) {
                    // Handled via sequence start in CombatTickHandler
                    CombatTickHandler.awakeningSequences.put(player.getUUID(), 40); 
                    
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new com.my.kaisen.network.PlayAnimationPayload("sukuna_awakening", player.getId()));

                    // Auto-equip Curio tattoo
                    top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
                        inventory.findFirstCurio(com.my.kaisen.registry.ModItems.SUKUNA_TATTOO.get()).ifPresentOrElse(
                                slotResult -> {}, // Already equipped
                                () -> {
                                    // Find a slot and equip
                                    inventory.getCurios().forEach((id, handler) -> {
                                        if (id.equals("body") || id.equals("head")) {
                                            for (int i = 0; i < handler.getSlots(); i++) {
                                                if (handler.getStacks().getStackInSlot(i).isEmpty()) {
                                                    handler.getStacks().setStackInSlot(i, new net.minecraft.world.item.ItemStack(com.my.kaisen.registry.ModItems.SUKUNA_TATTOO.get()));
                                                    return;
                                                }
                                            }
                                        }
                                    });
                                }
                        );
                    });

                    // Heal the player
                    player.heal(45.0f);
                    
                    // Buffs for 60 seconds
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 1200, 2));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 1200, 2));
                    
                    // Cinematic Sequence
                    // 1. Send animation payload
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new PlayAnimationPayload("king_of_curses_awakening", player.getId()));

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
        if (CombatTickHandler.isOnCooldown(playerId, 4)) return;
        CombatTickHandler.setCooldown(playerId, 4, 160); // 8 seconds

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
        if (CombatTickHandler.isOnCooldown(playerId, 2)) return;

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
                
                CombatTickHandler.setCooldown(playerId, 2, 200); // 10 seconds

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
                
                    CombatTickHandler.setCooldown(playerId, 2, 200); // 10 seconds
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
        if (CombatTickHandler.isOnCooldown(playerId, 2)) return;

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
                CombatTickHandler.setCooldown(playerId, 2, 100); // 5 seconds
            }

            player.level().playSound(null, player.blockPosition(),
                    com.my.kaisen.registry.ModSounds.CHARGING_DIVERGENT_FIST.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, new PlayAnimationPayload("divergent_fist", player.getId())
            );
        }
    }

    public static void handleFuga(final TriggerFugaPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Play Fuga Animation
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new PlayAnimationPayload("fuga", player.getId())
                );

                // Start Charge-Up (65 ticks = 3.25 seconds)
                CombatTickHandler.fugaChargeTicks.put(player.getUUID(), 65);
            }
        });
    }

    public static void handleM1(final TriggerM1Payload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getPersistentData().getInt("mykaisen_character") != 1) return;
                if (!player.getPersistentData().getBoolean("is_awakened")) return;

                // Spawn EXACTLY ONE DismantleProjectileEntity
                com.my.kaisen.entity.DismantleProjectileEntity dismantle = new com.my.kaisen.entity.DismantleProjectileEntity(com.my.kaisen.registry.ModEntities.DISMANTLE_PROJECTILE.get(), player, player.level());
                dismantle.setPos(player.getEyePosition());
                dismantle.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
                player.level().addFreshEntity(dismantle);
                
                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        });
    }
    public static void handleCleaveNormal(final TriggerCleaveNormalPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (CombatTickHandler.activeRushes.containsKey(player.getUUID())) {
                    CombatTickHandler.RushState state = CombatTickHandler.activeRushes.get(player.getUUID());
                    if (state.ticks >= 21) { // Slam phase
                        state.isCleaveMode = true;
                        player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SUKUNA_MOCKING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        });
    }

    public static void handleCleaveWeb(final TriggerCleaveWebPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getPersistentData().getInt("mykaisen_character") != 1) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cOnly Sukuna can use Cleave Web."));
                    return;
                }
                if (!player.getPersistentData().getBoolean("is_awakened")) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou must be Awakened to use Cleave Web."));
                    return;
                }
                
                // Cleave Web logic
                net.minecraft.world.level.Level level = player.level();
                Vec3 center = player.position();
                int radius = 7;

                // Slam down if in air to touch the ground (Manga accurate Spiderweb start)
                if (!player.onGround() && player.getDeltaMovement().y > -2.0) {
                    player.setDeltaMovement(0, -3.0, 0);
                    player.hurtMarked = true;
                }

                // VFX and Sound
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SpawnCleaveWebVfxPayload(center.x, center.y, center.z));
                level.playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.CLEAVE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.8F, 0.9F);
                level.playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.CLEAVE_SLASH.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F);
                level.playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.SUKUNA_MOCKING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.2F, 1.0F);

                // Entity damage (High damage AoE)
                net.minecraft.world.phys.AABB area = player.getBoundingBox().inflate(radius);
                java.util.List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurt(target.damageSources().playerAttack(player), 40.0F);
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new SpawnCleaveVfxPayload(target.getX(), target.getY(0.5), target.getZ(), (float)level.random.nextInt(360)));
                }

                // Block destruction (Fractured Spiderweb Crater)
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            double distSq = x*x + z*z;
                            if (distSq <= radius * radius) {
                                // Destroy ground blocks with "fracture" patterns
                                for (int y = -1; y <= 1; y++) {
                                    net.minecraft.core.BlockPos pos = player.blockPosition().offset(x, y, z);
                                    float destroySpeed = serverLevel.getBlockState(pos).getDestroySpeed(serverLevel, pos);
                                    if (destroySpeed >= 0 && destroySpeed < 50.0f) {
                                        // Pattern: Keep some blocks to look like lines of a web
                                        if (level.random.nextDouble() > 0.3) {
                                            serverLevel.destroyBlock(pos, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Cooldown
                    CombatTickHandler.setCooldown(player.getUUID(), 5, 360); // ID 5 for Cleave Web
            }
        });
    }
}
