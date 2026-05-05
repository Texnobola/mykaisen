package com.my.kaisen.entity;
 
import com.my.kaisen.network.CameraShakePayload;
import com.my.kaisen.network.SpawnFugaNukePayload;
import com.my.kaisen.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
 
import java.awt.*;
import java.util.List;
import java.util.Random;
 
public class FugaProjectileEntity extends Projectile {
    private static final Random RANDOM = new Random();
 
    public FugaProjectileEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
 
    public FugaProjectileEntity(EntityType<? extends Projectile> entityType, LivingEntity shooter, Level level) {
        this(entityType, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }
 
    @Override
    public void tick() {
        super.tick();
 
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.5f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(1.5f, 0.0f).build())
                        .setColorData(ColorParticleData.create(new Color(255, 100, 0), new Color(255, 50, 0)).build())
                        .setLifetime(10 + RANDOM.nextInt(10))
                        .setRandomOffset(0.2)
                        .spawn(this.level(), this.getX(), this.getY(), this.getZ());
            }
        }
 
        Vec3 deltaMovement = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }
 
        this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);
 
        if (!this.level().isClientSide && this.tickCount > 200) {
            this.discard();
        }
    }
 
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) return;
 
        ServerLevel serverLevel = (ServerLevel) this.level();
        LivingEntity shooter = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;
 
        if (shooter != null) {
            List<ShrineEntity> shrines = serverLevel.getEntitiesOfClass(ShrineEntity.class, this.getBoundingBox().inflate(300.0),
                    (e) -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(shooter.getUUID()));
 
            boolean synergized = false;
            for (ShrineEntity shrine : shrines) {
                if (shrine.isOpen() && shrine.getDustLevel() >= 1000) {
                    synergized = true;
                    
                    // THERMOBARIC WIPEOUT
                    AABB nukeArea = shrine.getBoundingBox().inflate(200.0);
                    List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, nukeArea, (e) -> e != shooter && e.isAlive());
 
                    for (LivingEntity target : targets) {
                        // "Turn them into dust"
                        target.hurt(this.damageSources().onFire(), 500.0F); // Instant Kill
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5, 1.0, 0.5, 0.05);
                        serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 1.0, 0.5, 0.1);
                    }
 
                    // Collapse Shrine immediately
                    shrine.setState(ShrineEntity.DomainState.COLLAPSING);
                    shrine.setDustLevel(0);
 
                    // Trigger Epic VFX
                    SpawnFugaNukePayload payload = new SpawnFugaNukePayload(shrine.getX(), shrine.getY(), shrine.getZ());
                    for (ServerPlayer p : serverLevel.players()) {
                        PacketDistributor.sendToPlayer(p, payload);
                        PacketDistributor.sendToPlayer(p, new CameraShakePayload(10.0f, 100));
                    }
                    break;
                }
            }
 
            if (!synergized) {
                this.level().explode(shooter, this.getX(), this.getY(), this.getZ(), 12.0F, true, Level.ExplosionInteraction.TNT);
            }
        }
 
        this.discard();
    }
 
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}
}
