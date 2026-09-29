# LOTMCraft 1.13.0 API audit used by this addon

Source inspected: `lotmcraft-1.13.0(3).jar` supplied for this project.

## Direct APIs used

### BeyonderData
- `getPathway(LivingEntity)`
- `getSequence(LivingEntity)`

### Advancement
`de.jakob.lotm.events.custom.StartAdvanceSequencePathwayEvent`
- `getEntity()`
- `getSequence()`
- `getPathway()`
- `getFailureChance()`
- `setFailureChance(double)`
- `getDuration()`

The LOTMCraft `AdvancementUtil` posts this event and only afterwards performs its random failure check. Its own `scheduleFailure` path handles the existing failure consequence. The addon therefore only changes the probability.

### Honorific names
`de.jakob.lotm.events.HonorificNamesEventHandler`
- `answerState` is a public `LinkedList<Pair<UUID, UUID>>`.
- The inspected chat handler has `HIGHEST` priority.
- A completed valid prayer adds a pair containing the target UUID and praying player's UUID.

The addon listens at `LOWEST`, after LOTMCraft's handler, and consumes newly-added pairs to award the target player anchors. A per-worshipper/target cooldown prevents chat spam from generating unlimited anchors.

## Important design decision

No code attempts to replace LOTMCraft's sequence advancement, death, characteristic drop, or regression logic. The addon integrates at the event that the supplied JAR already exposes for modifying failure chance.
