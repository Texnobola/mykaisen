package com.my.kaisen.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import java.awt.Color;
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
            // Lodestone Trail
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
        if (this.level().isClientSide) {
            // LODESTONE CINEMATIC VFX
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();
            Level level = this.level();

            // Layer 1: The Core Flash
            WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                    .setTransparencyData(GenericParticleData.create(1.0f, 0.0f).build())
                    .setScaleData(GenericParticleData.create(15.0f, 0.0f).build())
                    .setColorData(ColorParticleData.create(new Color(255, 200, 100), new Color(255, 100, 0)).build())
                    .setLifetime(15)
                    .spawn(level, x, y, z);

            // Layer 2: The Shockwave Ring
            for (int i = 0; i < 60; i++) {
                double angle = i * 6.0;
                double rad = Math.toRadians(angle);
                double vx = Math.cos(rad) * 1.5;
                double vz = Math.sin(rad) * 1.5;

                WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(4.0f, 8.0f).build())
                        .setColorData(ColorParticleData.create(new Color(100, 100, 100), new Color(50, 50, 50)).build())
                        .setLifetime(30 + RANDOM.nextInt(20))
                        .addMotion(vx, 0, vz)
                        .spawn(level, x, y, z);
            }

            // Layer 3: The Mushroom Cloud
            for (int i = 0; i < 100; i++) {
                double vx = (RANDOM.nextDouble() - 0.5) * 0.8;
                double vy = 0.5 + RANDOM.nextDouble() * 2.0;
                double vz = (RANDOM.nextDouble() - 0.5) * 0.8;

                WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                        .setTransparencyData(GenericParticleData.create(0.8f, 0.0f).build())
                        .setScaleData(GenericParticleData.create(3.0f, 6.0f).build())
                        .setColorData(ColorParticleData.create(new Color(255, 60, 0), Color.BLACK).build())
                        .setLifetime(40 + RANDOM.nextInt(40))
                        .addMotion(vx, vy, vz)
                        .spawn(level, x, y, z);
            }
        }

        if (!this.level().isClientSide) {
            // Play impact sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), com.my.kaisen.registry.ModSounds.FUGA_HITS.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 2.0F, 1.0F);
            
            // Violent Screen Shake for everyone within 40 blocks
            for (net.minecraft.server.level.ServerPlayer p : ((net.minecraft.server.level.ServerLevel)this.level()).players()) {
                if (p.distanceToSqr(this) < 40 * 40) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new com.my.kaisen.network.CameraShakePayload(5.0f, 40));
                }
            }

            // 15.0F radius is apocalyptic. 'true' sets everything on fire.
            this.level().explode(this.getOwner(), this.getX(), this.getY(), this.getZ(), 15.0F, true, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }
}
