package com.my.kaisen.registry;

import com.my.kaisen.MyKaisen;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.lodestar.lodestone.systems.particle.world.type.LodestoneWorldParticleType;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MyKaisen.MODID);

    public static final Supplier<LodestoneWorldParticleType> DISMANTLE_SLASH = PARTICLES.register("dismantle_slash", LodestoneWorldParticleType::new);
    public static final Supplier<LodestoneWorldParticleType> GLOWING_FIRE = PARTICLES.register("glowing_fire", LodestoneWorldParticleType::new);
    public static final Supplier<LodestoneWorldParticleType> CLEAVE_SLASH = PARTICLES.register("cleave_slash", LodestoneWorldParticleType::new);
    public static final Supplier<LodestoneWorldParticleType> CLEAVE_WEB = PARTICLES.register("cleave_web", LodestoneWorldParticleType::new);

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
