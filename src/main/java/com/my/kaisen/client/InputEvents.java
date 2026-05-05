package com.my.kaisen.client;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.network.AbilityPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class InputEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // We check input on Post tick to ensure the game has processed inputs for the frame
        if (KeyBindings.ABILITY_1_KEY.consumeClick()) {
            // Check for Cleave Web (Z + Right Shift)
            // We only check for Right Shift here; the server will validate Awakening and Character status.
            if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                PacketDistributor.sendToServer(new com.my.kaisen.network.TriggerCleaveWebPayload());
            } else {
                // Send payload for Cursed Strikes / Dismantle (Ability ID 1)
                PacketDistributor.sendToServer(new AbilityPayload(1));
            }
        }
        
        if (KeyBindings.ABILITY_2_KEY.consumeClick()) {
            // Send payload for Crushing Blow (Ability ID 2)
            PacketDistributor.sendToServer(new AbilityPayload(2));
        }

        if (KeyBindings.ABILITY_3_KEY.consumeClick()) {
            // Send payload for Divergent Fist (Ability ID 3)
            PacketDistributor.sendToServer(new AbilityPayload(3));
        }

        if (KeyBindings.ABILITY_4_KEY.consumeClick()) {
            // Send payload for Manji Kick (Ability ID 4)
            PacketDistributor.sendToServer(new AbilityPayload(4));
        }

        if (KeyBindings.SWITCH_MODE_KEY.consumeClick()) {
            // Send payload to toggle battle mode
            PacketDistributor.sendToServer(new com.my.kaisen.network.ToggleBattleModePayload());
        }

        if (KeyBindings.AWAKEN_KEY.consumeClick()) {
            // Send payload to trigger Awakening
            PacketDistributor.sendToServer(new com.my.kaisen.network.AwakenPayload());
        }

    }
}
