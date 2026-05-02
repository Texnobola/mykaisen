package com.my.kaisen.client;

import com.my.kaisen.network.SelectCharacterPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class CharacterSelectScreen extends Screen {
    public CharacterSelectScreen() {
        super(Component.literal("Character Selection"));
    }

    @Override
    protected void init() {
        int buttonWidth = 100;
        int buttonHeight = 20;
        this.addRenderableWidget(Button.builder(Component.literal("Sorcerer"), (button) -> {
            PacketDistributor.sendToServer(new SelectCharacterPayload(1));
            this.onClose();
        }).bounds(this.width / 2 - buttonWidth / 2, this.height / 2 - buttonHeight / 2, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw a semi-transparent background
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
