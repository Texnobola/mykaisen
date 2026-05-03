package com.my.kaisen.util;
 
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
 
public class DomainHandler {
 
    /**
     * Generates a physical arena for the Domain Expansion.
     */
    public static void generateDomainArena(Level level, BlockPos center, Player caster) {
        if (level.isClientSide) return;
 
        int radius = 15;
        int radiusSq = radius * radius;
        int innerRadiusSq = (radius - 1) * (radius - 1);
 
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distSq = x * x + y * y + z * z;
                    BlockPos currentPos = center.offset(x, y, z);
 
                    if (distSq <= radiusSq) {
                        if (distSq > innerRadiusSq) {
                            // Shell
                            level.setBlockAndUpdate(currentPos, Blocks.OBSIDIAN.defaultBlockState());
                        } else {
                            // Interior
                            if (y <= -radius + 2) {
                                // Floor within the inner radius
                                level.setBlockAndUpdate(currentPos, Blocks.CRIMSON_NYLIUM.defaultBlockState());
                            } else {
                                // Air
                                level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
 
        // Spawn the Malevolent Shrine entity at the center
        com.my.kaisen.entity.ShrineEntity shrine = new com.my.kaisen.entity.ShrineEntity(com.my.kaisen.registry.ModEntities.SHRINE.get(), level);
        shrine.setPos(center.getX() + 0.5, center.getY() - (radius - 2), center.getZ() + 0.5);
        level.addFreshEntity(shrine);
    }
}
