package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CombatTickHandler {

    // Thread-safe maps for tracking dash and beatdown states
    public static final Map<UUID, Integer> activeDashes = new ConcurrentHashMap<>();
    public static final Map<UUID, BeatdownState> activeBeatdowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> dropkickStates = new ConcurrentHashMap<>();

    // Record to hold the state of an ongoing beatdown
    public record BeatdownState(LivingEntity targetEntity, int ticksRemaining) {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Ensure we are only running on the server side
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();

        // -----------------------------------------------------
        // 1. Dash Collision Logic
        // -----------------------------------------------------
        if (activeDashes.containsKey(playerId)) {
            int dashTimer = activeDashes.get(playerId);
            
            if (dashTimer > 0) {
                // Decrement timer
                activeDashes.put(playerId, dashTimer - 1);

                // Create an AABB roughly 1.5 blocks in front of the player
                Vec3 look = player.getLookAngle();
                Vec3 frontCenter = player.position().add(look.scale(1.5));
                
                // 2x2x2 box centered 1.5 blocks in front
                AABB hitBox = new AABB(
                        frontCenter.x - 1.0, frontCenter.y - 1.0, frontCenter.z - 1.0,
                        frontCenter.x + 1.0, frontCenter.y + 1.0, frontCenter.z + 1.0
                );

                // Detect living entities inside the box (excluding the player themselves)
                List<LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        hitBox,
                        e -> e != player && e.isAlive()
                );

                if (!hitEntities.isEmpty()) {
                    LivingEntity target = hitEntities.get(0);
                    
                    // Calculate the vector between player and target
                    Vec3 diff = target.position().subtract(player.position());
                    
                    // Normalize vector to find ideal position exactly 1.5 blocks away from the target
                    Vec3 normDiff = diff.normalize();
                    Vec3 idealPos = target.position().subtract(normDiff.scale(1.5));
                    
                    // Calculate precise Yaw and Pitch to look directly at target's eye level
                    double yaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0;
                    double dy = (target.position().y + target.getEyeHeight()) - (player.position().y + player.getEyeHeight());
                    double horizontalDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
                    double pitch = -Math.toDegrees(Math.atan2(dy, horizontalDist));
                    
                    // Forcefully teleport the player to the ideal position and snap their camera
                    player.teleportTo((net.minecraft.server.level.ServerLevel) player.level(), idealPos.x, idealPos.y, idealPos.z, (float) yaw, (float) pitch);
                    
                    // Stop momentum
                    player.setDeltaMovement(Vec3.ZERO);
                    player.hurtMarked = true;
                    
                    // Remove from dashes map
                    activeDashes.remove(playerId);
                    
                    // Add to beatdowns map with 60 ticks duration
                    activeBeatdowns.put(playerId, new BeatdownState(target, 60));
                    
                    // Send animation payload to clients tracking the attacker (and the attacker themselves)
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                            player, 
                            new PlayAnimationPayload("cursed_strikes", player.getId())
                    );
                    
                    // Play Beatdown SFX
                    player.level().playSound(null, target.blockPosition(), com.my.kaisen.registry.ModSounds.CURSED_STRIKES.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            } else {
                // Timer ran out, remove the dash
                activeDashes.remove(playerId);
            }
        }

        // -----------------------------------------------------
        // 2. Beatdown Logic
        // -----------------------------------------------------
        if (activeBeatdowns.containsKey(playerId)) {
            BeatdownState state = activeBeatdowns.get(playerId);
            int ticks = state.ticksRemaining();
            LivingEntity target = state.targetEntity();

            // Safety check: if target is dead or removed, stop the sequence
            if (target == null || !target.isAlive() || target.isRemoved()) {
                activeBeatdowns.remove(playerId);
                return;
            }

            // Apply Slowness (amplifier 255) for 2 ticks to keep both locked in place
            // We use 2 ticks so it clears instantly if the sequence stops
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 255, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 255, false, false, false));



            // Apply exactly 12 hits over 60 ticks => hit every 5 ticks
            if (ticks % 5 == 0) {
                target.hurt(target.damageSources().generic(), 7.0f / 12.0f);
            }

            // Final hit knockback
            if (ticks == 1) {
                // Push target backward relative to the player's look vector
                Vec3 lookVec = player.getLookAngle();
                // 1.5 scale + slight upward Y vector gives a good ~5 block knockback arc
                Vec3 push = new Vec3(lookVec.x * 1.5, 0.4, lookVec.z * 1.5);
                target.setDeltaMovement(push);
                target.hurtMarked = true;
            }

            // Update or remove sequence
            if (ticks <= 1) {
                activeBeatdowns.remove(playerId);
            } else {
                activeBeatdowns.put(playerId, new BeatdownState(target, ticks - 1));
            }
        }

        // -----------------------------------------------------
        // 3. Dropkick Logic
        // -----------------------------------------------------
        if (dropkickStates.containsKey(playerId)) {
            int ticks = dropkickStates.get(playerId) + 1;
            
            // Failsafe
            if (ticks > 100) {
                dropkickStates.remove(playerId);
                return;
            }
            
            dropkickStates.put(playerId, ticks);
            
            // Apex of the jump
            if (ticks == 10) {
                Vec3 look = player.getLookAngle();
                Vec3 diveMotion = new Vec3(look.x * 1.5, -1.5, look.z * 1.5);
                player.setDeltaMovement(diveMotion);
                player.hurtMarked = true;
                
                player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.DROPKICK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            
            // Impact
            if (ticks > 10 && player.onGround()) {
                // Play impact sound
                player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.BLOW_AOE.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // Spawn explosion particles (visual only)
                ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                        player.getX(), player.getY(), player.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0
                );
                
                // Deal damage in AABB
                AABB aoeBox = new AABB(
                        player.getX() - 2.0, player.getY() - 1.5, player.getZ() - 2.0,
                        player.getX() + 2.0, player.getY() + 1.5, player.getZ() + 2.0
                );
                
                List<LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        aoeBox,
                        e -> e != player && e.isAlive()
                );
                
                for (LivingEntity target : hitEntities) {
                    target.hurt(target.damageSources().generic(), 5.0f);
                }
                
                dropkickStates.remove(playerId);
            }
        }
    }
}
