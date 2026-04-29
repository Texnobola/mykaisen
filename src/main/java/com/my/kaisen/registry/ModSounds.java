package com.my.kaisen.registry;

import com.my.kaisen.MyKaisen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MyKaisen.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DASH = SOUND_EVENTS.register("dash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "dash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CURSED_STRIKES = SOUND_EVENTS.register("cursed_strikes",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "cursed_strikes")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SPIN_MID_AIR = SOUND_EVENTS.register("spin_mid_air",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "spin_mid_air")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DROPKICK = SOUND_EVENTS.register("dropkick",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "dropkick")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BLOW_AOE = SOUND_EVENTS.register("blow_aoe",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "blow_aoe")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
