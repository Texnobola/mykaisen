package com.my.kaisen.registry;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.item.CharacterChooserItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyKaisen.MODID);

    public static final DeferredItem<Item> CHARACTER_CHOOSER = ITEMS.register("character_chooser",
            () -> new CharacterChooserItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SUKUNA_TATTOO = ITEMS.register("sukuna_tattoo",
            () -> new com.my.kaisen.item.SukunaTattooItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
