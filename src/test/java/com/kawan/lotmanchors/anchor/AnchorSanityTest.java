package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.network.AnchorNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;
import java.util.UUID;

public final class AnchorSanityTest {
    private static int checks;
    public static void main(String[] args) {
        check(AnchorSanityRules.deficit(0, 0) == 0, "zero requirement disables pressure");
        check(AnchorSanityRules.deficit(1000, 1000) == 0, "sufficient anchors");
        check(AnchorSanityRules.deficit(2000, 1000) == 0, "excess anchors do not over-heal");
        check(AnchorSanityRules.deficit(0, 1000) == 1, "full deficit");
        check(AnchorSanityRules.deficit(500, 1000) == 0.5, "partial deficit");
        check(AnchorSanityRules.deficit(-5, 1000) == 1, "negative anchors clamped");
        near(AnchorSanityRules.ceiling(1, 0, 1000, 0.15), 0.85, "Sequence 3 ceiling");
        near(AnchorSanityRules.ceiling(1, 0, 3000, 0.30), 0.70, "Sequence 2 ceiling");
        near(AnchorSanityRules.ceiling(1, 0, 10000, 0.50), 0.50, "Sequence 1 ceiling");
        near(AnchorSanityRules.ceiling(1, 0, 30000, 0.65), 0.35, "Sequence 0 ceiling");
        near(AnchorSanityRules.ceiling(1, 15000, 30000, 0.65), 0.675, "half coverage proportional penalty");
        near(AnchorSanityRules.ceiling(1.2f, 0, 30000, 0.65), 0.42, "native effective maximum respected");
        near(AnchorSanityRules.ceiling(1, 0, 1000, 0), 1, "zero penalty");
        near(AnchorSanityRules.ceiling(1, 1000, 1000, 0.65), 1, "anchors release ceiling");
        near(AnchorSanityRules.reduceToward(1, 0.35f, 1, 0.005, 5), 0.975, "gradual loss per check");
        near(AnchorSanityRules.reduceToward(0.36f, 0.35f, 1, 0.005, 5), 0.35, "does not overshoot ceiling");
        near(AnchorSanityRules.reduceToward(0.2f, 0.35f, 1, 0.005, 5), 0.2, "does not heal unrelated sanity loss");
        near(AnchorSanityRules.reduceToward(0.5f, 1, 1, 0.005, 5), 0.5, "sufficient anchors do not force healing");
        near(AnchorSanityRules.reduceToward(1, 0.35f, 1, 0, 5), 1, "zero drop disables reduction");
        float repeated = 1;
        for (int i = 0; i < 1000; i++) repeated = AnchorSanityRules.reduceToward(repeated, 0.35f, 1, 0.005, 5);
        near(repeated, 0.35, "no infinite drain from repeated checks");
        var session = new AnchorSanityRules.Session();
        check(!session.ready(0, 3, 100, 2400), "login grace starts");
        check(!session.ready(20, 3, 100, 2400), "does not run every tick");
        check(!session.ready(100, 3, 100, 2400), "interval alone does not bypass grace");
        check(session.ready(2400, 3, 100, 2400), "grace expires");
        check(!session.ready(2400, 3, 100, 2400), "same tick cannot apply twice");
        check(!session.pressureReady(2400, true, 600), "additional deficit grace");
        check(!session.pressureReady(2900, true, 600), "temporary chunk interruption tolerated");
        check(session.pressureReady(3000, true, 600), "continuous deficit pressure begins");
        check(!session.pressureReady(3100, false, 600), "coverage resets deficit timer");
        check(!session.pressureReady(3200, true, 600), "new deficit receives fresh grace");
        check(!session.ready(3300, 2, 100, 2400), "advancement resets grace");
        check(!session.ready(3400, 3, 100, 2400), "regression resets grace");
        check(!session.ready(10, 3, 100, 2400), "server tick rollback resets session");
        var noGrace = new AnchorSanityRules.Session();
        check(noGrace.ready(0, 0, 100, 0) && noGrace.pressureReady(0, true, 0), "zero grace setting respected");
        var notices = new AnchorSanityRules.Notices();
        check(notices.update(3, false, 0).isEmpty(), "stable login is silent");
        check(notices.update(3, true, 3000).equals("deficit"), "warn before pressure");
        check(notices.update(3, true, 2500).isEmpty(), "no repeated deficit warnings");
        check(notices.update(3, true, 200).equals("ending"), "warn near grace end");
        check(notices.update(3, true, 100).isEmpty(), "no repeated countdown warnings");
        check(notices.update(3, true, 0).equals("active"), "warn when pressure starts");
        check(notices.update(3, true, 0).isEmpty(), "no repeated active warnings");
        check(notices.update(3, false, 0).equals("recovered"), "announce recovery");
        check(notices.update(3, false, 0).isEmpty(), "no repeated recovery warnings");
        check(notices.update(3, true, 0).equals("active"), "zero grace directly warns active");
        check(notices.update(2, true, 2400).equals("deficit"), "sequence change resets warnings");
        check(session.graceRemaining(20, 2400) == 2390, "remaining grace follows reset timer");
        var payload = new AnchorNetwork.OpenAnchorInfoPayload(new UUID(1, 1), "Test", "red_priest", 3, "War Bishop",
                500, 500, 0, 3000, 2500, 0.5, 0, 500,
                0.9f, 0.925f, 1000, "Deficit (grace may apply)", 0.75f, List.of());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            AnchorNetwork.OpenAnchorInfoPayload.STREAM_CODEC.encode(buf, payload);
            check(payload.equals(AnchorNetwork.OpenAnchorInfoPayload.STREAM_CODEC.decode(buf)), "sanity status packet round trip");
            check(buf.readableBytes() == 0, "all sanity packet fields consumed");
        } finally { buf.release(); }
        System.out.println("AnchorSanity: " + checks + " checks passed");
    }
    private static void near(double actual, double expected, String message) { check(Math.abs(actual - expected) < 0.00001, message); }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
