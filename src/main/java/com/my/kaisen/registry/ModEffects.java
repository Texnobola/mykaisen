package com.my.kaisen.registry;

import com.my.kaisen.MyKaisen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MyKaisen.MODID);

    public static class FreezeEffect extends MobEffect {
        public FreezeEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    public static final DeferredHolder<MobEffect, MobEffect> STUN = MOB_EFFECTS.register("stun", 
            () -> new FreezeEffect(MobEffectCategory.HARMFUL, 0x808080)
                    .addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "stun_speed"), 
                            -1.0, 
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> LOCK_IN = MOB_EFFECTS.register("lock_in", 
            () -> new FreezeEffect(MobEffectCategory.BENEFICIAL, 0x00FFFF)
                    .addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "lock_in_speed"), 
                            -1.0, 
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
