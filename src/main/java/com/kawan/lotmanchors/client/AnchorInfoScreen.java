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

        int boxWidth = 410;
        int boxHeight = 260;

        int left = centerX - boxWidth / 2;
        int top = centerY - boxHeight / 2;

        // =========================
        // MAIN BACKGROUND
        // =========================

        guiGraphics.fill(
                left,
                top,
                left + boxWidth,
                top + boxHeight,
                0xDD101018
        );

        // =========================
        // HEADER
        // =========================

        guiGraphics.drawCenteredString(
                this.font,
                "LOTM ANCHORS",
                centerX,
                top + 10,
                0xD8B4FE
        );

        guiGraphics.drawCenteredString(
                this.font,
                data.playerName(),
                centerX,
                top + 24,
                0xFFFFFF
        );

        // =========================
        // COLUMNS
        // =========================

        int leftColumn = left + 15;
        int rightColumn = left + 225;

        int y = top + 43;

        // =========================
        // LEFT COLUMN
        // =========================

        guiGraphics.drawString(
                this.font,
                "Pathway: " + data.pathway(),
                leftColumn,
                y,
                0xFFFFFF
        );

        String sequenceText =
                "Sequence: " + data.sequence();

        if (data.sequenceName() != null
                && !data.sequenceName().isBlank()) {

            sequenceText +=
                    " - " + data.sequenceName();
        }

        guiGraphics.drawString(
                this.font,
                sequenceText,
                leftColumn,
                y + 14,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Normal Anchors: "
                        + formatNumber(data.anchors()),
                leftColumn,
                y + 32,
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
                leftColumn,
                y + 48,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Required: "
                        + formatNumber(data.required()),
                leftColumn,
                y + 64,
                0xFFFF55
        );

        guiGraphics.drawString(
                this.font,
                "Missing: "
                        + formatNumber(data.missing()),
                leftColumn,
                y + 80,
                0xFFFF55
        );

        guiGraphics.drawString(
                this.font,
                "Additional risk: "
                        + Math.round(data.risk() * 100.0D)
                        + "%",
                leftColumn,
                y + 96,
                0xFF5555
        );

        guiGraphics.drawString(
                this.font,
                "Lifetime earned: "
                        + formatNumber(
                        data.lifetimeEarned()
                ),
                leftColumn,
                y + 114,
                0xFFFFFF
        );

        guiGraphics.drawString(
                this.font,
                "Lifetime lost: "
                        + formatNumber(
                        data.lifetimeLost()
                ),
                leftColumn,
                y + 130,
                0xFFFFFF
        );

        // =========================
        // RIGHT COLUMN
        // =========================

        if (!data.uniqueAnchorGroups().isEmpty()) {

            guiGraphics.drawString(
                    this.font,
                    "Unique Anchors",
                    rightColumn,
                    y,
                    0xD8B4FE
            );

            int uniqueY = y + 18;

            for (AnchorNetwork.UniqueAnchorGroup group :
                    data.uniqueAnchorGroups()) {

                guiGraphics.drawString(
                        this.font,
                        group.name(),
                        rightColumn,
                        uniqueY,
                        0xFFFFFF
                );

                uniqueY += 15;

                for (AnchorNetwork.UniqueAnchorSequenceEntry entry :
                        group.sequences()) {

                    guiGraphics.drawString(
                            this.font,
                            "Sequence "
                                    + entry.sequence()
                                    + ": "
                                    + entry.count(),
                            rightColumn + 8,
                            uniqueY,
                            0xCCCCCC
                    );

                    uniqueY += 14;
                }

                guiGraphics.drawString(
                        this.font,
                        "Total: "
                                + formatNumber(
                                group.totalValue()
                        ),
                        rightColumn + 8,
                        uniqueY,
                        0x55FF55
                );

                uniqueY += 22;
            }
        }

        // =========================
        // TOTAL FOR STABILITY
        // =========================

        guiGraphics.drawString(
                this.font,
                "Total for Stability: "
                        + formatNumber(
                        data.totalForStability()
                ),
                leftColumn,
                top + boxHeight - 38,
                0xD8B4FE
        );

        // =========================
        // FOOTER
        // =========================

        guiGraphics.drawCenteredString(
                this.font,
                "Press ESC to close",
                centerX,
                top + boxHeight - 16,
                0xAAAAAA
        );
    }

    private static String formatNumber(long value) {
        return String.format(
                java.util.Locale.US,
                "%,d",
                value
        );
    }
}