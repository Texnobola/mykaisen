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

    public static void playAnimation(AbstractClientPlayer player, String animName) {
        var animation = PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, animName));
        if (animation != null) {
            var animationData = PlayerAnimationAccess.getPlayerAssociatedData(player).get(ANIMATION_LAYER_ID);
            if (animationData instanceof ModifierLayer<?> rawLayer) {
                ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) rawLayer;
                layer.setAnimation(new KeyframeAnimationPlayer((dev.kosmx.playerAnim.core.data.KeyframeAnimation) animation));
            }
        }
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
                        if (animationData instanceof ModifierLayer<?> rawLayer) {
                            ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) rawLayer;
                            
                            dev.kosmx.playerAnim.api.layered.modifier.FirstPersonModifier fpModifier = new dev.kosmx.playerAnim.api.layered.modifier.FirstPersonModifier();
                            fpModifier.setCurrentFirstPersonConfig(dev.kosmx.playerAnim.api.layered.modifier.FirstPersonModifier.FirstPersonConfigEnum.ENABLE_BOTH_ARMS);
                            fpModifier.setCurrentFirstPersonMode(dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL);
                            
                            layer.addModifierLast(fpModifier); // Note: addModifierLast is the standard KosmX method for this, overriding addModifierBefore which usually takes 2 args. If it errors, we will fix.
                            
                            layer.setAnimation(new KeyframeAnimationPlayer((dev.kosmx.playerAnim.core.data.KeyframeAnimation) animation));
                        }
                    }
                }
            }
        });
    }
}
