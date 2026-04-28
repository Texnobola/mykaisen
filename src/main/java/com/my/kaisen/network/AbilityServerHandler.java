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
                }
            }
        });
    }

    private static void executeCursedStrikesDash(ServerPlayer player) {
        // Get the direction the player is looking
        Vec3 lookVec = player.getLookAngle();
        
        // Multiply vector for a dash effect
        double dashMultiplier = 2.0;
        
        // Create a new motion vector based on looking direction
        Vec3 dashMotion = new Vec3(
                lookVec.x * dashMultiplier,
                0.2, // Small vertical hop to overcome ground friction briefly
                lookVec.z * dashMultiplier
        );

        player.setDeltaMovement(dashMotion);
        
        // CRITICAL: Tells the server to forcefully update the client with this new movement vector immediately.
        // Without this, the client will rubberband back to its original position because it didn't expect the server to move it.
        player.hurtMarked = true; 
    }
}
