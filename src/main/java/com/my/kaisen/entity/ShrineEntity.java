package com.my.kaisen.entity;
 
import com.my.kaisen.registry.ModBlocks;
import com.my.kaisen.registry.ModSounds;
import com.my.kaisen.util.DomainHandler;
import com.my.kaisen.network.SpawnDomainAshPayload;
import com.my.kaisen.network.SpawnDismantleVfxPayload;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
 
import java.util.List;
import java.util.UUID;
 
public class ShrineEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(ShrineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ShrineEntity.class, EntityDataSerializers.INT);
 
    public enum DomainState { FORMING, ACTIVE, COLLAPSING }
 
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private int lifeTicks = 0;
    private int stateTicks = 0;
    private BlockPos centerPos;
 
    public static final int MAX_ACTIVE_TICKS = 1200;
    public static final int MAX_COLLAPSE_TICKS = 160;
 
    public ShrineEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
 
    public void setOwner(Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
        }
    }
    
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
 
    public void setOpen(boolean open) {
        this.entityData.set(OPEN, open);
    }
 
    public boolean isOpen() {
        return this.entityData.get(OPEN);
    }
 
    public void setState(DomainState state) {
        this.entityData.set(STATE, state.ordinal());
        this.stateTicks = 0;
    }
 
    public DomainState getCurrentState() {
        return DomainState.values()[this.entityData.get(STATE)];
    }
 
    @Override
    public void tick() {
        super.tick();
        if (centerPos == null) centerPos = this.blockPosition();
 
        if (!this.level().isClientSide) {
            lifeTicks++;
            stateTicks++;
 
            DomainState state = getCurrentState();
            
            if (state == DomainState.FORMING) {
                if (!isOpen() && stateTicks <= 20) {
                    // Layer by layer generation
                    int radius = 15;
                    int yLayer = stateTicks - 2; // Starts from -1 up to 18 (we stop at 15)
                    if (yLayer <= radius) {
                        DomainHandler.handleDomainLayer(this.level(), centerPos, radius, yLayer, false);
                    }
                }
                if (stateTicks >= 20) {
                    setState(DomainState.ACTIVE);
                }
            } 
            else if (state == DomainState.ACTIVE) {
                tickActive();
                if (stateTicks >= MAX_ACTIVE_TICKS) {
                    setState(DomainState.COLLAPSING);
                }
            } 
            else if (state == DomainState.COLLAPSING) {
                tickCollapsing();
                if (stateTicks >= MAX_COLLAPSE_TICKS) {
                    this.discard();
                }
            }
        }
    }
 
    private void tickActive() {
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
        if (stateTicks % 5 == 0) {
            AABB area = this.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, 
                    (entity) -> entity.getUUID() != this.ownerUUID && entity.isAlive());
 
            for (LivingEntity target : targets) {
                target.hurt(this.damageSources().magic(), 2.0F);
                
                ServerLevel serverLevel = (ServerLevel) this.level();
                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.DISMANTLE_SLASH.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, 
                        new SpawnDismantleVfxPayload(target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), (float)Math.random() * 360f));
            }
        }
 
        // Shibuya-level Devastation (Open Barrier only)
        if (isOpen()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 40; i++) {
                int dx = this.getRandom().nextInt(400) - 200;
                int dz = this.getRandom().nextInt(400) - 200;
                BlockPos targetPos = centerPos.offset(dx, 0, dz);
                BlockPos surfacePos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos).below();
                
                net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(surfacePos);
                if (!state.isAir() && state.getDestroySpeed(serverLevel, surfacePos) >= 0 && state.getBlock() != ModBlocks.DOMAIN_BARRIER.get() && state.getBlock() != ModBlocks.DOMAIN_FLOOR.get()) {
                    serverLevel.setBlock(surfacePos, Blocks.AIR.defaultBlockState(), 2);
                    
                    if (this.getRandom().nextInt(5) == 0) {
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                                new SpawnDomainAshPayload(surfacePos.getX() + 0.5, surfacePos.getY() + 0.5, surfacePos.getZ() + 0.5));
                    }
                }
            }
        }
    }
 
    private void tickCollapsing() {
        if (!isOpen()) {
            // Layer by layer removal
            int radius = 15;
            int yLayer = 15 - (stateTicks / 10); // Remove layers over time
            if (yLayer >= -1) {
                DomainHandler.handleDomainLayer(this.level(), centerPos, radius, yLayer, true);
            }
        }
 
        // Stochastic removal for everything else/Visual ash
        ServerLevel serverLevel = (ServerLevel) this.level();
        for (int i = 0; i < 20; i++) {
            int dx = this.getRandom().nextInt(40) - 20;
            int dy = this.getRandom().nextInt(20) - 2;
            int dz = this.getRandom().nextInt(40) - 20;
            BlockPos targetPos = centerPos.offset(dx, dy, dz);
            
            if (this.getRandom().nextInt(3) == 0) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                        new SpawnDomainAshPayload(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5));
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
        builder.define(STATE, DomainState.FORMING.ordinal());
    }
 
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.lifeTicks = compound.getInt("LifeTicks");
        this.stateTicks = compound.getInt("StateTicks");
        this.setOpen(compound.getBoolean("IsOpen"));
        this.setState(DomainState.values()[compound.getInt("DomainState")]);
        if (compound.contains("CenterX")) {
            this.centerPos = new BlockPos(compound.getInt("CenterX"), compound.getInt("CenterY"), compound.getInt("CenterZ"));
        }
    }
 
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putInt("StateTicks", this.stateTicks);
        compound.putBoolean("IsOpen", this.isOpen());
        compound.putInt("DomainState", getCurrentState().ordinal());
        if (this.centerPos != null) {
            compound.putInt("CenterX", centerPos.getX());
            compound.putInt("CenterY", centerPos.getY());
            compound.putInt("CenterZ", centerPos.getZ());
        }
    }
 
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
 
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
