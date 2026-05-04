package com.my.kaisen.util;
 
import com.my.kaisen.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
 
public class DomainHandler {
 
    /**
     * Generates a physical arena for the Domain Expansion.
     */
    public static void generateDomainArena(Level level, BlockPos center, Player caster, boolean isOpen) {
        if (level.isClientSide) return;
 
        if (!isOpen) {
            int radius = 15;
            int radiusSq = radius * radius;
            int innerRadiusSq = (radius - 1) * (radius - 1);
 
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        // Top half only
                        if (y < -1) continue;
 
                        double distSq = x * x + y * y + z * z;
                        BlockPos currentPos = center.offset(x, y, z);
 
                        if (distSq <= radiusSq) {
                            if (y == -1) {
                                // Flat Floor
                                level.setBlockAndUpdate(currentPos, Blocks.CRIMSON_NYLIUM.defaultBlockState());
                            } else if (distSq > innerRadiusSq) {
                                // Half-Sphere Shell
                                level.setBlockAndUpdate(currentPos, ModBlocks.DOMAIN_BARRIER.get().defaultBlockState());
                            } else {
                                // Interior
                                level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
 
        // Spawn the Malevolent Shrine entity behind the caster
        com.my.kaisen.entity.ShrineEntity shrine = new com.my.kaisen.entity.ShrineEntity(com.my.kaisen.registry.ModEntities.SHRINE.get(), level);
        
        // Offset behind the caster
        float yaw = caster.getYRot();
        double rad = Math.toRadians(yaw);
        double offsetX = Math.sin(rad) * 2.5;
        double offsetZ = -Math.cos(rad) * 2.5;
        
        shrine.setPos(center.getX() + 0.5 + offsetX, center.getY() + (isOpen ? 0 : 0.5), center.getZ() + 0.5 + offsetZ);
        shrine.setYRot(yaw);
        shrine.setOwner(caster);
        shrine.setOpen(isOpen);
        level.addFreshEntity(shrine);
        
        // Cinematic Sound
        level.playSound(null, center, com.my.kaisen.registry.ModSounds.sukuna_awekening.get(), net.minecraft.sounds.SoundSource.PLAYERS, 4.0F, 1.0F);
    }
}
