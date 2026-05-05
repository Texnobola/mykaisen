package com.my.kaisen.client;
 
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.my.kaisen.MyKaisen;
import net.neoforged.api.distmarker.Dist;
 
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT)
public class VfxDebugCommand {
    private static String activeLoopEffect = null;
    private static int loopTicks = 0;
 
    public static void register(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        // Since we are in a client event subscriber, we might need a different way to register client commands.
        // In NeoForge, client commands are registered via RegisterClientCommandsEvent.
    }
 
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (activeLoopEffect != null) {
            loopTicks++;
            if (loopTicks >= 60) {
                loopTicks = 0;
                triggerEffect(activeLoopEffect);
            }
        }
    }
 
    private static void triggerEffect(String effect) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        if (effect.equals("fuga_nuke")) {
            ClientVfxHandler.spawnFugaNuke(player.level(), player.getX(), player.getY(), player.getZ());
        }
    }
 
    public static void setLoop(String effect) {
        activeLoopEffect = effect;
        loopTicks = 0;
    }
 
    public static void stopLoop() {
        activeLoopEffect = null;
    }
}
