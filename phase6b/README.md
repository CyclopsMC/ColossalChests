# Phase 6b in-game checks

Run from the repository root on the Phase 6b branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase6b/phase6b-1-compression.txt
```

The iron 3x3 with Compression starts with 10 iron blocks, 5 ingots and 3 nuggets (858 nuggets worth) in one slot.

| Screenshot | Shows |
|---|---|
| `phase6b-compressed-slot.png` | Blocks, ingots and nuggets in one slot, shown as 10 blocks |
| `phase6b-form-picker-blocks.png` | The hover picker: the amount in each form, blocks chosen |
| `phase6b-form-picker-ingots.png` | After scrolling down once: ingots chosen |
| `phase6b-after-ingots.png` | After shift-clicking with ingots chosen |
| `phase6b-form-picker-nuggets.png` | After taking 3 blocks and scrolling to nuggets: 39 nuggets left |
| `phase6b-emptied.png` | The slot emptied by shift-clicking nuggets |

Extraction read back from the player inventory after each shift-click:

| Form | Inventory |
|---|---|
| Ingots | 64 iron ingots |
| Blocks | + 3 iron blocks |
| Nuggets | + 39 iron nuggets |

64 x 9 + 3 x 81 + 39 = 858, so nothing was lost or created. Shift can't be faked by clientdevbridge, so the shift-clicks are sent as their click packets. Hopper extraction is covered by game tests.
