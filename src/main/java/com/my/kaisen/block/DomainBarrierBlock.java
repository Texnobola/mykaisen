package com.my.kaisen.block;
 
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
 
public class DomainBarrierBlock extends Block {
    public DomainBarrierBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .noLootTable());
    }
}
