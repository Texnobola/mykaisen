package com.my.kaisen.client;
 
import com.my.kaisen.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;
import team.lodestar.lodestone.systems.easing.Easing;
import com.my.kaisen.network.SpawnFugaVfxPayload;
import com.my.kaisen.network.SpawnFugaNukePayload;
import com.my.kaisen.network.SpawnDismantleVfxPayload;
import com.my.kaisen.network.SpawnCleaveRushVfxPayload;
import com.my.kaisen.network.SpawnBlackFlashPayload;
import com.my.kaisen.network.SpawnDivergentAuraPayload;
import com.my.kaisen.network.SpawnCursedStrikesVfxPayload;
import com.my.kaisen.network.SpawnCrushingBlowVfxPayload;
import com.my.kaisen.network.SpawnAwakeningVfxPayload;
import com.my.kaisen.network.SpawnDomainAshPayload;
import com.my.kaisen.network.SpawnDomainActivationVfxPayload;
 
import java.awt.*;
import java.util.Random;
import java.util.Optional;
 
public class ClientVfxHandler {
    private static final Random RANDOM = new Random();
    private static int absoluteCombo = 0;
    private static int blackFlashFlashTicks = 0;
    private static int blackFlashColorType = 0; // 0 for B-R, 1 for B-G-W
 
    private static void addScreenshake(int duration, float intensity) {
        ScreenshakeHandler.addScreenshake(new ScreenshakeInstance(duration, intensity, intensity, 0f, Easing.LINEAR, Easing.SINE_OUT, 1.0f, Optional.empty()));
    }
 
    public static void spawnFugaNuke(Level level, double x, double y, double z) {
        // 1. Implosion (Vacuum)
        for (int i = 0; i < 50; i++) {
            double rx = (RANDOM.nextDouble() - 0.5) * 10;
            double ry = (RANDOM.nextDouble() - 0.5) * 10;
            double rz = (RANDOM.nextDouble() - 0.5) * 10;
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.0f, 0.8f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(2.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(Color.ORANGE, Color.RED).build())
                    .setLifetime(15)
                    .setMotion(new Vec3(-rx * 0.2, -ry * 0.2, -rz * 0.2))
                    .spawn(level, x + rx, y + ry, rz + z);
        }
 
        // 2. Apocalyptic Shockwave (Horizontal Ring)
        for (int i = 0; i < 360; i += 2) {
            double rad = Math.toRadians(i);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(5.0f, 15.0f).build())
                    .setColorData(ColorParticleData.create(new Color(255, 60, 0), new Color(50, 10, 0)).build())
                    .setLifetime(40 + RANDOM.nextInt(20))
                    .setMotion(new Vec3(cos * 2.5, 0.1, sin * 2.5))
                    .spawn(level, x, y + 0.5, z);
        }
 
        // 3. Mushroom Cloud (Vertical Pillar)
        for (int i = 0; i < 100; i++) {
            WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.6f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(4.0f, 12.0f).build())
                    .setColorData(ColorParticleData.create(Color.DARK_GRAY, Color.BLACK).build())
                    .setLifetime(80 + RANDOM.nextInt(40))
                    .setMotion(new Vec3((RANDOM.nextDouble() - 0.5) * 0.5, 1.5 + RANDOM.nextDouble(), (RANDOM.nextDouble() - 0.5) * 0.5))
                    .spawn(level, x, y, z);
        }
 
        // 4. Blinding Flash & Fire Dome
        for (int i = 0; i < 200; i++) {
             double theta = RANDOM.nextDouble() * Math.PI;
             double phi = RANDOM.nextDouble() * 2 * Math.PI;
             double dx = Math.sin(theta) * Math.cos(phi);
             double dy = Math.cos(theta);
             double dz = Math.sin(theta) * Math.sin(phi);
             
             WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(3.0f, 8.0f).build())
                    .setColorData(ColorParticleData.create(Color.WHITE, Color.ORANGE).build())
                    .setLifetime(30 + RANDOM.nextInt(20))
                    .setMotion(new Vec3(dx * 1.5, Math.abs(dy) * 1.5, dz * 1.5))
                    .spawn(level, x, y + 2, z);
        }
 
        addScreenshake(100, 3.0f);
    }
 
    public static void spawnBlackFlash(Level level, double x, double y, double z) {
        if (level == null) return;
        
        blackFlashFlashTicks = 6; // ~0.3s
        blackFlashColorType = level.random.nextBoolean() ? 0 : 1;

        for (int i = 0; i < 360; i += 10) {
            double rad = Math.toRadians(i);
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(2.5f, 10.0f, 0.0f).build()) // Much larger
                    .setColorData(ColorParticleData.create(Color.BLACK, i % 20 == 0 ? Color.WHITE : Color.RED).build())
                    .setLifetime(25 + RANDOM.nextInt(15))
                    .setMotion(new Vec3(Math.cos(rad) * 1.5, 0.2, Math.sin(rad) * 1.5))
                    .spawn(level, x, y, z);
        }
        addScreenshake(60, 6.0f); // Massive shake
    }
 
    public static void spawnMenacingAura(Level level, double x, double y, double z, double width, double height) {
        if (RANDOM.nextInt(3) == 0) {
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.4f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(0.8f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(50, 0, 0), Color.BLACK).build())
                    .setLifetime(20)
                    .setMotion(new Vec3(0, 0.1, 0))
                    .spawn(level, x + (RANDOM.nextDouble() - 0.5) * width, y + RANDOM.nextDouble() * height, z + (RANDOM.nextDouble() - 0.5) * width);
        }
    }
 
    public static void handleBlackFlashVfx(com.my.kaisen.network.SpawnBlackFlashPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> spawnBlackFlash(Minecraft.getInstance().level, payload.x(), payload.y(), payload.z()));
    }

    public static void handleSyncCombo(com.my.kaisen.network.SyncComboPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            absoluteCombo = payload.combo();
        });
    }

    public static int getAbsoluteCombo() {
        return absoluteCombo;
    }

    public static void renderBlackFlashOverlay(net.minecraft.client.gui.GuiGraphics guiGraphics, int width, int height) {
        if (blackFlashFlashTicks > 0) {
            int color = 0xAA000000; // Black
            if (blackFlashFlashTicks % 2 == 0) {
                color = (blackFlashColorType == 0) ? 0xAAFF0000 : 0xAAFFFFFF; // Red or White
            }
            
            guiGraphics.fill(0, 0, width, height, color);
            blackFlashFlashTicks--;
        }
    }
 
    public static void handleDivergentAuraVfx(com.my.kaisen.network.SpawnDivergentAuraPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            for (int i = 0; i < 20; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.6f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.5f, 2.0f, 0.0f).build())
                        .setColorData(ColorParticleData.create(new Color(0, 100, 255), Color.BLUE).build())
                        .setLifetime(20)
                        .setRandomMotion(0.1)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
        });
    }
 
    public static void handleCursedStrikesVfx(com.my.kaisen.network.SpawnCursedStrikesVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            for (int i = 0; i < 15; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.2f, 0.8f, 0.0f).build())
                        .setColorData(ColorParticleData.create(Color.BLUE, Color.CYAN).build())
                        .setLifetime(10)
                        .setRandomMotion(0.2)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
        });
    }
 
    public static void handleCrushingBlowVfx(com.my.kaisen.network.SpawnCrushingBlowVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            for (int i = 0; i < 30; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.5f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.0f, 3.0f).build())
                        .setColorData(ColorParticleData.create(Color.GRAY, Color.DARK_GRAY).build())
                        .setLifetime(30)
                        .setRandomMotion(0.3)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
            addScreenshake(15, 0.5f);
        });
    }
 
    public static void handleCameraShake(com.my.kaisen.network.CameraShakePayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> addScreenshake(payload.durationTicks(), payload.intensity()));
    }
 
    public static void handleAwakeningVfx(com.my.kaisen.network.SpawnAwakeningVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            for (int i = 0; i < 100; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.0f, 4.0f, 0.0f).build())
                        .setColorData(ColorParticleData.create(new Color(150, 0, 0), Color.BLACK).build())
                        .setLifetime(40)
                        .setRandomMotion(0.5)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
            addScreenshake(60, 2.0f);
        });
    }
 
    public static void handleDismantleVfx(com.my.kaisen.network.SpawnDismantleVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            float angle = (float) Math.toRadians(payload.yRot());
            for (int i = 0; i < 10; i++) {
                double offset = (i - 5) * 0.2;
                WorldParticleBuilder.create(com.my.kaisen.registry.ModParticles.DISMANTLE_SLASH.get())
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.1f, 0.3f, 0.0f).build())
                        .setColorData(ColorParticleData.create(Color.WHITE, Color.LIGHT_GRAY).build())
                        .setLifetime(5)
                        .spawn(level, payload.x() + Math.cos(angle) * offset, payload.y(), payload.z() + Math.sin(angle) * offset);
            }
        });
    }
 
    public static void handleCleaveRushVfx(com.my.kaisen.network.SpawnCleaveRushVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            Color color = payload.isFinalHit() ? Color.RED : Color.WHITE;
            int count = payload.isFinalHit() ? 40 : 10;
            for (int i = 0; i < count; i++) {
                WorldParticleBuilder.create(com.my.kaisen.registry.ModParticles.CLEAVE_SLASH.get())
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.8f, 2.5f, 0.0f).build())
                        .setColorData(ColorParticleData.create(color, Color.BLACK).build())
                        .setLifetime(15)
                        .setRandomMotion(0.3)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
        });
    }
 
    public static void handleDomainAshVfx(com.my.kaisen.network.SpawnDomainAshPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(0.4f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(0.5f, 2.0f).build())
                    .setColorData(ColorParticleData.create(Color.DARK_GRAY, Color.BLACK).build())
                    .setLifetime(40)
                    .setMotion(new Vec3(0, 0.05, 0))
                    .spawn(level, payload.x(), payload.y(), payload.z());
        });
    }
 
    public static void handleFugaNukeVfx(SpawnFugaNukePayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> spawnFugaNuke(Minecraft.getInstance().level, payload.x(), payload.y(), payload.z()));
    }
 
    public static void handleFugaVfx(SpawnFugaVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            double x = payload.x();
            double y = payload.y();
            double z = payload.z();
            
            // Core Flash
            for (int i = 0; i < 30; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.0f, 3.0f, 0.0f).build())
                        .setColorData(ColorParticleData.create(Color.WHITE, Color.ORANGE).build())
                        .setLifetime(15)
                        .setRandomMotion(0.4)
                        .spawn(level, x, y, z);
            }
            
            // Fire Ring
            for (int i = 0; i < 360; i += 15) {
                double rad = Math.toRadians(i);
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.5f, 1.5f, 0.0f).build())
                        .setColorData(ColorParticleData.create(Color.ORANGE, Color.RED).build())
                        .setLifetime(20)
                        .setMotion(new Vec3(Math.cos(rad) * 0.6, 0.1, Math.sin(rad) * 0.6))
                        .spawn(level, x, y, z);
            }
            
            addScreenshake(30, 2.0f);
        });
    }

    public static void handleDomainActivationVfx(com.my.kaisen.network.SpawnDomainActivationVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            for (int i = 0; i < 360; i += 5) {
                double rad = Math.toRadians(i);
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(2.0f, 8.0f).build())
                        .setColorData(ColorParticleData.create(Color.BLACK, new Color(100, 0, 0)).build())
                        .setLifetime(40)
                        .setMotion(new Vec3(Math.cos(rad) * 1.2, 0.1, Math.sin(rad) * 1.2))
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }
            addScreenshake(60, 4.0f);
        });
    }

    public static void handleCleaveVfx(com.my.kaisen.network.SpawnCleaveVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            
            // Slice effect - Sharper and Larger to show "real texture"
            WorldParticleBuilder.create(com.my.kaisen.registry.ModParticles.CLEAVE_SLASH.get())
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.8f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(5.0f, 5.0f, 0.0f).build()) // Big and sharp
                    .setSpinData(SpinParticleData.create(payload.rotation(), payload.rotation()).build())
                    .setLifetime(8)
                    .spawn(level, payload.x(), payload.y(), payload.z());
        });
    }

    public static void handleCleaveWebVfx(com.my.kaisen.network.SpawnCleaveWebVfxPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            // Massive central spiderweb pattern (Visualizes the web from the reference)
            WorldParticleBuilder.create(com.my.kaisen.registry.ModParticles.CLEAVE_WEB.get())
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(14.0f, 14.0f, 0.0f).build()) // Huge and static
                    .setLifetime(40)
                    .spawn(level, payload.x(), payload.y() + 0.1, payload.z());

            // Secondary faint web for depth
            WorldParticleBuilder.create(com.my.kaisen.registry.ModParticles.CLEAVE_WEB.get())
                    .setTransparencyData(GenericParticleData.create(0.5f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(8.0f, 20.0f, 0.0f).build())
                    .setSpinData(SpinParticleData.create(45.0f, 45.0f).build())
                    .setLifetime(20)
                    .spawn(level, payload.x(), payload.y() + 0.1, payload.z());

            // Radial "Shatter" effects to simulate ground breaking
            for (int i = 0; i < 360; i += 15) {
                double rad = Math.toRadians(i);
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.4f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(0.8f, 2.5f, 0.0f).build())
                        .setColorData(ColorParticleData.create(Color.WHITE, Color.DARK_GRAY).build())
                        .setLifetime(15 + RANDOM.nextInt(10))
                        .setMotion(new Vec3(Math.cos(rad) * 0.4, 0.05, Math.sin(rad) * 0.4))
                        .spawn(level, payload.x(), payload.y() + 0.1, payload.z());
            }

            // Smoke/Dust puff at center
            for (int i = 0; i < 20; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.3f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.0f, 3.0f).build())
                        .setColorData(ColorParticleData.create(Color.GRAY, Color.BLACK).build())
                        .setLifetime(25)
                        .setRandomMotion(0.2)
                        .spawn(level, payload.x(), payload.y(), payload.z());
            }

            addScreenshake(40, 1.8f);
        });
    }
}
