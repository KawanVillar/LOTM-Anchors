package com.kawan.lotmanchors.client;
import com.kawan.lotmanchors.network.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public final class AnchorHistoryScreen extends AnchorScreen {
    private final AnchorHistoryNetwork.Snapshot data;
    private final AnchorSourcesScreen sources;
    private AnchorPanelLayout layout;
    private int scroll;
    public AnchorHistoryScreen(AnchorHistoryNetwork.Snapshot data, AnchorSourcesScreen sources) {
        super(Component.literal("Anchor history")); this.data = data; this.sources = sources;
    }
    private void backToSources() {
        // Local navigation must work while the integrated server is paused or rate-limited.
        minecraft.setScreen(sources);
    }
    @Override protected void init() {
        layout = AnchorPanelLayout.fit(width, height, 580, 340);
        addRenderableWidget(Button.builder(Component.literal("Back to sources"), b -> backToSources())
                .bounds(layout.left()+12, layout.top()+layout.height()-28, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(layout.left()+layout.width()-82, layout.top()+layout.height()-28, 70, 20).build());
    }
    private java.util.List<net.minecraft.util.FormattedCharSequence> lines() {
        var lines = new ArrayList<net.minecraft.util.FormattedCharSequence>();
        for (var entry : data.entries()) {
            String date = DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.US).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(entry.time()));
            lines.addAll(font.split(Component.literal(date + "  " + entry.message()), layout.width()-32));
            lines.add(Component.literal(" ").getVisualOrderText());
        }
        return lines;
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        scroll = Math.max(0, Math.min(Math.max(0, lines().size()*12-(layout.height()-90)), scroll-(int)(vertical*24))); return true;
    }
    @Override protected void renderContent(GuiGraphics g, int x, int y, float partialTick) {
        int left=layout.left(), top=layout.top(), w=layout.width(), h=layout.height();
        g.fill(left,top,left+w,top+h,0xF0181522); g.fill(left,top,left+w,top+2,0xFFAE82D1);
        g.drawString(font,"ANCHOR HISTORY",left+12,top+12,0xD8B4FE);
        g.drawString(font,"Latest 32 events - Scroll for more",left+12,top+28,0xAAA3B8);
        g.enableScissor(left+12,top+48,left+w-12,top+h-42);
        var lines=lines();
        if (lines.isEmpty()) lines=font.split(Component.literal("No events recorded yet."),w-32);
        for (int i=0;i<lines.size();i++) g.drawString(font,lines.get(i),left+16,top+48+i*12-scroll,0xDDD6E8);
        g.disableScissor();
    }
}
