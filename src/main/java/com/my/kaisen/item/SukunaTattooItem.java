package com.my.kaisen.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class SukunaTattooItem extends Item implements ICurioItem {
    public SukunaTattooItem(Properties properties) {
        super(properties);
    }

    // Modern Curios API on NeoForge 1.21.1 uses capabilities, 
    // but the ICurioItem interface provides a default implementation for the capability.
}
