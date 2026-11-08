package com.kawan.lotmanchors.anchor;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.*;

/** Deterministic scan-count regression; no timing claims or running Minecraft required. */
public final class ServerSourceIndexTest {
    private static int checks;
    private static final class Source {
        final UUID id;
        UUID owner;
        boolean alive = true;
        Source(int id, UUID owner) { this.id = new UUID(0, id); this.owner = owner; }
    }
    public static void main(String[] args) throws Exception {
        UUID first = new UUID(1, 1), second = new UUID(1, 2);
        AtomicInteger scans = new AtomicInteger();
        List<Source> entities = new ArrayList<>();
        for (int i = 0; i < 20_000; i++) entities.add(new Source(i, i < 1000 ? new UUID(1, i % 20 + 1) : null));
        var cache = new ServerTickCache<String, OwnedSourceIndex<Source>>();
        java.util.function.Supplier<OwnedSourceIndex<Source>> build = () -> OwnedSourceIndex.build(entities,
                e -> e.id, e -> { scans.incrementAndGet(); return e.owner == null ? List.of() : List.of(e.owner, e.owner); });
        OwnedSourceIndex<Source> index = cache.get("overworld", 10, build);
        for (int player = 1; player <= 20; player++) {
            for (int consumer = 0; consumer < 2; consumer++) {
                var shared = cache.get("overworld", 10, build);
                check(shared == index && shared.candidates(new UUID(1, player)).size() == 50,
                        "history and sanity share one owner-indexed scan");
            }
        }
        check(scans.get() == 20_000, "20 players and two consumers enumerate 20,000 sources once, not 80 times");
        check(index.candidates(new UUID(9, 9)).isEmpty(), "unrelated owner gets no entities");
        Source transferred = index.candidates(first).getFirst();
        transferred.owner = second;
        check(index.candidates(first).stream().filter(e -> first.equals(e.owner) && e.alive).count() == 49,
                "live ownership revalidation immediately rejects old owner");
        cache.invalidate("overworld");
        index = cache.get("overworld", 10, build);
        check(index.candidates(second).size() == 51, "forced refresh discovers same-tick ownership transfer");
        transferred.alive = false;
        check(index.candidates(second).stream().filter(e -> second.equals(e.owner) && e.alive).count() == 50,
                "index does not cache liveness or contribution");
        int before = scans.get();
        cache.get("overworld", 11, build);
        check(scans.get() == before + 20_000, "new tick rebuilds membership");
        cache.get("overworld", 1, build);
        check(scans.get() == before + 40_000, "tick rollback cannot reuse stale membership");
        var nether = cache.get("nether", 1, () -> OwnedSourceIndex.build(List.<Source>of(), e -> e.id, e -> List.of(first)));
        check(nether.candidates(first).isEmpty(), "dimension scopes never share candidates");
        cache.invalidate("nether");
        check(cache.get("overworld", 1, build).candidates(first).size() == 49, "invalidating another dimension preserves this scope");
        var beforeClear = cache.get("overworld", 1, build);
        cache.clear();
        check(cache.get("overworld", 1, build) != beforeClear, "server shutdown clears retained references");
        var failed = new ServerTickCache<String, String>();
        try { failed.get("scope", 1, () -> { throw new IllegalStateException("test failure"); }); }
        catch (IllegalStateException expected) { }
        check(failed.get("scope", 1, () -> "recovered").equals("recovered"), "failed scan does not poison the cache");
        boolean immutable = false;
        try { index.candidates(first).clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
        check(immutable, "caller cannot mutate shared candidate lists");
        Source duplicate = new Source(80_000, first);
        var dedup = OwnedSourceIndex.build(List.of(duplicate, duplicate), e -> e.id, e -> List.of(first, first));
        check(dedup.candidates(first).size() == 1, "duplicate identities and links are indexed once");
        try (InputStream input = ServerSourceIndexTest.class.getResourceAsStream(
                "/com/kawan/lotmanchors/anchor/AnchorSourceService.class")) {
            if (input == null) throw new AssertionError("Missing source service");
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String method, String descriptor, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            if (name.equals("getAllLevels") || name.equals("getAllEntities") || name.equals("getChunk"))
                                throw new AssertionError("Source service must not enumerate other dimensions or request chunks");
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG);
        }
        check(true, "source collection contains no global level/entity scans or chunk requests");
        System.out.println("ServerSourceIndex: " + checks + " checks passed");
        System.out.println("Synthetic workload: 20,000 entity visits for 20 players x 2 consumers in one tick; previous structure: 1,600,000.");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
