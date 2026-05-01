package com.my.kaisen.client;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.registry.ModEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        if (event.getPlayer().hasEffect(ModEffects.STUN) || event.getPlayer().hasEffect(ModEffects.LOCK_IN)) {
            event.setNewFovModifier(1.0f);
        }
    }
}
