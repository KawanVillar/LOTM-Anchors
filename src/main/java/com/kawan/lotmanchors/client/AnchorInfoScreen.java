package com.kawan.lotmanchors.client;

import com.kawan.lotmanchors.network.AnchorNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AnchorInfoScreen extends Screen {

    private final AnchorNetwork.OpenAnchorInfoPayload data;

    public AnchorInfoScreen(
            AnchorNetwork.OpenAnchorInfoPayload data) {

        super(Component.literal("LOTM Anchors"));

        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        this.renderBackground(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int boxWidth = 320;
        int boxHeight = 250;

        int left = centerX - boxWidth / 2;
        int top = centerY - boxHeight / 2;

        guiGraphics.fill(
                left,
                top,
                left + boxWidth,
                top + boxHeight,
                0xDD101018
        );

        guiGraphics.drawCenteredString(
                this.font,
                "LOTM ANCHORS",
                centerX,
                top + 15,
                0xD8B4FE
        );

        guiGraphics.drawCenteredString(
                this.font,
                data.playerName(),
                centerX,
                top + 32,
                0xFFFFFF
        );

        int x = left + 20;
        int y = top + 55;

        guiGraphics.drawString(
                this.font,
                "Pathway: " + data.pathway(),
                x,
                y,
                0xFFFFFF
        );

        String sequenceText =
                "Sequence: "
                        + data.sequence();

        if (data.sequenceName() != null
                && !data.sequenceName().isBlank()) {

            sequenceText +=
                    " - " + data.sequenceName();
        }

        guiGraphics.drawString(
                this.font,
                sequenceText,
                x,
                y + 18,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Anchors: " + data.anchors(),
                x,
                y + 42,
                0x55FF55
        );

        String nextAdvance =
                data.sequence() <= 0
                        ? "None"
                        : "Sequence "
                        + (data.sequence() - 1);

        guiGraphics.drawString(
                this.font,
                "Next advance: " + nextAdvance,
                x,
                y + 65,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Required: " + data.required(),
                x,
                y + 83,
                0xFFFF55
        );

        guiGraphics.drawString(
                this.font,
                "Missing: " + data.missing(),
                x,
                y + 101,
                0xFFFF55
        );

        guiGraphics.drawString(
                this.font,
                "Additional risk: "
                        + Math.round(
                        data.risk() * 100.0D
                )
                        + "%",
                x,
                y + 119,
                0xFF5555
        );

        guiGraphics.drawString(
                this.font,
                "Lifetime earned: "
                        + data.lifetimeEarned(),
                x,
                y + 143,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Lifetime lost: "
                        + data.lifetimeLost(),
                x,
                y + 161,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                "Press ESC to close",
                centerX,
                top + boxHeight - 20,
                0xAAAAAA
        );
    }
}