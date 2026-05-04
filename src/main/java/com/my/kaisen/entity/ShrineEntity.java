package com.my.kaisen.entity;
 
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
 
import java.util.List;
import java.util.UUID;
 
public class ShrineEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(ShrineEntity.class, EntityDataSerializers.BOOLEAN);
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private int lifeTicks = 0;
 
    public ShrineEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
 
    public void setOwner(Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
        }
    }
 
    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }
 
    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }
 
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            lifeTicks++;
 
            LivingEntity owner = null;
            if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(this.ownerUUID);
                if (entity instanceof LivingEntity) {
                    owner = (LivingEntity) entity;
                }
            }
 
            // Caster Buffs
            if (owner != null && owner.isAlive()) {
                owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 10, 2, false, false));
                owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, false, false));
                owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 1, false, false));
            }
 
            double radius = isOpen() ? 200.0 : 14.0;
 
            // Sure-Hit Dismantle Storm (Every 5 ticks)
            if (lifeTicks % 5 == 0) {
                AABB area = this.getBoundingBox().inflate(radius);
                List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, 
                        (entity) -> entity.getUUID() != this.ownerUUID && entity.isAlive());
 
                for (LivingEntity target : targets) {
                    target.hurt(this.damageSources().magic(), 2.0F);
                    
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), com.my.kaisen.registry.ModSounds.DISMANTLE_SLASH.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, 
                            new com.my.kaisen.network.SpawnDismantleVfxPayload(target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), (float)Math.random() * 360f));
                }
            }
 
            // Shibuya-level Devastation (Open Barrier only)
            if (isOpen()) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                for (int i = 0; i < 40; i++) {
                    int dx = this.getRandom().nextInt(400) - 200;
                    int dz = this.getRandom().nextInt(400) - 200;
                    BlockPos targetPos = this.blockPosition().offset(dx, 0, dz);
                    BlockPos surfacePos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos).below();
                    
                    net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(surfacePos);
                    if (!state.isAir() && state.getDestroySpeed(serverLevel, surfacePos) >= 0 && state.getBlock() != com.my.kaisen.registry.ModBlocks.DOMAIN_BARRIER.get()) {
                        // Flag 2: Update clients but prevent heavy vanilla physics/particles
                        serverLevel.setBlock(surfacePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                        
                        // GPU-Friendly Lodestone Ash (1-in-5 chance)
                        if (this.getRandom().nextInt(5) == 0) {
                            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                                    new com.my.kaisen.network.SpawnDomainAshPayload(surfacePos.getX() + 0.5, surfacePos.getY() + 0.5, surfacePos.getZ() + 0.5));
                        }
                    }
                }
            }
 
            if (lifeTicks > 200) { // Disappear after 10 seconds
                this.discard();
            }
        }
    }
 
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
 
    @Override
    public boolean isPushable() {
        return false;
    }
 
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OPEN, false);
    }
 
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.lifeTicks = compound.getInt("LifeTicks");
        this.setOpen(compound.getBoolean("IsOpen"));
    }
 
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putBoolean("IsOpen", this.isOpen());
    }
 
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
 
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
