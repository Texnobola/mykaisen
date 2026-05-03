package com.my.kaisen.registry;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.entity.DismantleProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MyKaisen.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DismantleProjectileEntity>> DISMANTLE_PROJECTILE = ENTITY_TYPES.register("dismantle_projectile",
            () -> EntityType.Builder.<DismantleProjectileEntity>of(DismantleProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("dismantle_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.my.kaisen.entity.FugaProjectileEntity>> FUGA_PROJECTILE = ENTITY_TYPES.register("fuga_projectile",
            () -> EntityType.Builder.<com.my.kaisen.entity.FugaProjectileEntity>of(com.my.kaisen.entity.FugaProjectileEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("fuga_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
