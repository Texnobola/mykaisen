package com.my.kaisen;

import com.my.kaisen.registry.ModEntities;
import com.my.kaisen.registry.ModItems;
import com.my.kaisen.client.DismantleProjectileRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import top.theillusivec4.curios.api.CuriosApi;

@Mod(value = MyKaisen.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MyKaisenClient {
    public MyKaisenClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup code
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DISMANTLE_PROJECTILE.get(), DismantleProjectileRenderer::new);
    }
}
