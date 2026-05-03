package com.my.kaisen.entity;

import com.my.kaisen.registry.ModParticles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

import java.awt.*;

public class DismantleProjectileEntity extends Projectile {

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> GIANT = net.minecraft.network.syncher.SynchedEntityData.defineId(DismantleProjectileEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    public DismantleProjectileEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public DismantleProjectileEntity(EntityType<? extends Projectile> entityType, LivingEntity shooter, Level level) {
        this(entityType, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        if (!shooter.onGround()) {
            this.setGiant(true);
        }
    }

    public boolean isGiant() {
        return this.entityData.get(GIANT);
    }

    public void setGiant(boolean giant) {
        this.entityData.set(GIANT, giant);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 deltaMovement = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHit(hitResult);
        }

        this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        // Block destruction logic (Penetration)
        if (!this.level().isClientSide) {
            double inflation = this.isGiant() ? 1.5 : 0.5;
            net.minecraft.world.phys.AABB destroyBox = this.getBoundingBox().inflate(inflation);
            net.minecraft.core.BlockPos min = net.minecraft.core.BlockPos.containing(destroyBox.minX, destroyBox.minY, destroyBox.minZ);
            net.minecraft.core.BlockPos max = net.minecraft.core.BlockPos.containing(destroyBox.maxX, destroyBox.maxY, destroyBox.maxZ);

            for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(min, max)) {
                net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
                if (!state.isAir() && state.getDestroySpeed(this.level(), pos) >= 0) {
                    this.level().destroyBlock(pos, true);
                }
            }
        }

        if (!this.level().isClientSide && this.tickCount > 40) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target && target != this.getOwner()) {
            float damage = this.isGiant() ? 12.0F : 4.0F;
            if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                target.hurt(this.damageSources().playerAttack(player), damage);
            } else {
                target.hurt(this.damageSources().generic(), damage);
            }
            Vec3 look = this.getDeltaMovement().normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(look.scale(0.5).add(0, 0.2, 0)));
            target.hurtMarked = true;
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        // Empty override to allow block penetration
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(GIANT, false);
    }
}
