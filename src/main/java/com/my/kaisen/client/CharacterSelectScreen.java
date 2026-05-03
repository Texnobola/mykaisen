package com.my.kaisen.client;

import com.my.kaisen.network.SelectCharacterPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class CharacterSelectScreen extends Screen {
    private static final net.minecraft.resources.ResourceLocation BOOK_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mykaisen", "textures/gui/cursed_book.png");
    private final int imageWidth = 192;
    private final int imageHeight = 192;

    public CharacterSelectScreen() {
        super(Component.literal("Character Selection"));
    }

    @Override
    protected void init() {
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;

        // "Vessel" button on the right-hand page
        this.addRenderableWidget(Button.builder(Component.literal("Vessel"), (button) -> {
            PacketDistributor.sendToServer(new SelectCharacterPayload(1));
            this.onClose();
        }).bounds(x + 115, y + 50, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;

        // Draw the book texture
        guiGraphics.blit(BOOK_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw title on the left page
        guiGraphics.drawString(this.font, "Choose Your Path", x + 20, y + 20, 0x000000, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
