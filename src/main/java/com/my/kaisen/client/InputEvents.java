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
            // Send payload for Cursed Strikes (Ability ID 1)
            PacketDistributor.sendToServer(new AbilityPayload(1));
        }
        
        if (KeyBindings.ABILITY_2_KEY.consumeClick()) {
            // Send payload for Crushing Blow (Ability ID 2)
            PacketDistributor.sendToServer(new AbilityPayload(2));
        }

        if (KeyBindings.ABILITY_3_KEY.consumeClick()) {
            // Send payload for Divergent Fist (Ability ID 3)
            PacketDistributor.sendToServer(new AbilityPayload(3));
        }
    }
}
