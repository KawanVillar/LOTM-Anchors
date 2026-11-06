package com.kawan.lotmanchors.client;

import com.kawan.lotmanchors.network.AnchorNetwork;
import com.kawan.lotmanchors.network.AnchorSourcesNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;

public final class AnchorInfoScreen extends AnchorScreen {
    private record Line(String text, int color) {}
    private final AnchorNetwork.OpenAnchorInfoPayload data;
    private AnchorPanelLayout layout;
    private double scroll;
    private String hoveredText;

    public AnchorInfoScreen(AnchorNetwork.OpenAnchorInfoPayload data) {
        super(Component.literal("LOTM Anchors"));
        this.data = data;
    }

    @Override protected void init() {
        layout = AnchorPanelLayout.fit(width, height, 640, 360);
        addRenderableWidget(Button.builder(Component.literal("Sources & faith"),
                button -> PacketDistributor.sendToServer(new AnchorSourcesNetwork.Request(data.target(), AnchorSourcesNetwork.NONE)))
                .bounds(layout.left() + layout.width() - 122, layout.top() + 8, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(layout.left() + layout.width() - 82, layout.top() + layout.height() - 28, 70, 20).build());
        clampScroll();
    }

    private List<Line> summary() {
        return List.of(
                new Line("Pathway: " + data.pathway(), 0xFFFFFF),
                new Line("Sequence: " + data.sequence() + (data.sequenceName() == null || data.sequenceName().isBlank()
                        ? "" : " - " + data.sequenceName()), 0xFFFFFF),
                new Line("Normal anchors: " + number(data.anchors()), 0x86DEA2),
                new Line("Next advance: " + (data.sequence() <= 0 ? "None" : "Sequence " + (data.sequence() - 1)), 0xFFFFFF),
                new Line("Next Sequence anchors required: " + number(data.required()), 0xFFD166),
                new Line("Next Sequence anchors missing: " + number(data.missing()), 0xFFD166),
                new Line("Additional risk: " + Math.round(data.risk() * 100.0) + "%", 0xFF8585),
                new Line("Next sanity ceiling (current native maximum): " + (data.sequence() <= 0 ? "No further advance"
                        : data.nextSanityCeiling() < 0 ? "Not applied" : Math.round(data.nextSanityCeiling() * 100) + "%"), 0xFFD166),
                new Line("Current stability: " + number(data.totalForStability()) + "/" + number(data.currentSequenceRequired()), 0x86DEA2),
                new Line("Current anchor deficit: " + number(Math.max(0, data.currentSequenceRequired() - data.totalForStability())), 0xFFD166),
                new Line("Sanity: " + Math.round(data.sanity() * 100) + "%", 0xFFFFFF),
                new Line("Current stability requirement: " + number(data.currentSequenceRequired()), 0xAAA3B8),
                new Line("Anchor sanity ceiling: " + (data.sanityState().equals("Disabled") || data.sanityState().equals("Not applicable")
                        || data.sanityState().equals("No pressure configured") ? "Not applied" : Math.round(data.sanityCeiling() * 100) + "%"), 0xFFD166),
                new Line("Anchor pressure: " + data.sanityState(), 0xAAA3B8),
                new Line("Lifetime earned: " + number(data.lifetimeEarned()), 0xAAA3B8),
                new Line("Lifetime lost: " + number(data.lifetimeLost()), 0xAAA3B8));
    }

    private List<Line> unique() {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line("Unique anchors", 0xD8B4FE));
        if (data.uniqueAnchorGroups().isEmpty()) lines.add(new Line("No unique sources currently active.", 0xAAA3B8));
        for (var group : data.uniqueAnchorGroups()) {
            lines.add(new Line(group.name(), 0xFFFFFF));
            for (var entry : group.sequences().stream().sorted(java.util.Comparator.comparingInt(
                    entry -> com.kawan.lotmanchors.anchor.AnchorSourceRow.sequenceRank(entry.sequence()))).toList()) {
                lines.add(new Line("  " + (entry.sequence() == 10 ? "Non-Beyonder" : "Sequence " + entry.sequence())
                        + ": " + entry.count(), 0xAAA3B8));
            }
            lines.add(new Line("  Total: " + number(group.totalValue()), 0x86DEA2));
            lines.add(new Line("", 0xFFFFFF));
        }
        return lines;
    }

    private boolean twoColumns() { return layout.width() >= 480; }
    private int viewportHeight() { return layout.height() - 104; }
    private int contentHeight() {
        return (twoColumns() ? Math.max(summary().size(), unique().size()) : summary().size() + 1 + unique().size()) * 16;
    }
    private void clampScroll() {
        scroll = Math.max(0, Math.min(layout.scrollLimit(contentHeight(), viewportHeight()), scroll));
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= layout.left() && x <= layout.left() + layout.width()
                && y >= layout.top() + 44 && y < layout.top() + layout.height() - 58) {
            scroll -= vertical * 16;
            clampScroll();
            return true;
        }
        return false;
    }

    @Override protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = layout.left(), top = layout.top(), w = layout.width(), h = layout.height();
        graphics.fill(left, top, left + w, top + h, 0xF0181522);
        graphics.fill(left, top, left + w, top + 2, 0xFFAE82D1);
        graphics.drawString(font, "LOTM ANCHORS", left + 12, top + 10, 0xD8B4FE);
        graphics.drawString(font, font.plainSubstrByWidth(data.playerName(), w - 24), left + 12, top + 29, 0xAAA3B8);
        graphics.fill(left + 10, top + 40, left + w - 10, top + 41, 0xFF3B304B);
        hoveredText = null;
        clampScroll();
        if (twoColumns()) {
            int colWidth = (w - 36) / 2;
            drawLines(graphics, summary(), left + 12, top + 46, colWidth, mouseX, mouseY);
            drawLines(graphics, unique(), left + 24 + colWidth, top + 46, colWidth, mouseX, mouseY);
        } else {
            List<Line> all = new ArrayList<>(summary());
            all.add(new Line("", 0xFFFFFF));
            all.addAll(unique());
            drawLines(graphics, all, left + 12, top + 46, w - 24, mouseX, mouseY);
        }
        graphics.fill(left + 10, top + h - 56, left + w - 10, top + h - 55, 0xFF3B304B);
        String total = "Total stability: " + number(data.totalForStability());
        graphics.drawString(font, font.plainSubstrByWidth(total, w - 24), left + 12, top + h - 45, 0xD8B4FE);
        String hint = contentHeight() > viewportHeight() ? "Scroll for more • ESC to close" : "ESC to close";
        graphics.drawString(font, font.plainSubstrByWidth(hint, w - 108), left + 12, top + h - 20, 0xAAA3B8);
    }

    private void drawLines(GuiGraphics graphics, List<Line> lines, int x, int bodyTop, int w, int mouseX, int mouseY) {
        int bottom = layout.top() + layout.height() - 58;
        graphics.enableScissor(x, bodyTop, x + w, bottom);
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            int y = bodyTop + i * 16 - (int) scroll;
            if (y + 9 < bodyTop || y >= bottom) continue;
            graphics.drawString(font, font.plainSubstrByWidth(line.text(), w), x, y, line.color());
            if (mouseX >= x && mouseX < x + w && mouseY >= Math.max(bodyTop, y)
                    && mouseY < Math.min(bottom, y + 12) && font.width(line.text()) > w) hoveredText = line.text();
        }
        graphics.disableScissor();
    }

    @Override protected void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredText != null) graphics.renderTooltip(font,
                font.split(Component.literal(hoveredText), Math.min(300, layout.width() - 24)), mouseX, mouseY);
    }

    private static String number(long value) { return String.format(java.util.Locale.US, "%,d", value); }
}
