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

    public DismantleProjectileEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public DismantleProjectileEntity(EntityType<? extends Projectile> entityType, LivingEntity shooter, Level level) {
        this(entityType, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 deltaMovement = this.getDeltaMovement();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        // Block destruction logic
        if (!this.level().isClientSide) {
            net.minecraft.world.phys.AABB destroyBox = this.getBoundingBox().inflate(0.5);
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
            if (this.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                target.hurt(this.damageSources().playerAttack(player), 4.0F);
            } else {
                target.hurt(this.damageSources().generic(), 4.0F);
            }
            Vec3 look = this.getDeltaMovement().normalize();
            target.setDeltaMovement(target.getDeltaMovement().add(look.scale(0.5).add(0, 0.2, 0)));
            target.hurtMarked = true;
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && result.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }
}
