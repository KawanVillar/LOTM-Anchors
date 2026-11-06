package com.kawan.lotmanchors.anchor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-owned consent state. Pending offers are intentionally not persisted. */
public final class FollowerBook {
    public enum Stage { OFFER, INVITATION }
    public record Request(UUID deity, UUID worshipper, Stage stage, long expiresAt) {}
    private final Map<UUID, UUID> patrons = new HashMap<>();
    private final Map<UUID, Integer> sequences = new HashMap<>();
    private final Map<UUID, Request> requests = new HashMap<>();
    public UUID patron(UUID worshipper) { return patrons.get(worshipper); }
    public long count(UUID deity) { return patrons.values().stream().filter(deity::equals).count(); }
    public Map<UUID, UUID> snapshot() { return Map.copyOf(patrons); }
    public int sequence(UUID follower) { return sequences.getOrDefault(follower, 10); }
    public boolean updateSequence(UUID follower, int sequence) {
        if (!patrons.containsKey(follower) || sequence < 0 || sequence > 10) return false;
        Integer previous = sequences.put(follower, sequence);
        return previous == null || previous != sequence;
    }
    public int[] countsBySequence(UUID deity) {
        int[] counts = new int[11];
        patrons.forEach((follower, patron) -> {
            if (deity.equals(patron)) counts[sequence(follower)]++;
        });
        return counts;
    }
    public static long weightedAnchors(int[] counts, java.util.function.IntToLongFunction value) {
        long total = 0;
        for (int sequence = 0; sequence <= 10; sequence++) {
            long contribution = (long) counts[sequence] * value.applyAsLong(sequence);
            total = Long.MAX_VALUE - total < contribution ? Long.MAX_VALUE : total + contribution;
        }
        return total;
    }
    public void restore(UUID worshipper, UUID deity) {
        if (worshipper != null && deity != null && !worshipper.equals(deity)) patrons.put(worshipper, deity);
    }
    public UUID offer(UUID deity, UUID worshipper, long now, long duration) {
        requests.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        if (deity == null || worshipper == null || deity.equals(worshipper)
                || patron(worshipper) != null || duration <= 0 || requests.size() >= 1024) return null;
        if (requests.values().stream().anyMatch(r -> r.deity().equals(deity) && r.worshipper().equals(worshipper))) return null;
        UUID token = UUID.randomUUID();
        requests.put(token, new Request(deity, worshipper, Stage.OFFER, now + duration));
        return token;
    }
    public Request request(UUID token, long now) {
        Request request = requests.get(token);
        if (request != null && request.expiresAt() <= now) { requests.remove(token); return null; }
        return request;
    }
    public UUID invite(UUID actor, UUID token, long now, long duration) {
        Request request = request(token, now);
        if (request == null || request.stage() != Stage.OFFER || !request.deity().equals(actor)
                || patron(request.worshipper()) != null || duration <= 0) return null;
        requests.remove(token);
        UUID invitation = UUID.randomUUID();
        requests.put(invitation, new Request(actor, request.worshipper(), Stage.INVITATION, now + duration));
        return invitation;
    }
    public boolean respond(UUID actor, UUID token, boolean accept, long now) {
        Request request = request(token, now);
        if (request == null || request.stage() != Stage.INVITATION || !request.worshipper().equals(actor)) return false;
        requests.remove(token);
        if (!accept) return true;
        if (patron(actor) != null) return false;
        patrons.put(actor, request.deity());
        clearRequests(actor);
        return true;
    }
    public boolean leave(UUID worshipper) {
        boolean removed = patrons.remove(worshipper) != null;
        sequences.remove(worshipper);
        clearRequests(worshipper);
        return removed;
    }
    public boolean remove(UUID deity, UUID worshipper) {
        if (!deity.equals(patron(worshipper))) return false;
        return leave(worshipper);
    }
    private void clearRequests(UUID worshipper) {
        requests.entrySet().removeIf(entry -> entry.getValue().worshipper().equals(worshipper));
    }
}
