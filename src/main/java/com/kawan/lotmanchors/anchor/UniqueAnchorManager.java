package com.kawan.lotmanchors.anchor;

import com.kawan.lotmanchors.config.AnchorConfig;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.VirtualPersonaComponent;

public final class UniqueAnchorManager {

    private UniqueAnchorManager() {

    }

    private static boolean canUseUniqueAnchorType(
            ServerPlayer player,
            String uniqueAnchorType
    ) {
        String pathway = BeyonderData.getPathway(player);

        if (pathway == null) {
            return false;
        }

        return switch (uniqueAnchorType) {
            case "error_avatar" ->
                    "error".equalsIgnoreCase(pathway);

            case "marionette" ->
                    "fool".equalsIgnoreCase(pathway);

            case "persona" ->
                    "visionary".equalsIgnoreCase(pathway);

            default ->
                    false;
        };
    }

    /**
     * Counts Error Avatars owned by the player, grouped by sequence.
     *
     * Index:
     * 0 = unused
     * 1 = Sequence 1
     * 2 = Sequence 2
     * 3 = Sequence 3
     * 4 = Sequence 4
     * 5 = Sequence 5
     * 6 = Sequence 6
     */
    public static int[] countErrorAvatarsBySequence(ServerPlayer player) {

        if (!canUseUniqueAnchorType(player, "error_avatar")) {
            return new int[7];
        }

        int[] counts = new int[7];

        for (ServerLevel level : player.getServer().getAllLevels()) {

            for (Entity entity : level.getAllEntities()) {

                if (!(entity instanceof AvatarEntity avatar)) {
                    continue;
                }

                if (avatar.getOriginalOwner() == null) {
                    continue;
                }

                if (!avatar.getOriginalOwner().equals(player.getUUID())) {
                    continue;
                }

                if (!"error".equalsIgnoreCase(avatar.getPathway())) {
                    continue;
                }

                int sequence = avatar.getSequence();

                if (sequence >= 1 && sequence <= 6) {
                    counts[sequence]++;
                }
            }
        }

        return counts;
    }

    /**
     * Calculates the total Unique Anchors generated
     * by the player's Error Avatars.
     */
    public static long getErrorAvatarUniqueAnchors(ServerPlayer player) {

        int[] counts = countErrorAvatarsBySequence(player);

        long total = 0L;

        total += (long) counts[1]
                * AnchorConfig.errorAvatarSequence1Anchors();

        total += (long) counts[2]
                * AnchorConfig.errorAvatarSequence2Anchors();

        total += (long) counts[3]
                * AnchorConfig.errorAvatarSequence3Anchors();

        total += (long) counts[4]
                * AnchorConfig.errorAvatarSequence4Anchors();

        total += (long) counts[5]
                * AnchorConfig.errorAvatarSequence5Anchors();

        total += (long) counts[6]
                * AnchorConfig.errorAvatarSequence6Anchors();

        return total;
    }

    /**
     * Temporary compatibility/debug method.
     */
    public static int countErrorAvatars(ServerPlayer player) {

        int[] counts = countErrorAvatarsBySequence(player);

        return counts[1]
                + counts[2]
                + counts[3]
                + counts[4]
                + counts[5]
                + counts[6];
    }

    // =========================================================
    // MARIONETTES
    // =========================================================

    /**
     * Counts the player's Marionettes by their own Beyonder Sequence.
     *
     * Index:
     * 0 = unused
     * 1 = Sequence 1
     * 2 = Sequence 2
     * ...
     * 9 = Sequence 9
     * 10 = Non-Beyonder
     *
     * A Marionette is counted only when:
     *
     * 1. The entity has the Marionette attachment.
     * 2. The entity is actually a Marionette.
     * 3. The controller UUID belongs to the player.
     *
     * The Sequence belongs to the Marionette itself.
     * The controller's Sequence is NOT inherited.
     */
    public static int[] countMarionettesBySequence(ServerPlayer player) {

        if (!canUseUniqueAnchorType(player, "marionette")) {
            return new int[11];
        }

        int[] counts = new int[11];

        for (ServerLevel level : player.getServer().getAllLevels()) {

            for (Entity entity : level.getAllEntities()) {

                if (!(entity instanceof LivingEntity livingEntity)) {
                    continue;
                }

                MarionetteComponent component =
                        livingEntity.getData(
                                ModAttachments.MARIONETTE_COMPONENT
                        );

                if (!component.isMarionette()) {
                    continue;
                }

                String controllerUUID =
                        component.getControllerUUID();

                if (controllerUUID == null) {
                    continue;
                }

                if (!controllerUUID.equals(player.getStringUUID())) {
                    continue;
                }

                int sequence =
                        BeyonderData.getSequence(livingEntity);

                if (sequence >= 1 && sequence <= 9) {

                    counts[sequence]++;

                } else if (sequence == 10) {

                    counts[10]++;
                }
            }
        }

        return counts;
    }


    // Calculates the total Unique Anchors generated by the player's Marionettes.

    public static long getMarionetteUniqueAnchors(ServerPlayer player) {

        int[] counts = countMarionettesBySequence(player);

        long total = 0L;

        total += (long) counts[1]
                * AnchorConfig.marionetteSequence1Anchors();

        total += (long) counts[2]
                * AnchorConfig.marionetteSequence2Anchors();

        total += (long) counts[3]
                * AnchorConfig.marionetteSequence3Anchors();

        total += (long) counts[4]
                * AnchorConfig.marionetteSequence4Anchors();

        total += (long) counts[5]
                * AnchorConfig.marionetteSequence5Anchors();

        total += (long) counts[6]
                * AnchorConfig.marionetteSequence6Anchors();

        total += (long) counts[7]
                * AnchorConfig.marionetteSequence7Anchors();

        total += (long) counts[8]
                * AnchorConfig.marionetteSequence8Anchors();

        total += (long) counts[9]
                * AnchorConfig.marionetteSequence9Anchors();

        total += (long) counts[10]
                * AnchorConfig.marionetteNonBeyonderAnchors();

        return total;
    }


    // Temporary compatibility/debug method.

    public static int countMarionettes(ServerPlayer player) {

        int[] counts = countMarionettesBySequence(player);

        int total = 0;

        for (int sequence = 1; sequence <= 10; sequence++) {
            total += counts[sequence];
        }

        return total;
    }

    public static int[] countPersonasBySequence(ServerPlayer player) {

        if (!canUseUniqueAnchorType(player, "persona")) {
            return new int[6];
        }

        int[] counts = new int[6];

        String ownerName = player.getName().getString();

        /*
         * Personas placed on other players.
         *
         * getAffectedBy(sequence) returns Personas with
         * Persona Sequence >= the requested sequence.
         * Therefore we reconstruct the exact Sequence counts
         * using subtraction.
         */
        int[] atLeast = new int[6];

        for (ServerPlayer target : player.getServer().getPlayerList().getPlayers()) {

            VirtualPersonaComponent component =
                    target.getData(ModAttachments.VIRTUAL_PERSONAS.get());

            for (int sequence = 1; sequence <= 5; sequence++) {

                for (String owner : component.getAffectedBy(sequence)) {

                    if (ownerName.equals(owner)) {
                        atLeast[sequence]++;
                    }
                }
            }
        }

        counts[1] = atLeast[1] - atLeast[2];
        counts[2] = atLeast[2] - atLeast[3];
        counts[3] = atLeast[3] - atLeast[4];
        counts[4] = atLeast[4] - atLeast[5];
        counts[5] = atLeast[5];

        /*
         * Personas converted into Avatars.
         *
         * VirtualPersonaComponent only stores the UUID of the Avatar,
         * so the original Persona Sequence is recovered from:
         *
         * Avatar Sequence = Persona Sequence + 2
         *
         * Therefore:
         * Avatar Seq 3 -> Persona Seq 1
         * Avatar Seq 4 -> Persona Seq 2
         * Avatar Seq 5 -> Persona Seq 3
         * Avatar Seq 6 -> Persona Seq 4
         * Avatar Seq 7 -> Persona Seq 5
         */
        VirtualPersonaComponent ownComponent =
                player.getData(ModAttachments.VIRTUAL_PERSONAS.get());

        for (java.util.UUID avatarUUID : ownComponent.getAvatars()) {

            for (ServerLevel level : player.getServer().getAllLevels()) {

                Entity entity = level.getEntity(avatarUUID);

                if (!(entity instanceof AvatarEntity avatar)) {
                    continue;
                }

                if (avatar.getOriginalOwner() == null) {
                    continue;
                }

                if (!avatar.getOriginalOwner().equals(player.getUUID())) {
                    continue;
                }

                int avatarSequence = avatar.getSequence();

                if (avatarSequence >= 1 && avatarSequence <= 5) {
                    counts[avatarSequence]++;
                }

                break;
            }
        }

        return counts;
    }

    public static long getPersonaUniqueAnchors(ServerPlayer player) {
        int[] counts = countPersonasBySequence(player);

        long total = 0L;

        total += (long) counts[1] * AnchorConfig.personaSequence1Anchors();
        total += (long) counts[2] * AnchorConfig.personaSequence2Anchors();
        total += (long) counts[3] * AnchorConfig.personaSequence3Anchors();
        total += (long) counts[4] * AnchorConfig.personaSequence4Anchors();
        total += (long) counts[5] * AnchorConfig.personaSequence5Anchors();

        return total;
    }

    public static int countPersonas(ServerPlayer player) {
        int[] counts = countPersonasBySequence(player);

        int total = 0;

        for (int sequence = 1; sequence <= 5; sequence++) {
            total += counts[sequence];
        }

        return total;
    }
}