package com.my.kaisen.client;
 
import com.my.kaisen.entity.ShrineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import java.util.List;
 
public class ShrineOverlay implements LayeredDraw.Layer {
 
    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
 
        // Find an owned ShrineEntity within a 200-block radius
        ShrineEntity ownedShrine = null;
        List<ShrineEntity> shrines = mc.level.getEntitiesOfClass(ShrineEntity.class, player.getBoundingBox().inflate(200.0));
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
 
    private void renderShrineHud(GuiGraphics guiGraphics, ShrineEntity shrine, Minecraft mc) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        ShrineEntity.DomainState state = shrine.getCurrentState();
 
        if (state == ShrineEntity.DomainState.FORMING) {
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
            int dust = shrine.getDustLevel();
            float progress = dust / 1000.0f;
            
            int barWidth = 180;
            int barHeight = 8;
            int x = (width - barWidth) / 2;
            int y = height - 70;
 
            // Background
            guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
            
            // Progress Bar (Bright Orange)
            int color = 0xFFFF4500; // Orange Red
            guiGraphics.fill(x, y, x + (int)(barWidth * progress), y + barHeight, color);
            
            String label = "EXPLOSIVE DUST: " + (int)(progress * 100) + "%";
            guiGraphics.drawCenteredString(mc.font, label, width / 2, y - 12, 0xFFAA00);
        }
    }
}
