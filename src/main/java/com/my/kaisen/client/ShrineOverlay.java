package com.my.kaisen.client;
 
import com.my.kaisen.MyKaisen;
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
 
import java.awt.Color;
import java.util.List;
 
@EventBusSubscriber(modid = MyKaisen.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ShrineOverlay {
 
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MyKaisen.MODID, "shrine_overlay"), (guiGraphics, partialTick) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.level == null) return;
 
            // Find an owned ShrineEntity
            ShrineEntity ownedShrine = null;
            List<ShrineEntity> shrines = mc.level.getEntitiesOfClass(ShrineEntity.class, player.getBoundingBox().inflate(300.0));
            for (ShrineEntity shrine : shrines) {
                if (shrine.getOwnerUUID() != null && shrine.getOwnerUUID().equals(player.getUUID())) {
                    ownedShrine = shrine;
                    break;
                }
            }
 
            if (ownedShrine != null) {
                renderShrineHud(guiGraphics, ownedShrine, mc);
            }
        });
    }
 
    private static void renderShrineHud(GuiGraphics guiGraphics, ShrineEntity shrine, Minecraft mc) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        ShrineEntity.DomainState state = shrine.getCurrentState();
 
        if (state == ShrineEntity.DomainState.FORMING) {
            // 1. REVERSE COUNTDOWN
            int remainingTicks = 100 - shrine.getFormingTicks();
            int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);
            
            String text = "DOMAIN EXPANSION IN: " + remainingSeconds;
            Component component = Component.literal(text).withStyle(net.minecraft.ChatFormatting.RED).withStyle(net.minecraft.ChatFormatting.BOLD);
            
            guiGraphics.pose().pushPose();
            float scale = 1.5f;
            guiGraphics.pose().translate(width / 2.0, 40, 0);
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.drawCenteredString(mc.font, component, 0, 0, 0xFFFFFF);
            guiGraphics.pose().popPose();
        } 
        else if (state == ShrineEntity.DomainState.ACTIVE && shrine.isOpen()) {
            // 2. DUST METER
            int dust = shrine.getDustLevel();
            float progress = dust / 1000.0f;
            
            int barWidth = 180;
            int barHeight = 8;
            int x = (width - barWidth) / 2;
            int y = height - 70; // Above hotbar
 
            // Background
            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
            
            // Progress Bar Color Shift
            int color;
            if (progress < 0.5f) {
                color = interpolateColor(0xFF555555, 0xFFFFAA00, progress * 2.0f);
            } else {
                color = interpolateColor(0xFFFFAA00, 0xFFFF4500, (progress - 0.5f) * 2.0f);
            }
            
            guiGraphics.fill(x, y, x + (int)(barWidth * progress), y + barHeight, color);
            
            String label = "EXPLOSIVE DUST: [" + (int)(progress * 100) + "]%";
            guiGraphics.drawCenteredString(mc.font, label, width / 2, y - 12, 0xFFAA00);
        }
    }
 
    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xff;
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = color1 & 0xff;
 
        int a2 = (color2 >> 24) & 0xff;
        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = color2 & 0xff;
 
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
 
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
