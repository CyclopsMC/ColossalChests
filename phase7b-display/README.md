# Phase 7b Display wall in-game checks

Run from the repository root on the Display wall branch, on NeoForge, in a fresh world, in this order:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase7b-display/display-1-face.txt
clientdevbridge batch --continue-on-error <path-to>/phase7b-display/display-2-clicks.txt
```

`display-1-face.txt` builds an iron 3x3 with Compression, Lock and Void, holding 300 iron blocks in a locked, voiding slot. Right-clicking the Display wall with 10 iron ingots makes it show iron ingots and inserts them.

| Screenshot | Shows |
|---|---|
| `display-all-toggles.png` | The Display wall with all visual settings on: iron ingot, 2.71K (300 blocks and 10 ingots counted as ingots), fill bar, and the Compression, Void and Lock indicators |
| `display-no-counts.png` | Counts turned off |
| `display-no-fill.png` | Fill levels turned off |
| `display-no-indicators.png` | Upgrade indicators turned off |
| `display-toggles-strip.png` | The four states side by side, zoomed |

`display-2-clicks.txt` checks clicks with real input, read back from the inventory:

| Action | Result |
|---|---|
| Left-click, empty hand | 64 iron ingots |
| Sneak-left-click, empty hand | 1 iron ingot |
| Holding attack 60 ticks, empty hand | 64 iron ingots once, the wall does not break |
| Holding attack with an iron pickaxe | The wall breaks |

`break` with an empty hand reports a failure, which is the expected outcome, hence `--continue-on-error`. `hold-key ATTACK` does not register a click like a mouse press does, so clicks are held for 8 ticks, past vanilla's 5 tick repeat delay in creative; that also checks that a held click takes only once.
