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
                
                if (abilityId == 1) {
                    // Ability 1: Cursed Strikes (Forward Dash)
                    executeCursedStrikesDash(player);
                } else if (abilityId == 2) {
                    // Ability 2: Crushing Blow
                    executeCrushingBlow(player);
                } else if (abilityId == 3) {
                    // Ability 3: Divergent Fist / Black Flash
                    executeDivergentFist(player);
                }
            }
        });
    }

    private static void executeCursedStrikesDash(ServerPlayer player) {
        // 1. Apply Cooldown (Placeholder)
        // TODO: Apply a 10-second (200 tick) cooldown to the player using standard capabilities or item cooldowns

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
                // TODO: send the "black_flash" animation payload
            }
            return;
        }
        
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
            
            player.level().playSound(null, player.blockPosition(),
                    com.my.kaisen.registry.ModSounds.CHARGING_DIVERGENT_FIST.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            // TODO: send the "divergent_fist" animation payload
        }
    }
}
