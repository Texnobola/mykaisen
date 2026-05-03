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

    public static boolean cooldownsEnabled = true;
    public static final Map<UUID, Integer> abilityCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Float> awakeningMeter = new ConcurrentHashMap<>();

    public static void addAwakening(ServerPlayer player, float amount) {
        UUID uuid = player.getUUID();
        float current = awakeningMeter.getOrDefault(uuid, 0.0f);
        float next = Math.min(100.0f, current + amount);
        awakeningMeter.put(uuid, next);
        
        if (next >= 100.0f && current < 100.0f) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Your Awakening meter is FULL! Press G to Awaken."));
        }
    }

    // Thread-safe maps for tracking dash and beatdown states
    public static final Map<UUID, Integer> activeDashes = new ConcurrentHashMap<>();
    public static final Map<UUID, BeatdownState> activeBeatdowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> dropkickStates = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> activeManjiKicks = new ConcurrentHashMap<>();
    
    public static final Map<UUID, CrushingBlowState> activeCrushingBlows = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> crushingBlowMisses = new ConcurrentHashMap<>();
    public static final Map<UUID, AirCrushingState> airCrushingBlows = new ConcurrentHashMap<>();
    public static final Map<UUID, DivergentFistState> activeDivergentFists = new ConcurrentHashMap<>();
    public static final Map<UUID, DismantleBurstState> activeDismantles = new ConcurrentHashMap<>();

    public record BeatdownState(LivingEntity targetEntity, int ticksRemaining) {}
    public record CrushingBlowState(LivingEntity targetEntity, int ticksElapsed) {}
    public record AirCrushingState(LivingEntity target, int ticks) {}
    
    public static class DismantleBurstState {
        public int ticks = 0;
        public int shotsFired = 0;
    }

    public static class DivergentFistState {
        public LivingEntity target;
        public int ticks = 0;
        public int chainCount = 0;
        public boolean isBlackFlash = false;
        
        public DivergentFistState(LivingEntity target) {
            this.target = target;
        }
    }

    public static final Map<UUID, CleaveRushState> activeCleaveRushes = new ConcurrentHashMap<>();

    public static class CleaveRushState {
        public int ticks = 0;
        public LivingEntity target = null;
        public boolean hasGrabbed = false;
    }

    public static final Map<UUID, RushState> activeRushes = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> awakeningSequences = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> suspendedPlayers = new ConcurrentHashMap<>();

    public static class RushState {
        public int ticks = 0;
        public LivingEntity target = null;
    }
    
    private static void createCrater(net.minecraft.server.level.ServerLevel level, Vec3 pos, int minBlocks, int maxBlocks) {
        net.minecraft.core.BlockPos center = net.minecraft.core.BlockPos.containing(pos.x, pos.y - 0.1, pos.z);
        int numBlocks = minBlocks + level.random.nextInt(maxBlocks - minBlocks + 1);
        int radius = 2;
        
        java.util.List<net.minecraft.core.BlockPos> validBlocks = new java.util.ArrayList<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    net.minecraft.core.BlockPos targetPos = center.offset(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(targetPos);
                    if (!state.isAir() && state.getDestroySpeed(level, targetPos) >= 0 && !state.liquid()) {
                        validBlocks.add(targetPos);
                    }
                }
            }
        }
        
        java.util.Collections.shuffle(validBlocks);
        int destroyedCount = 0;
        
        for (net.minecraft.core.BlockPos blockPos : validBlocks) {
            if (destroyedCount >= numBlocks) break;
            
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(blockPos);
            
            // Destroy the block (true drops items/particles, we'll use false to not drop items)
            level.destroyBlock(blockPos, false);
            
            // Spawn FallingBlockEntity for visual debris
            net.minecraft.world.entity.item.FallingBlockEntity fallingBlock = net.minecraft.world.entity.item.FallingBlockEntity.fall(level, blockPos, state);
            fallingBlock.setPos(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            
            // Upward/outward velocity
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = 0.5 + level.random.nextDouble() * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            fallingBlock.setDeltaMovement(new Vec3(offsetX, offsetY, offsetZ));
            
            level.addFreshEntity(fallingBlock);
            destroyedCount++;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Ensure we are only running on the server side
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();

        if (awakeningSequences.containsKey(playerId)) {
            int ticks = awakeningSequences.get(playerId);
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            if (ticks <= 1) {
                awakeningSequences.remove(playerId);
            } else {
                awakeningSequences.put(playerId, ticks - 1);
            }
        }

        if (suspendedPlayers.containsKey(playerId)) {
            int ticks = suspendedPlayers.get(playerId);
            player.setDeltaMovement(0, 0, 0);
            player.hasImpulse = true;
            if (ticks <= 1) {
                suspendedPlayers.remove(playerId);
            } else {
                suspendedPlayers.put(playerId, ticks - 1);
            }
        }

        if (activeDismantles.containsKey(playerId)) {
            DismantleBurstState state = activeDismantles.get(playerId);
            if (state.ticks % 4 == 0) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 endPos = eyePos.add(look.scale(15.0));
                AABB searchBox = player.getBoundingBox().expandTowards(look.scale(15.0)).inflate(1.0);

                net.minecraft.world.phys.EntityHitResult hitResult = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                        player.level(), player, eyePos, endPos, searchBox, e -> e instanceof LivingEntity && e.isAlive() && e != player
                );

                if (hitResult != null && hitResult.getEntity() instanceof LivingEntity target) {
                    target.hurt(player.damageSources().playerAttack(player), 4.0F);
                    target.setDeltaMovement(target.getDeltaMovement().add(look.scale(0.5).add(0, 0.2, 0)));
                    target.hurtMarked = true;
                }

                // Spawn VFX 1.5 blocks in front of the player
                Vec3 spawnPos = eyePos.add(look.scale(1.5D));
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                        new SpawnDismantleVfxPayload(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot()));
                
                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
                state.shotsFired++;
            }
            state.ticks++;
            if (state.shotsFired >= 5) {
                activeDismantles.remove(playerId);
            }
        }

        // -----------------------------------------------------
        // 0. Cooldown Logic & Timers
        // -----------------------------------------------------
        if (cooldownsEnabled && abilityCooldowns.containsKey(playerId)) {
            int currentCooldown = abilityCooldowns.get(playerId);
            if (currentCooldown > 0) {
                abilityCooldowns.put(playerId, currentCooldown - 1);
            } else {
                abilityCooldowns.remove(playerId);
            }
        }

        if (activeManjiKicks.containsKey(playerId)) {
            int currentTicks = activeManjiKicks.get(playerId);
            if (currentTicks > 0) {
                activeManjiKicks.put(playerId, currentTicks - 1);
            } else {
                activeManjiKicks.remove(playerId);
            }
        }

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

            // Apply custom effects for 2 ticks to keep both locked in place
            // We use 2 ticks so it clears instantly if the sequence stops
            player.addEffect(new MobEffectInstance(com.my.kaisen.registry.ModEffects.LOCK_IN, 2, 0, false, false, false));
            target.addEffect(new MobEffectInstance(com.my.kaisen.registry.ModEffects.STUN, 2, 0, false, false, false));



            // Apply exactly 12 hits over 60 ticks => hit every 5 ticks
            if (ticks % 5 == 0) {
                target.hurt(target.damageSources().generic(), 7.0f / 12.0f);
                addAwakening(player, 1.5f); // Reward each hit in the beatdown
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                        new SpawnCursedStrikesVfxPayload(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
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
                    addAwakening(player, 5.0f);
                }
                
                dropkickStates.remove(playerId);
            }
        }

        // -----------------------------------------------------
        // 4. Crushing Blow Logic
        // -----------------------------------------------------
        if (activeCrushingBlows.containsKey(playerId)) {
            CrushingBlowState state = activeCrushingBlows.get(playerId);
            int ticks = state.ticksElapsed() + 1;
            LivingEntity target = state.targetEntity();

            if (target == null || !target.isAlive() || target.isRemoved() || ticks > 35) {
                activeCrushingBlows.remove(playerId);
            } else {
                activeCrushingBlows.put(playerId, new CrushingBlowState(target, ticks));
                
                if (ticks == 8 || ticks == 18) {
                    player.level().playSound(null, target.blockPosition(), com.my.kaisen.registry.ModSounds.EACH_BLOW_IMPACT.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    createCrater((net.minecraft.server.level.ServerLevel) player.level(), target.position(), 4, 10);
                    target.hurt(target.damageSources().generic(), 3.0f);
                    addAwakening(player, 5.0f);
                } else if (ticks == 28) {
                    player.level().playSound(null, target.blockPosition(), com.my.kaisen.registry.ModSounds.GROUND_BREAKING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    createCrater((net.minecraft.server.level.ServerLevel) player.level(), target.position(), 10, 15);
                    target.hurt(target.damageSources().generic(), 4.0f);
                    addAwakening(player, 10.0f);
                    
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new SpawnCrushingBlowVfxPayload(target.getX(), target.getY(), target.getZ()));
                            
                    target.setDeltaMovement(new Vec3(0, 0.8, 0));
                    target.hurtMarked = true;
                }
            }
        }

        if (crushingBlowMisses.containsKey(playerId)) {
            int ticks = crushingBlowMisses.get(playerId) + 1;
            if (ticks > 10) {
                crushingBlowMisses.remove(playerId);
            } else {
                crushingBlowMisses.put(playerId, ticks);
                if (ticks == 8) {
                    player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.GROUND_BREAKING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    createCrater((net.minecraft.server.level.ServerLevel) player.level(), player.position(), 4, 10);
                    crushingBlowMisses.remove(playerId);
                }
            }
        }

        if (airCrushingBlows.containsKey(playerId)) {
            AirCrushingState state = airCrushingBlows.get(playerId);
            int ticks = state.ticks() + 1;
            LivingEntity target = state.target();
            
            if (ticks > 100) {
                airCrushingBlows.remove(playerId); // Failsafe
            } else {
                airCrushingBlows.put(playerId, new AirCrushingState(target, ticks));
                
                // At apex (tick 10), rocket towards the target
                if (ticks == 10 && target != null && target.isAlive()) {
                    Vec3 diff = target.position().subtract(player.position());
                    player.setDeltaMovement(diff.normalize().scale(3.0));
                    player.hurtMarked = true;
                }
                
                // Prevent fall damage while in this state
                player.fallDistance = 0;
                
                if (player.onGround() && ticks > 5) {
                    player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.GROUND_BREAKING.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    createCrater((net.minecraft.server.level.ServerLevel) player.level(), player.position(), 15, 25); // Massive crater
                    
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, 
                            new SpawnCrushingBlowVfxPayload(player.getX(), player.getY(), player.getZ()));
                            
                    
                    AABB aoeBox = new AABB(
                            player.getX() - 3.0, player.getY() - 2.0, player.getZ() - 3.0,
                            player.getX() + 3.0, player.getY() + 2.0, player.getZ() + 3.0
                    );
                    
                    List<LivingEntity> hitEntities = player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            aoeBox,
                            e -> e != player && e.isAlive()
                    );
                    
                    for (LivingEntity t : hitEntities) {
                        t.hurt(t.damageSources().generic(), 6.0f);
                        addAwakening(player, 10.0f);
                        // Launch them outwards and up
                        Vec3 diff = t.position().subtract(player.position());
                        if (diff.lengthSqr() == 0) diff = new Vec3(1, 0, 0); // fallback
                        Vec3 launch = diff.normalize().scale(1.5).add(0, 0.8, 0);
                        t.setDeltaMovement(launch);
                        t.hurtMarked = true;
                    }
                    
                    airCrushingBlows.remove(playerId);
                }
            }
        }
        
        // -----------------------------------------------------
        // 5. Divergent Fist & Black Flash
        // -----------------------------------------------------
        tickDivergentFists(player);

        // -----------------------------------------------------
        // 6. Cleave Rush
        // -----------------------------------------------------
        tickCleaveRush(player);

        // -----------------------------------------------------
        // 7. Rush (Awakened Ability 3)
        // -----------------------------------------------------
        tickRush(player);
    }
    
    private static void tickDivergentFists(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (activeDivergentFists.containsKey(playerId)) {
            DivergentFistState state = activeDivergentFists.get(playerId);
            LivingEntity target = state.target;
            
            if (target == null || !target.isAlive() || target.isRemoved() || state.ticks > 40) {
                activeDivergentFists.remove(playerId);
                return;
            }
            
            state.ticks++;
            
            if (!state.isBlackFlash) {
                // Standard Divergent Fist Logic
                if (state.ticks == 1) {
                    player.level().playSound(null, player.blockPosition(),
                            com.my.kaisen.registry.ModSounds.CHARGING_DIVERGENT_FIST.get(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                } else if (state.ticks == 15) {
                    player.level().playSound(null, target.blockPosition(),
                            com.my.kaisen.registry.ModSounds.BODY_HIT_DF.get(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    target.hurt(target.damageSources().generic(), 6.0f);
                } else if (state.ticks == 25) {
                    player.level().playSound(null, target.blockPosition(),
                            com.my.kaisen.registry.ModSounds.DIVERGENT_FIST_HIT.get(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    target.hurt(target.damageSources().magic(), 10.0f);
                    addAwakening(player, 10.0f);
                    
                    Vec3 lookVec = player.getLookAngle();
                    Vec3 push = new Vec3(lookVec.x * 1.8, 0.3, lookVec.z * 1.8);
                    target.setDeltaMovement(push);
                    target.hurtMarked = true;
                    
                    // Trigger Divergent Fist Aura Lodestone VFX at the target's center-of-mass on all nearby clients
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                            player,
                            new SpawnDivergentAuraPayload(target.getX(), target.getY(0.5), target.getZ())
                    );
                    activeDivergentFists.remove(playerId);
                }
            } else {
                // Black Flash Logic
                if (state.ticks == 20) {
                    executeBlackFlash(player, state);
                    activeDivergentFists.remove(playerId);
                }
            }
        }
    }
    
    private static void executeBlackFlash(ServerPlayer player, DivergentFistState state) {
        LivingEntity target = state.target;
        
        // Trigger Black Flash Lodestone VFX at the target's center-of-mass on all nearby clients
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                new SpawnBlackFlashPayload(target.getX(), target.getY(0.5), target.getZ())
        );
        // Heavy short camera shake for all nearby players observing the Black Flash
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player, new CameraShakePayload(0.4f, 15)
        );

        // Spawn Black Flash Sparks (Black and Red particles)
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
        net.minecraft.world.phys.Vec3 particlePos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        
        // Black Sparks
        serverLevel.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.0f, 0.0f, 0.0f), 1.5f), 
                particlePos.x, particlePos.y, particlePos.z, 40, 0.3, 0.3, 0.3, 0.2);
        // Red Sparks
        serverLevel.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.8f, 0.0f, 0.0f), 1.0f), 
                particlePos.x, particlePos.y, particlePos.z, 30, 0.4, 0.4, 0.4, 0.3);
        // High energy impact sparks
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, 
                particlePos.x, particlePos.y, particlePos.z, 20, 0.2, 0.2, 0.2, 0.5);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT, 
                particlePos.x, particlePos.y, particlePos.z, 15, 0.3, 0.3, 0.3, 0.4);
        
        boolean isBackHit = player.getLookAngle().normalize().dot(target.getLookAngle().normalize()) > 0.5;
        
        if (isBackHit && state.chainCount < 3) {
            player.level().playSound(null, target.blockPosition(),
                    com.my.kaisen.registry.ModSounds.BLACK_FLASH.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            target.hurt(target.damageSources().generic(), 12.0f);
            
            // Freeze the target in place for the next chain hit (20 ticks = 1 second)
            target.addEffect(new MobEffectInstance(com.my.kaisen.registry.ModEffects.STUN, 20, 0, false, false, false));
            
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
            
            state.ticks = 0;
            state.isBlackFlash = false;
            state.chainCount++;
            
            activeDivergentFists.put(player.getUUID(), state);
        } else {
            float damage = (state.chainCount == 3) ? 35.0f : 20.0f;
            player.level().playSound(null, target.blockPosition(),
                    com.my.kaisen.registry.ModSounds.BLACK_FLASH_FINAL.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            target.hurt(target.damageSources().generic(), damage);
            addAwakening(player, 25.0f); // Massive gain for Black Flash Final
            
            Vec3 lookVec = player.getLookAngle();
            Vec3 push = new Vec3(lookVec.x * 4.5, 0.6, lookVec.z * 4.5);
            target.setDeltaMovement(push);
            target.hurtMarked = true;
        }
    }
    public static void executeManjiKick(ServerPlayer player) {
        UUID playerId = player.getUUID();
        
        if (cooldownsEnabled && abilityCooldowns.containsKey(playerId)) return;
        
        activeManjiKicks.put(playerId, 20); // 1 second stance duration
        
        player.level().playSound(null, player.blockPosition(), com.my.kaisen.registry.ModSounds.manji_stance.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PlayAnimationPayload("manji_stance", player.getId()));
    }

    private static void tickCleaveRush(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!activeCleaveRushes.containsKey(playerId)) return;

        CleaveRushState state = activeCleaveRushes.get(playerId);

        // --------------------------------------------------------
        // Phase 1: Gap-closer dash – search for a grab target
        // --------------------------------------------------------
        if (!state.hasGrabbed) {
            state.ticks++;

            // Propel the player forward continuously
            player.setDeltaMovement(player.getLookAngle().scale(1.8D));
            player.hurtMarked = true;

            // AABB 2 blocks ahead of the player
            Vec3 look = player.getLookAngle();
            Vec3 front = player.position().add(look.scale(2.0));
            AABB grabBox = new AABB(
                    front.x - 0.8, front.y - 1.0, front.z - 0.8,
                    front.x + 0.8, front.y + 1.0, front.z + 0.8
            );

            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class, grabBox, e -> e != player && e.isAlive()
            );

            if (!nearby.isEmpty()) {
                state.target = nearby.get(0);
                state.hasGrabbed = true;
                state.ticks = 0;
                player.setDeltaMovement(Vec3.ZERO);
                player.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            } else if (state.ticks > 15) {
                // Missed – clean up
                activeCleaveRushes.remove(playerId);
            }
            return;
        }

        // --------------------------------------------------------
        // Phase 2 & 3: Grab is active
        // --------------------------------------------------------
        LivingEntity target = state.target;
        if (target == null || !target.isAlive() || target.isRemoved()) {
            activeCleaveRushes.remove(playerId);
            return;
        }

        state.ticks++;

        // Lock player in place
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;

        // Pin target 1.5 blocks in front of the player's face
        Vec3 pinPos = player.getEyePosition().add(player.getLookAngle().scale(1.5));
        target.teleportTo(pinPos.x, pinPos.y - target.getEyeHeight() * 0.5, pinPos.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;

        // Hits every 2 ticks from tick 0..20
        if (state.ticks <= 20 && state.ticks % 2 == 0) {
            target.hurt(target.damageSources().playerAttack(player), 3.0F);
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, new SpawnCleaveRushVfxPayload(target.getX(), target.getY(0.5), target.getZ(), false)
            );
        }

        // Final blow at tick 25
        if (state.ticks == 25) {
            target.hurt(target.damageSources().playerAttack(player), 20.0F);

            Vec3 lookVec = player.getLookAngle();
            Vec3 finalPush = new Vec3(lookVec.x * 3.0, 0.8, lookVec.z * 3.0);
            target.setDeltaMovement(finalPush);
            target.hurtMarked = true;

            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, new SpawnCleaveRushVfxPayload(target.getX(), target.getY(0.5), target.getZ(), true)
            );
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, new CameraShakePayload(0.5f, 20)
            );
            player.level().playSound(null, target.blockPosition(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.4F);

            activeCleaveRushes.remove(playerId);
        }
    }

    private static void tickRush(ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (!activeRushes.containsKey(playerId)) return;

        RushState state = activeRushes.get(playerId);
        state.ticks++;

        // Failsafe
        if (state.ticks > 60) {
            activeRushes.remove(playerId);
            return;
        }

        // ----------------------------------------------------------------
        // Phase 1 – Forward dash (ticks 1-10)
        // ----------------------------------------------------------------
        if (state.ticks <= 10) {
            player.setDeltaMovement(player.getLookAngle().scale(1.5D));
            player.hurtMarked = true;

            // Kick off the full Rush animation on the very first tick
            if (state.ticks == 1) {
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new PlayAnimationPayload("rush", player.getId())
                );
            }

            // AABB check 2 blocks ahead to find a target
            Vec3 look = player.getLookAngle();
            Vec3 front = player.position().add(look.scale(2.0));
            AABB grabBox = new AABB(
                    front.x - 0.8, front.y - 1.0, front.z - 0.8,
                    front.x + 0.8, front.y + 1.0, front.z + 0.8
            );

            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class, grabBox, e -> e != player && e.isAlive()
            );

            if (!nearby.isEmpty()) {
                state.target = nearby.get(0);
                state.ticks = 11; // Skip straight to Phase 2
                player.setDeltaMovement(Vec3.ZERO);
                player.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.9F);
            } else if (state.ticks == 10) {
                // Dash expired with no target – cancel
                activeRushes.remove(playerId);
            }
            return;
        }

        // All subsequent phases require a valid target
        LivingEntity target = state.target;
        if (target == null || !target.isAlive() || target.isRemoved()) {
            activeRushes.remove(playerId);
            return;
        }

        // ----------------------------------------------------------------
        // Phase 2 – Upward kick (ticks 11-20)
        // ----------------------------------------------------------------
        if (state.ticks >= 11 && state.ticks <= 20) {
            player.setDeltaMovement(0, 1.2, 0);
            target.setDeltaMovement(0, 1.2, 0);
            player.hurtMarked = true;
            target.hurtMarked = true;
            target.fallDistance = 0; // Prevent fall damage accumulation

            if (state.ticks == 11) {
                target.hurt(target.damageSources().playerAttack(player), 15.0F);
                player.level().playSound(null, target.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.7F);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new PlayAnimationPayload("rush_kick_up", player.getId())
                );
            }
            return;
        }

        // ----------------------------------------------------------------
        // Phase 3 – Downward slam (ticks 21-30)
        // ----------------------------------------------------------------
        if (state.ticks >= 21 && state.ticks <= 30) {
            player.setDeltaMovement(0, -2.5, 0);
            target.setDeltaMovement(0, -2.5, 0);
            player.hurtMarked = true;
            target.hurtMarked = true;

            if (state.ticks == 21) {
                player.level().playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new PlayAnimationPayload("rush_slam_down", player.getId())
                );
            }
            return;
        }

        // ----------------------------------------------------------------
        // Phase 4 – Impact (wait for target to land)
        // ----------------------------------------------------------------
        if (state.ticks > 30) {
            // Prevent indefinite hang: stop player movement while waiting
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;

            if (target.onGround()) {
                // Final impact damage
                target.hurt(target.damageSources().playerAttack(player), 5.0F);

                // Crater + VFX
                if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    createCrater(serverLevel, target.position(), 8, 14);

                    // Dust shockwave – server-side vanilla particles as fallback
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                            target.getX(), target.getY(), target.getZ(), 3, 0.5, 0.0, 0.5, 0.0
                    );
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            target.getX(), target.getY(), target.getZ(), 30, 1.5, 0.2, 1.5, 0.05
                    );
                }

                // Lodestone VFX payload for blood/dust effect
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new SpawnCleaveRushVfxPayload(target.getX(), target.getY(0.5), target.getZ(), true)
                );

                // Heavy screen shake
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                        player, new CameraShakePayload(0.7f, 25)
                );

                player.level().playSound(null, target.blockPosition(),
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.8F);

                activeRushes.remove(playerId);
            }
        }
    }
}

