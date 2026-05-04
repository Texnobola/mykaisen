package com.my.kaisen.registry;
 
import com.my.kaisen.MyKaisen;
import com.my.kaisen.block.DomainBarrierBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
 
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MyKaisen.MODID);
 
    public static final DeferredHolder<Block, DomainBarrierBlock> DOMAIN_BARRIER = BLOCKS.register("domain_barrier", DomainBarrierBlock::new);
    public static final DeferredHolder<Block, DomainBarrierBlock> DOMAIN_FLOOR = BLOCKS.register("domain_floor", DomainBarrierBlock::new);
 
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
