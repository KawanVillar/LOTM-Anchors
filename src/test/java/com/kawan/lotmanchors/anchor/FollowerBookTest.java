package com.kawan.lotmanchors.anchor;

import java.util.UUID;

public final class FollowerBookTest {
    private static int checks;
    private static final UUID DEITY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID FOLLOWER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static void main(String[] args) {
        FollowerBook book = new FollowerBook();
        check(book.offer(DEITY, DEITY, 0, 100) == null, "self prayer");
        check(book.offer(DEITY, null, 0, 100) == null, "null worshipper");
        check(book.offer(DEITY, FOLLOWER, 0, 0) == null, "invalid duration");
        UUID offer = book.offer(DEITY, FOLLOWER, 0, 100);
        check(offer != null && book.count(DEITY) == 0, "prayer alone grants nothing");
        check(book.offer(DEITY, FOLLOWER, 1, 100) == null, "duplicate prayer offer");
        check(!book.respond(FOLLOWER, offer, true, 1), "cannot accept before deity invites");
        check(book.invite(OTHER, offer, 1, 100) == null, "foreign deity cannot invite");
        UUID invitation = book.invite(DEITY, offer, 2, 100);
        check(invitation != null && book.count(DEITY) == 0, "invitation alone grants nothing");
        check(book.invite(DEITY, offer, 3, 100) == null, "offer cannot be replayed");
        check(!book.respond(OTHER, invitation, true, 3), "foreign player cannot accept");
        check(book.respond(FOLLOWER, invitation, false, 4), "decline allowed");
        check(book.count(DEITY) == 0 && book.patron(FOLLOWER) == null, "decline grants nothing");
        check(!book.respond(FOLLOWER, invitation, true, 5), "cannot accept declined invitation");
        offer = book.offer(DEITY, FOLLOWER, 10, 100);
        invitation = book.invite(DEITY, offer, 11, 100);
        check(book.respond(FOLLOWER, invitation, true, 12), "explicit consent");
        check(book.count(DEITY) == 1 && DEITY.equals(book.patron(FOLLOWER)), "one follower link");
        check(!book.respond(FOLLOWER, invitation, true, 13), "accept replay rejected");
        check(book.offer(DEITY, FOLLOWER, 13, 100) == null, "repeated prayers never multiply");
        check(book.offer(OTHER, FOLLOWER, 13, 100) == null, "no silent patron switch");
        check(!book.remove(OTHER, FOLLOWER), "foreign deity cannot revoke");
        FollowerBook restored = new FollowerBook();
        book.snapshot().forEach(restored::restore);
        check(restored.count(DEITY) == 1 && DEITY.equals(restored.patron(FOLLOWER)), "persisted link restored");
        check(restored.request(invitation, 14) == null, "requests are not restored");
        check(book.leave(FOLLOWER) && book.count(DEITY) == 0, "leave removes contribution");
        check(!book.leave(FOLLOWER), "leave idempotent");
        offer = book.offer(DEITY, FOLLOWER, 20, 10);
        check(book.invite(DEITY, offer, 30, 100) == null, "offer expires at deadline");
        offer = book.offer(DEITY, FOLLOWER, 40, 100);
        invitation = book.invite(DEITY, offer, 41, 10);
        check(!book.respond(FOLLOWER, invitation, true, 51), "invitation expires at deadline");
        check(book.count(DEITY) == 0, "expired invitation grants nothing");
        offer = book.offer(DEITY, FOLLOWER, 60, 100);
        invitation = book.invite(DEITY, offer, 61, 100);
        book.leave(FOLLOWER);
        check(!book.respond(FOLLOWER, invitation, true, 62), "leave cancels pending invitations");
        offer = book.offer(DEITY, FOLLOWER, 70, 100);
        UUID competing = book.offer(OTHER, FOLLOWER, 70, 100);
        invitation = book.invite(DEITY, offer, 71, 100);
        UUID competingInvitation = book.invite(OTHER, competing, 71, 100);
        check(book.respond(FOLLOWER, invitation, true, 72), "first consent wins");
        check(!book.respond(FOLLOWER, competingInvitation, true, 73), "competing stale consent rejected");
        check(book.count(DEITY) == 1 && book.count(OTHER) == 0, "only one active patron");
        check(book.remove(DEITY, FOLLOWER) && book.count(DEITY) == 0, "deity revokes own follower");
        restored.restore(OTHER, OTHER);
        check(restored.patron(OTHER) == null, "self links rejected on load");
        check(!book.updateSequence(FOLLOWER, 1), "no contribution before consent");
        book.restore(FOLLOWER, DEITY);
        check(book.sequence(FOLLOWER) == 10, "legacy follower defaults to non-beyonder");
        check(book.updateSequence(FOLLOWER, 5), "capture follower sequence");
        check(!book.updateSequence(FOLLOWER, 5), "unchanged sequence needs no save");
        check(book.countsBySequence(DEITY)[5] == 1, "counts by follower sequence");
        long[] values = {30_000, 10_000, 2500, 500, 100, 50, 35, 25, 15, 10, 5};
        check(FollowerBook.weightedAnchors(book.countsBySequence(DEITY), seq -> values[seq]) == 50,
                "sequence 5 weighting");
        book.updateSequence(FOLLOWER, 4);
        check(FollowerBook.weightedAnchors(book.countsBySequence(DEITY), seq -> values[seq]) == 100,
                "advancement increases contribution without new prayer");
        book.updateSequence(FOLLOWER, 5);
        check(FollowerBook.weightedAnchors(book.countsBySequence(DEITY), seq -> values[seq]) == 50,
                "regression reduces contribution");
        check(!book.updateSequence(FOLLOWER, -1) && !book.updateSequence(FOLLOWER, 11)
                && book.sequence(FOLLOWER) == 5, "invalid sequences cannot overwrite known sequence");
        book.updateSequence(FOLLOWER, 0);
        check(book.countsBySequence(DEITY)[0] == 1, "true deity follower is supported");
        book.restore(OTHER, DEITY);
        book.updateSequence(OTHER, 3);
        check(FollowerBook.weightedAnchors(book.countsBySequence(DEITY), seq -> values[seq]) == 30_500,
                "mixed sequence followers sum correctly");
        check(FollowerBook.weightedAnchors(book.countsBySequence(DEITY), seq -> values[seq] * 2) == 61_000,
                "configuration changes apply without recreating links");
        book.remove(DEITY, FOLLOWER);
        check(book.countsBySequence(DEITY)[0] == 0 && book.sequence(FOLLOWER) == 10, "removal clears cached sequence");
        System.out.println("FollowerBook: " + checks + " checks passed");
    }
    private static void check(boolean condition, String scenario) {
        if (!condition) throw new AssertionError(scenario);
        checks++;
    }
}
