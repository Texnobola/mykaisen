package com.my.kaisen.client;

import com.my.kaisen.MyKaisen;
import com.my.kaisen.network.PlayAnimationPayload;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientAnimationHandler {

    // Unique ID for our animation layer
    public static final ResourceLocation ANIMATION_LAYER_ID = ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "animation_layer");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register the ModifierLayer to all abstract client players with a priority (e.g., 42)
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ANIMATION_LAYER_ID,
                42,
                player -> new ModifierLayer<IAnimation>()
        );
    }

    public static void handleAnimation(PlayAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                // Find the player entity by ID
                if (mc.level.getEntity(payload.playerId()) instanceof AbstractClientPlayer clientPlayer) {
                    
                    // Retrieve the requested animation from KosmX's registry
                    var animation = PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, payload.animationName()));
                    
                    if (animation != null) {
                        // Get the player's associated animation data layer we registered earlier
                        var animationData = PlayerAnimationAccess.getPlayerAssociatedData(clientPlayer).get(ANIMATION_LAYER_ID);
                        
                        // Cast and set the animation
                        if (animationData instanceof ModifierLayer<?> layer) {
                            ((ModifierLayer<IAnimation>) layer).setAnimation(new KeyframeAnimationPlayer((dev.kosmx.playerAnim.core.data.KeyframeAnimation) animation));
                        }
                    }
                }
            }
        });
    }
}
