# Phase 7c Redstone wall in-game checks

Run from the repository root on the Redstone wall branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/phase7c-redstone/redstone-1.txt
```

An iron 3x3 with a Redstone wall at the bottom middle of its front, a comparator in front of it, redstone dust and a lamp.

| Screenshot | Shows |
|---|---|
| `redstone-empty.png` | Empty chest: comparator off, lamp off |
| `redstone-whole-chest.png` | 5000 stone in the chest: comparator on, lamp lit |
| `redstone-settings-whole-chest.png` | The settings, opened by sneak-right-click with an empty hand: empty slot means whole chest, signal 3 |
| `redstone-settings-dirt.png` | After shift-clicking dirt: the wall follows dirt, which the chest does not hold, so signal 0 |
| `redstone-dirt-none.png` | Back in the world: comparator off, lamp off |
| `redstone-sneak-icon.png` | While sneaking, the wall shows its icon, like other functional walls |

Read back from the world: the comparator's powered state and the lamp's lit state follow each step.
