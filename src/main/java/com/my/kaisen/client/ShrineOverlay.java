package com.my.kaisen.client;
 
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import java.util.List;
 
public class ShrineOverlay implements LayeredDraw.Layer {
 
    private static int blackFlashCombo = 0;
 
    public static void setBlackFlashCombo(int combo) {
        blackFlashCombo = combo;
    }
 
    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        ClientVfxHandler.renderBlackFlashOverlay(guiGraphics, width, height);
        renderAbsoluteCombo(guiGraphics, mc);
 
        // Domain Charge Countdown
        int chargeTicks = ClientEvents.getDomainChargeTicks();
        if (chargeTicks > 0 && chargeTicks < 100) {
            renderChargeCountdown(guiGraphics, chargeTicks, mc);
        }
 
        // Find an owned ShrineEntity within a 250-block radius
        ShrineEntity ownedShrine = null;
        List<ShrineEntity> shrines = mc.level.getEntitiesOfClass(ShrineEntity.class, player.getBoundingBox().inflate(250.0));
        for (ShrineEntity shrine : shrines) {
            if (shrine.getOwnerUUID() != null && shrine.getOwnerUUID().equals(player.getUUID())) {
                ownedShrine = shrine;
                break;
            }
        }
 
        if (ownedShrine != null) {
            renderShrineHud(guiGraphics, ownedShrine, mc);
        }
    }
 
    private void renderChargeCountdown(GuiGraphics guiGraphics, int ticks, Minecraft mc) {
        int width = mc.getWindow().getGuiScaledWidth();
        int remainingSeconds = (int) Math.ceil((100 - ticks) / 20.0);
        
        String text = "CONCENTRATING: " + remainingSeconds + "s";
        guiGraphics.pose().pushPose();
        float scale = 2.0f;
        guiGraphics.pose().translate(width / 2.0, 60, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.drawCenteredString(mc.font, Component.literal(text).withStyle(net.minecraft.ChatFormatting.YELLOW).withStyle(net.minecraft.ChatFormatting.BOLD), 0, 0, 0xFFFFFF);
        guiGraphics.pose().popPose();
    }
 
    private void renderAbsoluteCombo(GuiGraphics guiGraphics, Minecraft mc) {
        int absoluteCombo = ClientVfxHandler.getAbsoluteCombo();
        if (absoluteCombo <= 0) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Shake logic: more intense as combo grows
        float shakeIntensity = Math.min(10.0f, absoluteCombo / 2.0f);
        double offsetX = (mc.level.random.nextDouble() - 0.5) * shakeIntensity;
        double offsetY = (mc.level.random.nextDouble() - 0.5) * shakeIntensity;

        String text = absoluteCombo + " HIT COMBO!";
        guiGraphics.pose().pushPose();
        float scale = 3.0f + (absoluteCombo / 20.0f); // Scales up to 4.0 at 20 combo
        
        guiGraphics.pose().translate(width - 150 + offsetX, height / 2.0 + offsetY, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        
        // Color shifts to bright yellow/red as it gets higher
        int color = absoluteCombo >= 15 ? 0xFFFF00 : 0xFF3333;
        
        guiGraphics.drawCenteredString(mc.font, Component.literal(text).withStyle(net.minecraft.ChatFormatting.BOLD).withStyle(net.minecraft.ChatFormatting.ITALIC), 0, 0, color);
        guiGraphics.pose().popPose();
    }
 
    private void renderShrineHud(GuiGraphics guiGraphics, ShrineEntity shrine, Minecraft mc) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        ShrineEntity.DomainState state = shrine.getCurrentState();
 
        if (state == ShrineEntity.DomainState.FORMING) {
            int remainingTicks = 40 - shrine.getFormingTicks();
            int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);
            
            String text = "DOMAIN MANIFESTING: " + remainingSeconds;
            Component component = Component.literal(text).withStyle(net.minecraft.ChatFormatting.RED).withStyle(net.minecraft.ChatFormatting.BOLD);
            
            guiGraphics.pose().pushPose();
            float scale = 1.5f;
            guiGraphics.pose().translate(width / 2.0, 40, 0);
            guiGraphics.pose().scale(scale, scale, scale);
            guiGraphics.drawCenteredString(mc.font, component, 0, 0, 0xFFFFFF);
            guiGraphics.pose().popPose();
        } 
        else if (state == ShrineEntity.DomainState.ACTIVE && shrine.isOpen()) {
            int dust = shrine.getDustLevel();
            float progress = dust / 5000.0f;
            
            int barWidth = 240; // Wider
            int barHeight = 12; // Taller
            int x = (width - barWidth) / 2;
            int y = height - 100; // Moved up slightly

            // Outer Frame (Glowing Dark Red)
            guiGraphics.fill(x - 3, y - 3, x + barWidth + 3, y + barHeight + 3, 0xFF770000);
            guiGraphics.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0xFF000000);
            
            // Progress Bar (Vibrant Red with pulse effect?)
            int color = 0xFFFF0000;
            if (progress >= 1.0f) color = 0xFFFFD700; // Gold when ready
            
            guiGraphics.fill(x, y, x + (int)(barWidth * Math.min(1.0f, progress)), y + barHeight, color);
            
            // Background of bar (Darker red)
            guiGraphics.fill(x + (int)(barWidth * Math.min(1.0f, progress)), y, x + barWidth, y + barHeight, 0x55440000);

            String label = "MALEVOLENT DUST: " + (int)(Math.min(1.0f, progress) * 100) + "%";
            if (progress >= 1.0f) label = "§lDIVINE FLAME: READY";
            
            guiGraphics.pose().pushPose();
            float labelScale = 1.2f;
            guiGraphics.pose().translate(width / 2.0, y - 18, 0);
            guiGraphics.pose().scale(labelScale, labelScale, labelScale);
            guiGraphics.drawCenteredString(mc.font, label, 0, 0, progress >= 1.0f ? 0xFFFF00 : 0xFFFFFF);
            guiGraphics.pose().popPose();
        }
    }
}
