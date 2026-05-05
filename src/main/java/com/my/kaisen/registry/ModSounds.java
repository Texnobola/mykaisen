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

    public static final DeferredHolder<SoundEvent, SoundEvent> GROUND_BREAKING = SOUND_EVENTS.register("ground_breaking",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "ground_breaking")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SWING_BACK = SOUND_EVENTS.register("swing_back",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "swing_back")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SWING_BACK_AIR = SOUND_EVENTS.register("swing_back_air",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "swing_back_air")));

    public static final DeferredHolder<SoundEvent, SoundEvent> EACH_BLOW_IMPACT = SOUND_EVENTS.register("each_blow_impact",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "each_blow_impact")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGING_DIVERGENT_FIST = SOUND_EVENTS.register("charging_divergent_fist",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "charging_divergent_fist")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGING_DIVERGENT_FIST_2 = SOUND_EVENTS.register("charging_divergent_fist_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "charging_divergent_fist_2")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BODY_HIT_DF = SOUND_EVENTS.register("body_hit_df",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "body_hit_df")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DIVERGENT_FIST_HIT = SOUND_EVENTS.register("divergent_fist_hit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "divergent_fist_hit")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_FLASH = SOUND_EVENTS.register("black_flash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "black_flash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_FLASH_FINAL = SOUND_EVENTS.register("black_flash_final",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "black_flash_final")));

    public static final DeferredHolder<SoundEvent, SoundEvent> manji_stance = SOUND_EVENTS.register("startup_sound_mk",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "startup_sound_mk")));

    public static final DeferredHolder<SoundEvent, SoundEvent> manji_dash = SOUND_EVENTS.register("swing_mk",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "swing_mk")));

    public static final DeferredHolder<SoundEvent, SoundEvent> manji_kick = SOUND_EVENTS.register("slam_mk",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "slam_mk")));

    public static final DeferredHolder<SoundEvent, SoundEvent> universal_awekekning_sound = SOUND_EVENTS.register("universal_awekekning_sound",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "universal_awekekning_sound")));

    public static final DeferredHolder<SoundEvent, SoundEvent> sukuna_awekening = SOUND_EVENTS.register("sukuna_awekening",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "sukuna_awekening")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_CHARGING = SOUND_EVENTS.register("charging_up",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "charging_up")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_FLAME_FORMING = SOUND_EVENTS.register("flame_forming",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "flame_forming")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_ARROW_CREATED = SOUND_EVENTS.register("arrow_created",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "arrow_created")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_FLAME_IDLE = SOUND_EVENTS.register("flame_idle",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "flame_idle")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_SHOOT = SOUND_EVENTS.register("fuga_shoot",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "fuga_shoot")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUGA_HITS = SOUND_EVENTS.register("fuga_hits",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "fuga_hits")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DISMANTLE_SLASH = SOUND_EVENTS.register("dismantle_slash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "dismantle_slash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CLEAVE_SLASH = SOUND_EVENTS.register("cleave_slash",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "cleave_slash")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SUKUNA_MOCKING = SOUND_EVENTS.register("sukuna_mocking",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "sukuna_mocking")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
