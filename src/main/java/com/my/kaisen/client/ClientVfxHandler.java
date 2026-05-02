package com.my.kaisen.client;

import com.my.kaisen.network.SpawnBlackFlashPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.Color;
import java.util.Random;

/**
 * Client-side handler for Lodestone-based visual effects.
 */
public class ClientVfxHandler {

    private static final Random RANDOM = new Random();

    /**
     * Receiver for the SpawnBlackFlashPayload.
     */
    public static void handleBlackFlashVfx(SpawnBlackFlashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnBlackFlash(level, payload.x(), payload.y(), payload.z());
            }
        });
    }

    /**
     * Spawns an explosive burst of Lodestone particles (Black and Crimson) with lightning streaks.
     */
    public static void spawnBlackFlash(Level level, double x, double y, double z) {
        // Core Glow
        for (int i = 0; i < 15; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 0.3;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.3;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.3;

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(1.5f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(100, 0, 0), Color.BLACK).build())
                    .setLifetime(10 + RANDOM.nextInt(10))
                    .setRandomOffset(0.2)
                    .addMotion(vx, vy, vz)
                    .spawn(level, x, y, z);
        }

        // Lightning Streaks
        for (int i = 0; i < 20; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 6.0; // Increased velocity
            double vy = (RANDOM.nextDouble() - 0.5) * 6.0;
            double vz = (RANDOM.nextDouble() - 0.5) * 6.0;

            WorldParticleBuilder.create(LodestoneParticleTypes.SPARK_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 1.0f, 0.0f).build()) // Opaque then sharp fade
                    .setScaleData(GenericParticleData.create(3.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(255, 0, 0), Color.BLACK).build())
                    .setSpinData(SpinParticleData.create(RANDOM.nextFloat() * 6.28f, RANDOM.nextFloat() * 0.5f).build())
                    .setLifetime(10 + RANDOM.nextInt(5))
                    .setRandomOffset(0.1)
                    .addMotion(vx, vy, vz)
                    .spawn(level, x, y, z);
        }
    }

    /**
     * Receiver for the SpawnDivergentAuraPayload.
     */
    public static void handleDivergentAuraVfx(com.my.kaisen.network.SpawnDivergentAuraPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnDivergentAura(level, payload.x(), payload.y(), payload.z());
            }
        });
    }

    /**
     * Spawns an expanding burst of cursed energy (Bright Cyan to Deep Blue).
     */
    public static void spawnDivergentAura(Level level, double x, double y, double z) {
        for (int i = 0; i < 30; i++) {
            // Calculate random outward velocity
            double vx = (RANDOM.nextDouble() - 0.5) * 0.5;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.5;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.5;

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(2.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(0, 255, 255), new Color(0, 0, 139)).build())
                    .setLifetime(20 + RANDOM.nextInt(10))
                    .setRandomOffset(0.3)
                    .addMotion(vx, vy, vz)
                    .spawn(level, x, y, z);
        }
    }
    /**
     * Receiver for the SpawnCursedStrikesVfxPayload.
     */
    public static void handleCursedStrikesVfx(com.my.kaisen.network.SpawnCursedStrikesVfxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnCursedStrikesVfx(level, payload.x(), payload.y(), payload.z());
            }
        });
    }

    /**
     * Spawns a rapid, tight burst of white and light-blue particles.
     */
    public static void spawnCursedStrikesVfx(Level level, double x, double y, double z) {
        for (int i = 0; i < 8; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 0.4;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.4;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.4;

            WorldParticleBuilder.create(LodestoneParticleTypes.SPARK_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(Color.WHITE, new Color(173, 216, 230)).build())
                    .setLifetime(5 + RANDOM.nextInt(3))
                    .setRandomOffset(0.1)
                    .addMotion(vx, vy, vz)
                    .spawn(level, x, y, z);
        }
    }

    /**
     * Receiver for the SpawnCrushingBlowVfxPayload.
     */
    public static void handleCrushingBlowVfx(com.my.kaisen.network.SpawnCrushingBlowVfxPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnCrushingBlowVfx(level, payload.x(), payload.y(), payload.z());
            }
        });
    }

    /**
     * Spawns a large horizontal ring of smoke/dust and a vertical pillar of red cursed energy.
     */
    public static void spawnCrushingBlowVfx(Level level, double x, double y, double z) {
        // Horizontal Ring
        for (int i = 0; i < 40; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double speed = 0.5 + RANDOM.nextDouble() * 0.5;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;

            WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.6f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(2.5f, 4.0f).build())
                    .setColorData(ColorParticleData.create(new Color(100, 100, 100), new Color(50, 50, 50)).build())
                    .setLifetime(20 + RANDOM.nextInt(10))
                    .addMotion(vx, 0.0, vz)
                    .spawn(level, x, y, z);
        }

        // Vertical Pillar
        for (int i = 0; i < 30; i++) {
            double vy = 0.5 + RANDOM.nextDouble() * 1.5;

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(2.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(255, 0, 0), new Color(139, 0, 0)).build())
                    .setLifetime(15 + RANDOM.nextInt(15))
                    .setRandomOffset(0.5)
                    .addMotion(0.0, vy, 0.0)
                    .spawn(level, x, y, z);
        }
    }
    /**
     * Receiver for the CameraShakePayload using Lodestone's ScreenShake API.
     */
    public static void handleCameraShake(com.my.kaisen.network.CameraShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            team.lodestar.lodestone.handlers.ScreenshakeHandler.addScreenshake(
                    team.lodestar.lodestone.systems.screenshake.ScreenshakeBuilder.create()
                            .setDuration(payload.durationTicks())
                            .setStrength(payload.intensity())
                            .build()
            );
        });
    }
    /**
     * Receiver for the SpawnAwakeningVfxPayload.
     */
    public static void handleAwakeningVfx(final com.my.kaisen.network.SpawnAwakeningVfxPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnAwakeningVfx(level, payload.x(), payload.y(), payload.z());
            }
        });
    }

    /**
     * Spawns a massive shockwave of dark red and black cursed energy.
     */
    public static void spawnAwakeningVfx(Level level, double x, double y, double z) {
        // Massive shockwave of dark red and black
        for (int i = 0; i < 60; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 1.5;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.5;
            double vz = (RANDOM.nextDouble() - 0.5) * 1.5;
            
            Color color = i % 2 == 0 ? new Color(139, 0, 0) : Color.BLACK;
            
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(4.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(color, Color.BLACK).build())
                    .setLifetime(30 + RANDOM.nextInt(20))
                    .addMotion(vx, vy, vz)
                    .spawn(level, x, y, z);
        }
    }

    /**
     * Spawns a continuous, aggressive 'Menacing Red Aura' around a position.
     */
    public static void spawnMenacingAura(Level level, double x, double y, double z, double width, double height) {
        // Crimson Wisp
        if (RANDOM.nextFloat() < 0.4f) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double r = RANDOM.nextDouble() * width;
            double px = x + Math.cos(angle) * r;
            double pz = z + Math.sin(angle) * r;
            double py = y + RANDOM.nextDouble() * height;

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.6f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(1.2f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(220, 20, 60), Color.BLACK).build())
                    .setLifetime(15 + RANDOM.nextInt(10))
                    .addMotion(0, 0.08, 0)
                    .spawn(level, px, py, pz);
        }

        // Black Smoke
        if (RANDOM.nextFloat() < 0.3f) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double r = RANDOM.nextDouble() * width;
            double px = x + Math.cos(angle) * r;
            double pz = z + Math.sin(angle) * r;
            double py = y + RANDOM.nextDouble() * height;

            WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.4f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(0.8f, 1.5f).build())
                    .setColorData(ColorParticleData.create(Color.BLACK, new Color(50, 0, 0)).build())
                    .setLifetime(20 + RANDOM.nextInt(15))
                    .addMotion(0, 0.04, 0)
                    .spawn(level, px, py, pz);
        }
    }

    /**
     * Receiver for the SpawnDismantleVfxPayload.
     */
    public static void handleDismantleVfx(final com.my.kaisen.network.SpawnDismantleVfxPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnDismantle(level, payload.x(), payload.y(), payload.z(), payload.yRot());
            }
        });
    }

    /**
     * Spawns a wide, thin horizontal slash of white/light-blue cursed energy.
     */
    public static void spawnDismantle(Level level, double x, double y, double z, float yRot) {
        // yRot is the yaw. We want the line to be perpendicular to the look direction.
        // Convert to radians and add 90 degrees (pi/2) to get the perpendicular vector.
        double angle = Math.toRadians(yRot + 90.0f);
        double perpX = Math.cos(angle);
        double perpZ = Math.sin(angle);

        // Spawn a tight line of sparks extending left and right from the center point
        int sparkCount = 15;
        double slashWidth = 2.5; // Total width of the slash
        
        for (int i = 0; i < sparkCount; i++) {
            // Calculate offset along the perpendicular vector (-0.5 to 0.5)
            double offset = (i / (double)(sparkCount - 1)) - 0.5;
            
            double px = x + (perpX * offset * slashWidth);
            double pz = z + (perpZ * offset * slashWidth);

            // Asymmetric scaling: wide horizontally, thin vertically
            // We use a slight random rotation to make it look energetic but mostly flat
            WorldParticleBuilder.create(LodestoneParticleTypes.SPARK_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(0.8f, 0.1f).build()) // Asymmetric-like effect by scaling down quickly
                    .setColorData(ColorParticleData.create(Color.WHITE, new Color(173, 216, 230)).build())
                    .setLifetime(4 + RANDOM.nextInt(3)) // Very short lifetime
                    .spawn(level, px, y, pz);
        }
    }
    /**
     * Receiver for the SpawnCleaveRushVfxPayload.
     */
    public static void handleCleaveRushVfx(final com.my.kaisen.network.SpawnCleaveRushVfxPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                spawnCleaveRushVfx(level, payload.x(), payload.y(), payload.z(), payload.isFinalHit());
            }
        });
    }

    /**
     * Cleave Rush VFX:
     * - Regular hits: tight crimson/white slash sparks.
     * - Final hit: massive dark-red & black explosion of wisps.
     */
    public static void spawnCleaveRushVfx(Level level, double x, double y, double z, boolean isFinalHit) {
        if (!isFinalHit) {
            // Regular hit – fast crimson slash sparks
            for (int i = 0; i < 8; i++) {
                double vx = (RANDOM.nextDouble() - 0.5) * 0.6;
                double vy = (RANDOM.nextDouble()) * 0.4;
                double vz = (RANDOM.nextDouble() - 0.5) * 0.6;

                WorldParticleBuilder.create(LodestoneParticleTypes.SPARK_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.2f, 0.0f).build())
                        .setColorData(ColorParticleData.create(new Color(220, 20, 60), Color.WHITE).build())
                        .setLifetime(5 + RANDOM.nextInt(3))
                        .addMotion(vx, vy, vz)
                        .spawn(level, x, y, z);
            }
        } else {
            // Final hit – explosive shockwave of dark-red wisps
            for (int i = 0; i < 50; i++) {
                double vx = (RANDOM.nextDouble() - 0.5) * 2.0;
                double vy = (RANDOM.nextDouble()) * 1.5;
                double vz = (RANDOM.nextDouble() - 0.5) * 2.0;

                Color color = i % 3 == 0 ? new Color(139, 0, 0) : (i % 3 == 1 ? Color.BLACK : Color.WHITE);

                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(3.5f, 0.0f).build())
                        .setColorData(ColorParticleData.create(color, Color.BLACK).build())
                        .setLifetime(20 + RANDOM.nextInt(15))
                        .addMotion(vx, vy, vz)
                        .spawn(level, x, y, z);
            }

            // Extra crimson sparks for impact
            for (int i = 0; i < 20; i++) {
                double vx = (RANDOM.nextDouble() - 0.5) * 3.0;
                double vy = (RANDOM.nextDouble()) * 2.0;
                double vz = (RANDOM.nextDouble() - 0.5) * 3.0;

                WorldParticleBuilder.create(LodestoneParticleTypes.SPARK_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(2.0f, 0.0f).build())
                        .setColorData(ColorParticleData.create(new Color(220, 20, 60), Color.BLACK).build())
                        .setSpinData(SpinParticleData.create(RANDOM.nextFloat() * 6.28f, RANDOM.nextFloat() * 0.4f).build())
                        .setLifetime(10 + RANDOM.nextInt(10))
                        .addMotion(vx, vy, vz)
                        .spawn(level, x, y, z);
            }
        }
    }
}

