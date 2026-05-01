package com.my.kaisen.client;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.network.CameraShakePayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.Random;

/**
 * Client-side camera shake system.
 *
 * State is intentionally plain static fields — only one local player
 * can be shaking at a time on any given client.
 */
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientShakeHandler {

    private static float currentIntensity = 0f;
    private static int   remainingTicks   = 0;
    private static final Random RANDOM    = new Random();

    // -----------------------------------------------------------------------
    // Network entry point — called from NetworkHandler on the render thread
    // -----------------------------------------------------------------------

    public static void handleShake(CameraShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Always take the strongest shake if one is already running
            if (payload.intensity() >= currentIntensity) {
                currentIntensity = payload.intensity();
                remainingTicks   = payload.durationTicks();
            }
        });
    }

    // -----------------------------------------------------------------------
    // Tick decay — reduce intensity linearly each tick so it eases out
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks == 0) {
                currentIntensity = 0f;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Camera angle injection — applied every render frame (interpolated)
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (remainingTicks <= 0 || currentIntensity <= 0f) return;

        // Progress: 1.0 at start, 0.0 at end — shake decays naturally
        float progress = (float) remainingTicks / 20f; // normalize against a 20-tick max reference
        float scale    = Math.min(progress, 1.0f) * currentIntensity;

        float pitchOffset = (float) (RANDOM.nextGaussian() * scale);
        float yawOffset   = (float) (RANDOM.nextGaussian() * scale);

        event.setPitch(event.getPitch() + pitchOffset);
        event.setYaw(event.getYaw() + yawOffset);
    }
}
