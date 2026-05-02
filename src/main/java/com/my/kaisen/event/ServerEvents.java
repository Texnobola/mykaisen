package com.my.kaisen.event;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.command.ToggleCooldownsCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = MyKaisen.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ToggleCooldownsCommand.register(event.getDispatcher());
    }
}
