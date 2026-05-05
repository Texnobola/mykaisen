package com.my.kaisen.client;

import com.my.kaisen.network.CombatTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.DeltaTracker;

public class AbilityOverlay implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        
        LocalPlayer player = mc.player;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        
        int charId = player.getPersistentData().getInt("mykaisen_character");
        if (charId != 1) return;

        boolean isAwakened = player.getPersistentData().getBoolean("is_awakened");
        int globalCooldown = CombatTickHandler.abilityCooldowns.getOrDefault(player.getUUID(), 0);
        
        // Ability HUD Layout (Bottom Right)
        int x = width - 150;
        int y = height - 120;
        
        String[] names = isAwakened ? 
            new String[]{"Dismantle", "Divine Flame", "Rush", "Malevolent Shrine"} :
            new String[]{"Cursed Strikes", "Crushing Blow", "Divergent Fist", "Manji Kick"};
            
        String[] keys = new String[]{"Z", "X", "C", "V"};
        
        for (int i = 0; i < 4; i++) {
            int slotY = y + (i * 26);
            
            // Outer Glow/Frame
            guiGraphics.fill(x - 1, slotY - 1, x + 141, slotY + 23, 0xFF440000);
            
            // Background
            guiGraphics.fill(x, slotY, x + 140, slotY + 22, 0xDD111111);
            
            // Key Label
            guiGraphics.fill(x + 2, slotY + 2, x + 18, slotY + 20, 0xFF333333);
            guiGraphics.drawCenteredString(mc.font, keys[i], x + 10, slotY + 7, 0xFFFF00);
            
            // Name
            int textColor = globalCooldown > 0 ? 0x777777 : (isAwakened ? 0xFF5555 : 0xFFFFFF);
            guiGraphics.drawString(mc.font, names[i], x + 25, slotY + 7, textColor);
            
            // Cooldown Overlay
            if (globalCooldown > 0) {
                int cdWidth = (int)((globalCooldown / 40.0f) * 140);
                guiGraphics.fill(x, slotY + 20, x + Math.min(140, cdWidth), slotY + 22, 0xFFFF0000);
                
                if (globalCooldown > 20) {
                    String time = (globalCooldown / 20) + "s";
                    guiGraphics.drawString(mc.font, time, x + 120, slotY + 7, 0xFF0000);
                }
            }
        }
    }
}
