package com.my.kaisen.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.my.kaisen.MyKaisen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final String KEY_CATEGORY_MY_KAISEN = "key.category.mykaisen";
    public static final String KEY_ABILITY_1 = "key.mykaisen.ability1";

    public static final KeyMapping ABILITY_1_KEY = new KeyMapping(
            KEY_ABILITY_1,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KEY_CATEGORY_MY_KAISEN
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(ABILITY_1_KEY);
    }
}
