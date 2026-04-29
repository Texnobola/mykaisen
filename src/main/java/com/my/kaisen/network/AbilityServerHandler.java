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
        // 1. Apply Cooldown (Placeholder)
        // TODO: Apply a 10-second (200 tick) cooldown to the player using standard capabilities or item cooldowns

        // 2. Propel player forward roughly 3 blocks
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

        // 3. Register player in active dashes with a 10-tick timer
        CombatTickHandler.activeDashes.put(player.getUUID(), 10);
    }
}
