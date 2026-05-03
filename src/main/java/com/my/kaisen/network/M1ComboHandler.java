package com.my.kaisen.network;

import com.my.kaisen.MyKaisen;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.GAME)
public class M1ComboHandler {
    public static final Map<UUID, Integer> comboTicks = new HashMap<>();
    public static final Map<UUID, Integer> comboShots = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // Iterate through players in the combo maps
        for (UUID uuid : new java.util.ArrayList<>(comboTicks.keySet())) {
            int ticks = comboTicks.get(uuid);
            int shots = comboShots.getOrDefault(uuid, 0);

            if (ticks <= 0) {
                if (shots > 0) {
                    ServerPlayer player = (ServerPlayer) net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        spawnDismantle(player);
                        comboShots.put(uuid, shots - 1);
                        comboTicks.put(uuid, 4); // 0.2 seconds interval
                    } else {
                        comboTicks.remove(uuid);
                        comboShots.remove(uuid);
                    }
                } else {
                    comboTicks.remove(uuid);
                    comboShots.remove(uuid);
                }
            } else {
                comboTicks.put(uuid, ticks - 1);
            }
        }
    }

    private static void spawnDismantle(ServerPlayer player) {
        com.my.kaisen.entity.DismantleProjectileEntity dismantle = new com.my.kaisen.entity.DismantleProjectileEntity(com.my.kaisen.registry.ModEntities.DISMANTLE_PROJECTILE.get(), player, player.level());
        dismantle.setPos(player.getEyePosition());
        dismantle.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
        player.level().addFreshEntity(dismantle);
        
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
    }
}
