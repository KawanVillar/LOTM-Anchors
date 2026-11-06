package com.kawan.lotmanchors.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Screen.render in 1.21.1 renders the blurred background, not just widgets. */
public abstract class AnchorScreen extends Screen {
    protected AnchorScreen(Component title) { super(title); }

    @Override public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        renderContent(graphics, mouseX, mouseY, partialTick);
        for (var widget : renderables) widget.render(graphics, mouseX, mouseY, partialTick);
        renderOverlay(graphics, mouseX, mouseY);
    }

    protected abstract void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    protected void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {}
}
