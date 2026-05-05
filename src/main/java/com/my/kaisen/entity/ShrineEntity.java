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
    private static final EntityDataAccessor<Integer> DUST_LEVEL = SynchedEntityData.defineId(ShrineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FORMING_TICKS = SynchedEntityData.defineId(ShrineEntity.class, EntityDataSerializers.INT);
 
    public enum DomainState { FORMING, ACTIVE, COLLAPSING }
 
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private int lifeTicks = 0;
    private int formingTicks = 0;
    private int activeTicks = 0;
    private int collapseTicks = 0;
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
    }
 
    public DomainState getCurrentState() {
        return DomainState.values()[this.entityData.get(STATE)];
    }
 
    public void setDustLevel(int level) {
        this.entityData.set(DUST_LEVEL, Math.min(1000, level));
    }
 
    public int getDustLevel() {
        return this.entityData.get(DUST_LEVEL);
    }
 
    public int getFormingTicks() {
        return this.entityData.get(FORMING_TICKS);
    }
 
    public void setFormingTicks(int ticks) {
        this.entityData.set(FORMING_TICKS, ticks);
    }
 
    @Override
    public void tick() {
        super.tick();
        if (centerPos == null) centerPos = this.blockPosition();
 
        if (!this.level().isClientSide) {
            lifeTicks++;
 
            switch (getCurrentState()) {
                case FORMING:
                    formingTicks++;
                    setFormingTicks(formingTicks);
                    
                    // Lock caster movement during forming
                    if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
                        Entity entity = serverLevel.getEntity(this.ownerUUID);
                        if (entity instanceof LivingEntity owner) {
                            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 20, false, false));
                            owner.setDeltaMovement(0, 0, 0);
                        }
                    }
 
                    if (formingTicks >= 40) { // 2 Seconds forming after the 5s charge
                        int radius = 15;
                        if (!this.isOpen()) {
                            for (int yLayer = -1; yLayer <= radius; yLayer++) {
                                DomainHandler.handleDomainLayer(this.level(), centerPos, radius, yLayer, false);
                            }
                        } else {
                            DomainHandler.handleDomainLayer(this.level(), centerPos, radius, -1, false);
                        }
                        
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                                new com.my.kaisen.network.SpawnDomainActivationVfxPayload(centerPos.getX(), centerPos.getY(), centerPos.getZ()));
 
                        setState(DomainState.ACTIVE);
                    }
                    break;
 
                case ACTIVE:
                    activeTicks++;
                    tickActive();
                    if (activeTicks >= MAX_ACTIVE_TICKS) {
                        setState(DomainState.COLLAPSING);
                    }
                    break;
 
                case COLLAPSING:
                    collapseTicks++;
                    tickCollapsing();
                    if (collapseTicks >= MAX_COLLAPSE_TICKS) {
                        this.discard();
                    }
                    break;
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
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 10, 3, false, false));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 2, false, false));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 2, false, false));
        }
 
        // Sure-Hit Dismantle Storm (Every 3 ticks - FASTER)
        if (activeTicks % 3 == 0) {
            double radius = isOpen() ? 200.0 : 15.0;
            AABB area = this.getBoundingBox().inflate(radius);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, 
                    (entity) -> entity.getUUID() != this.ownerUUID && entity.isAlive());
 
            for (LivingEntity target : targets) {
                float healthBefore = target.getHealth();
                target.hurt(this.damageSources().magic(), 4.0F); // Buffed Damage
                
                // If killed, increment dust
                if (!target.isAlive() || target.getHealth() < healthBefore) {
                     setDustLevel(getDustLevel() + 5);
                }
 
                ServerLevel serverLevel = (ServerLevel) this.level();
                if (activeTicks % 6 == 0) {
                    serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.DISMANTLE_SLASH.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
                
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, 
                        new SpawnDismantleVfxPayload(target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), (float)Math.random() * 360f));
            }
        }
 
        // Shibuya-level Devastation (Open Barrier only) - MASSIVELY BUFFED
        if (isOpen()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            for (int i = 0; i < 120; i++) { // Increased from 40
                int dx = this.getRandom().nextInt(400) - 200;
                int dz = this.getRandom().nextInt(400) - 200;
                BlockPos targetPos = centerPos.offset(dx, 0, dz);
                BlockPos surfacePos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos).below();
                
                net.minecraft.world.level.block.state.BlockState state = serverLevel.getBlockState(surfacePos);
                if (!state.isAir() && state.getDestroySpeed(serverLevel, surfacePos) >= 0 && state.getBlock() != ModBlocks.DOMAIN_BARRIER.get() && state.getBlock() != ModBlocks.DOMAIN_FLOOR.get()) {
                    serverLevel.setBlock(surfacePos, Blocks.AIR.defaultBlockState(), 2 | 16);
                    setDustLevel(getDustLevel() + 1);
 
                    if (this.getRandom().nextInt(10) == 0) {
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                                new SpawnDomainAshPayload(surfacePos.getX() + 0.5, surfacePos.getY() + 0.5, surfacePos.getZ() + 0.5));
                    }
                }
            }
        }
    }
 
    private void tickCollapsing() {
        if (!isOpen()) {
            int radius = 15;
            int yLayer = 15 - (collapseTicks / 10);
            if (yLayer >= -1) {
                DomainHandler.handleDomainLayer(this.level(), centerPos, radius, yLayer, true);
            }
        }
 
        ServerLevel serverLevel = (ServerLevel) this.level();
        for (int i = 0; i < 40; i++) {
            int dx = this.getRandom().nextInt(40) - 20;
            int dy = this.getRandom().nextInt(20) - 2;
            int dz = this.getRandom().nextInt(40) - 20;
            BlockPos targetPos = centerPos.offset(dx, dy, dz);
            
            if (this.getRandom().nextInt(2) == 0) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, 
                        new SpawnDomainAshPayload(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5));
            }
        }
    }
 
    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }
 
    @Override
    public boolean isPushable() { return false; }
 
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OPEN, false);
        builder.define(STATE, DomainState.FORMING.ordinal());
        builder.define(DUST_LEVEL, 0);
        builder.define(FORMING_TICKS, 0);
    }
 
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("OwnerUUID")) this.ownerUUID = compound.getUUID("OwnerUUID");
        this.lifeTicks = compound.getInt("LifeTicks");
        this.formingTicks = compound.getInt("FormingTicks");
        this.activeTicks = compound.getInt("ActiveTicks");
        this.collapseTicks = compound.getInt("CollapseTicks");
        this.setOpen(compound.getBoolean("IsOpen"));
        this.setState(DomainState.values()[compound.getInt("DomainState")]);
        this.setDustLevel(compound.getInt("DustLevel"));
        if (compound.contains("CenterX")) {
            this.centerPos = new BlockPos(compound.getInt("CenterX"), compound.getInt("CenterY"), compound.getInt("CenterZ"));
        }
    }
 
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) compound.putUUID("OwnerUUID", this.ownerUUID);
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putInt("FormingTicks", this.formingTicks);
        compound.putInt("ActiveTicks", this.activeTicks);
        compound.putInt("CollapseTicks", this.collapseTicks);
        compound.putBoolean("IsOpen", this.isOpen());
        compound.putInt("DomainState", getCurrentState().ordinal());
        compound.putInt("DustLevel", getDustLevel());
        if (this.centerPos != null) {
            compound.putInt("CenterX", centerPos.getX());
            compound.putInt("CenterY", centerPos.getY());
            compound.putInt("CenterZ", centerPos.getZ());
        }
    }
 
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
 
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
