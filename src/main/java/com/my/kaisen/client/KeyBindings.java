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
    public static final String KEY_ABILITY_2 = "key.mykaisen.ability2";
    public static final String KEY_ABILITY_3 = "key.mykaisen.ability3";
    public static final String KEY_ABILITY_4 = "key.mykaisen.ability4";

    public static final KeyMapping ABILITY_1_KEY = new KeyMapping(
            KEY_ABILITY_1,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KEY_CATEGORY_MY_KAISEN
    );

    public static final KeyMapping ABILITY_2_KEY = new KeyMapping(
            KEY_ABILITY_2,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_CATEGORY_MY_KAISEN
    );

    public static final KeyMapping ABILITY_3_KEY = new KeyMapping(
            KEY_ABILITY_3,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY_MY_KAISEN
    );

    public static final KeyMapping ABILITY_4_KEY = new KeyMapping(
            KEY_ABILITY_4,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY_MY_KAISEN
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(ABILITY_1_KEY);
        event.register(ABILITY_2_KEY);
        event.register(ABILITY_3_KEY);
        event.register(ABILITY_4_KEY);
    }
}
