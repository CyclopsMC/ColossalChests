# Phase 7d Magnet wall in-game checks

Run from the repository root on the Magnet wall branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/phase7d-magnet/magnet-1.txt
```

An iron 3x3 with a Magnet wall at the top middle, default config (on, radius 8).

| Screenshot | Shows |
|---|---|
| `magnet-thrown.png` | 64 cobblestone just thrown with the drop key |
| `magnet-pulling.png` | It lands in front of the chest first: thrown items have a pickup delay, which the magnet respects |
| `magnet-sneak-icon.png` | While sneaking, the wall shows its icon, like other functional walls |

Read back from the world: after the delay the cobblestone was pulled in (slot 0: 64 cobblestone), an oak log broken 3 blocks away dropped into the chest (slot 1: 1 oak log), and no item entities were left.
