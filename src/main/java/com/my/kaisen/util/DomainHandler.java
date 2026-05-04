package com.my.kaisen.util;
 
import com.my.kaisen.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
 
public class DomainHandler {
 
    /**
     * Builds or removes a specific layer of the domain sphere.
     */
    public static void handleDomainLayer(Level level, BlockPos center, int radius, int yOffset, boolean remove) {
        if (level.isClientSide) return;
 
        int radiusSq = radius * radius;
        int innerRadiusSq = (radius - 1) * (radius - 1);
 
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distSq = x * x + yOffset * yOffset + z * z;
                BlockPos currentPos = center.offset(x, yOffset, z);
 
                if (distSq <= radiusSq) {
                    if (remove) {
                        BlockState state = level.getBlockState(currentPos);
                        if (state.is(ModBlocks.DOMAIN_BARRIER.get()) || state.is(ModBlocks.DOMAIN_FLOOR.get())) {
                            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    } else {
                        if (yOffset == -1) {
                            // Flat Floor
                            level.setBlock(currentPos, ModBlocks.DOMAIN_FLOOR.get().defaultBlockState(), 2);
                        } else if (distSq > innerRadiusSq) {
                            // Half-Sphere Shell
                            level.setBlock(currentPos, ModBlocks.DOMAIN_BARRIER.get().defaultBlockState(), 2);
                        } else {
                            // Interior
                            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }
 
    /**
     * Spawns the Malevolent Shrine entity behind the caster.
     */
    public static com.my.kaisen.entity.ShrineEntity spawnShrine(Level level, BlockPos center, Player caster, boolean isOpen) {
        com.my.kaisen.entity.ShrineEntity shrine = new com.my.kaisen.entity.ShrineEntity(com.my.kaisen.registry.ModEntities.SHRINE.get(), level);
        
        float yaw = caster.getYRot();
        double rad = Math.toRadians(yaw);
        double offsetX = Math.sin(rad) * 2.5;
        double offsetZ = -Math.cos(rad) * 2.5;
        
        shrine.setPos(center.getX() + 0.5 + offsetX, center.getY() + (isOpen ? 0 : 0.5), center.getZ() + 0.5 + offsetZ);
        shrine.setYRot(yaw);
        shrine.setOwner(caster);
        shrine.setOpen(isOpen);
        level.addFreshEntity(shrine);
        
        level.playSound(null, center, com.my.kaisen.registry.ModSounds.sukuna_awekening.get(), net.minecraft.sounds.SoundSource.PLAYERS, 4.0F, 1.0F);
        return shrine;
    }
}
