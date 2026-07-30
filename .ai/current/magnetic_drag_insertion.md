# Magnetic Rebuild Floating Drag Insertion

## Scope

Only the magnetic rebuild assembled-token interaction and adjacency presentation are changed. Practice, exam, wrong-book, favorites, history, AI, fragmentation settings, answer card, and persisted draft schema remain unchanged.

## Behavior

- Long press lifts one assembled token into a floating overlay.
- The original layout keeps a translucent placeholder so the FlowRow does not collapse during drag.
- Pointer position is compared with measured token bounds to calculate an insertion index across wrapped rows.
- A primary-color insertion marker previews the drop position.
- Dragging near the top or bottom of the window scrolls the existing page ScrollState.
- Drop emits the existing `SessionCommand.MagneticMoveToken(tokenId, targetIndex)` once; database progress is not written per pointer frame.
- Accessibility custom actions for move-before, move-after, and return-to-candidates remain available.

## Independent adjacency

`evaluateMagneticAdjacency` evaluates every adjacent pair separately. It returns both the correct pair count and the set of tokens participating in at least one correct pair. A wrong earlier pair cannot suppress a later correct pair.

## Tests

- `MagneticDragInsertionTest`: before/between/across-row/end insertion targets.
- `MagneticIndependentAdjacencyTest`: later correct segments remain connected after an earlier break.
