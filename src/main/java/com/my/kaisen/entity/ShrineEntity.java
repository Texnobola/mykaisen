package com.my.kaisen.entity;
 
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
 
public class ShrineEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
 
    public ShrineEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
 
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > 400) { // Disappear after 20 seconds
            this.discard();
        }
    }
 
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }
 
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }
 
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
 
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
 
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
