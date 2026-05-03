package com.my.kaisen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MyKaisen.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MyKaisenClient {
    public MyKaisenClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup code
        top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(
                com.my.kaisen.registry.ModItems.SUKUNA_TATTOO.get(),
                com.my.kaisen.client.TattooCurioRenderer::new
        );
    }

    @SubscribeEvent
    static void onRegisterRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.my.kaisen.registry.ModEntities.DISMANTLE_PROJECTILE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
    }
}
