package com.my.kaisen.client;

import com.my.kaisen.network.SpawnBlackFlashPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;

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
     * Spawns an explosive burst of Lodestone particles (Black and Crimson).
     */
    public static void spawnBlackFlash(Level level, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            // Calculate random outward velocity
            double vx = (RANDOM.nextDouble() - 0.5) * 0.5;
            double vy = (RANDOM.nextDouble() - 0.5) * 0.5;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.5;

            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(1.5f, 0.0f).build())
                    .setColorData(ColorParticleData.create(Color.BLACK, new Color(180, 0, 0)).build())
                    .setLifetime(15 + RANDOM.nextInt(10))
                    .setRandomOffset(0.2)
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
}
