package com.kawan.lotmanchors.client;

import com.kawan.lotmanchors.anchor.AnchorSourceRow;
import com.kawan.lotmanchors.anchor.PathwayAnchorRules;
import com.kawan.lotmanchors.network.AnchorSourcesNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;
import java.util.UUID;

public final class AnchorSourcesScreen extends AnchorScreen {
    private final AnchorSourcesNetwork.Snapshot data;
    private int category, page;
    private int left, top, panelWidth, panelHeight, pageSize;
    private AnchorSourceRow hovered;

    public AnchorSourcesScreen(AnchorSourcesNetwork.Snapshot data) { this(data, 0, 0); }
    public boolean showsOwner(UUID owner) { return data.owner().equals(owner); }
    public AnchorSourcesScreen updated(AnchorSourcesNetwork.Snapshot snapshot) {
        return new AnchorSourcesScreen(snapshot, category, page);
    }
    private AnchorSourcesScreen(AnchorSourcesNetwork.Snapshot data, int category, int page) {
        super(Component.literal("Anchor sources"));
        this.data = data; this.category = category; this.page = page;
    }

    @Override protected void init() {
        var layout = AnchorPanelLayout.fit(width, height, 640, 370);
        panelWidth = layout.width(); panelHeight = layout.height();
        left = layout.left(); top = layout.top(); pageSize = layout.sourcePageSize();
        rebuild();
    }

    private List<AnchorSourceRow> filtered() {
        return data.rows().stream().filter(row -> row.category() == category).sorted(AnchorSourceRow.DISPLAY_ORDER).toList();
    }
    private int pages() { return Math.max(1, (filtered().size() + pageSize - 1) / pageSize); }
    private Button button(String label, int x, int y, int w, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }

    private void rebuild() {
        clearWidgets();
        List<Integer> visible = PathwayAnchorRules.panelCategories(data.pathway());
        if (!visible.contains(category)) { category = visible.getFirst(); page = 0; }
        page = Math.min(page, pages() - 1);
        int tabWidth = (panelWidth - 20 - (visible.size() - 1) * 2) / visible.size();
        for (int i = 0; i < visible.size(); i++) {
            int tab = visible.get(i);
            var tabButton = button(AnchorSourceRow.label(tab) + " (" + data.count(tab) + ")", left + 10 + i * (tabWidth + 2), top + 77, tabWidth,
                    () -> { category = tab; page = 0; rebuild(); });
            tabButton.active = category != tab;
            tabButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(AnchorSourceRow.description(tab))));
        }
        List<AnchorSourceRow> rows = filtered();
        boolean own = minecraft.player != null && minecraft.player.getUUID().equals(data.owner());
        for (int i = 0; i < pageSize; i++) {
            int index = page * pageSize + i;
            if (index >= rows.size()) break;
            AnchorSourceRow row = rows.get(index);
            if (category == 0 && own) {
                button("Remove", left + panelWidth - 78, top + 124 + i * 32 + 4, 66, () -> {
                    minecraft.setScreen(new ConfirmScreen(confirmed -> {
                        minecraft.setScreen(this);
                        if (confirmed) request(row.id());
                    }, Component.literal("End the faith bond with " + row.name() + "?"),
                            Component.literal("This follower will stop contributing. A new bond requires another prayer and consent."),
                            Component.literal("End bond"), Component.literal("Cancel")));
                });
            }
        }
        int footer = top + panelHeight - 28;
        button("<", left + 10, footer, 22, () -> { page = Math.max(0, page - 1); rebuild(); }).active = page > 0;
        button(">", left + 36, footer, 22, () -> { page = Math.min(pages() - 1, page + 1); rebuild(); }).active = page < pages() - 1;
        int small = Math.max(1, Math.min(102, (panelWidth - 100) / 3));
        button("History", left + panelWidth - small * 3 - 18, footer, small, () ->
                PacketDistributor.sendToServer(new com.kawan.lotmanchors.network.AnchorHistoryNetwork.Request(data.owner())));
        button("Refresh", left + panelWidth - small * 2 - 14, footer, small, () -> request(AnchorSourcesNetwork.NONE));
        button("Close", left + panelWidth - small - 10, footer, small, this::onClose);
    }

    private void request(UUID remove) {
        PacketDistributor.sendToServer(new AnchorSourcesNetwork.Request(data.owner(), remove));
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (vertical != 0) { page = Math.max(0, Math.min(pages() - 1, page + (vertical < 0 ? 1 : -1))); rebuild(); }
        return true;
    }

    @Override protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF0181522);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFFAE82D1);
        graphics.drawString(font, font.plainSubstrByWidth("LOTM ANCHORS  /  SOURCES", panelWidth - 24), left + 12, top + 10, 0xD8B4FE);
        graphics.drawString(font, font.plainSubstrByWidth("Bonds of " + data.name(), panelWidth - 24),
                left + 12, top + 24, 0xAAA3B8);
        int cardWidth = (panelWidth - 28) / 3;
        String[] titles = {"Total stability", "Normal anchors", "Panel sources"};
        long[] values = {data.stability(), data.normal(), data.panelTotal()};
        for (int i = 0; i < 3; i++) {
            int x = left + 10 + i * (cardWidth + 4);
            graphics.fill(x, top + 38, x + cardWidth, top + 70, 0xFF252031);
            graphics.drawString(font, font.plainSubstrByWidth(titles[i], cardWidth - 10), x + 5, top + 42, 0xAAA3B8);
            graphics.drawString(font, font.plainSubstrByWidth(number(values[i]), cardWidth - 10), x + 5, top + 56, i == 0 ? 0xD8B4FE : 0x86DEA2);
        }
        graphics.drawString(font, font.plainSubstrByWidth(AnchorSourceRow.label(category) + " • " + number(data.total(category)) + " anchors", panelWidth - 85), left + 12, top + 106, 0xD8B4FE);
        String pageLabel = (page + 1) + "/" + pages();
        graphics.drawString(font, pageLabel, left + panelWidth - 12 - font.width(pageLabel), top + 106, 0xAAA3B8);
        List<AnchorSourceRow> rows = filtered();
        hovered = null;
        boolean removalVisible = category == 0 && minecraft.player != null
                && minecraft.player.getUUID().equals(data.owner());
        int actionWidth = removalVisible ? 78 : 12;
        int valueWidth = Math.min(90, panelWidth / 4);
        int textWidth = Math.max(1, panelWidth - actionWidth - valueWidth - 32);
        for (int i = 0; i < pageSize; i++) {
            int index = page * pageSize + i;
            if (index >= rows.size()) break;
            AnchorSourceRow row = rows.get(index);
            int y = top + 124 + i * 32;
            graphics.fill(left + 10, y, left + panelWidth - 10, y + 30, i % 2 == 0 ? 0xFF252031 : 0xFF201B2B);
            graphics.drawString(font, font.plainSubstrByWidth(row.name() + " • " + sequence(row.sequence()), textWidth), left + 16, y + 4, 0xFFFFFF);
            String detail = row.state() + " • " + (row.active() ? (row.value() > 0 ? "Contributing" : "Value configured to zero") : row.reason());
            graphics.drawString(font, font.plainSubstrByWidth(detail, textWidth), left + 16, y + 17, row.active() ? 0xAAA3B8 : 0xE6B869);
            String value = "+" + number(row.value());
            value = font.plainSubstrByWidth(value, valueWidth);
            graphics.drawString(font, value, left + panelWidth - actionWidth - 8 - font.width(value), y + 9, row.active() ? 0x86DEA2 : 0xE6B869);
            if (mouseX >= left + 10 && mouseX < left + panelWidth - 10 && mouseY >= y && mouseY < y + 30) hovered = row;
        }
        if (rows.isEmpty()) graphics.drawString(font, font.plainSubstrByWidth("No sources recorded in this category.", panelWidth - 32), left + 16, top + 130, 0xAAA3B8);
        String note = data.truncated() ? "List limited to 256 sources; totals include all."
                : category == 2 ? "Unloaded NPCs are not listed. Hover for details."
                : category == 0 ? "Offline: last known Sequence. Hover for details."
                : category == 1 ? "Offline Team members do not contribute. Hover for details."
                : category == 5 ? "Mind links: online hosts only. Registered unloaded avatars show no contribution."
                : "Only loaded linked entities are listed. Hover for owner, pathway and dimension.";
        graphics.drawString(font, font.plainSubstrByWidth(note, panelWidth - 24), left + 12, top + panelHeight - 43, 0xAAA3B8);
    }

    @Override protected void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hovered != null) {
            var tooltip = Component.literal(hovered.name() + " • " + sequence(hovered.sequence()) + "\n"
                    + hovered.state() + " • +" + number(hovered.value()) + " anchors\n" + hovered.reason() + "\nUUID: " + hovered.id());
            graphics.renderTooltip(font, font.split(tooltip, Math.min(300, panelWidth - 24)), mouseX, mouseY);
        }
    }

    private static String sequence(int seq) { return seq < 0 ? "Unknown Seq." : seq == 10 ? "Non-Beyonder" : "Seq. " + seq; }
    private static String number(long value) { return String.format(java.util.Locale.US, "%,d", value); }
}
