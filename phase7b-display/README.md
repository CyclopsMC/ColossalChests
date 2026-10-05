# Phase 7b Display wall in-game checks

Run from the repository root on the Display wall branch, on NeoForge, each script in a fresh world (`clientdevbridge world-reset`):

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase7b-display/display-1-face.txt
clientdevbridge batch --continue-on-error <path-to>/phase7b-display/display-2-clicks.txt
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase7b-display/display-3-look.txt
```

## Look (display-3-look.txt)

A wood and an iron 3x3, each with Display walls for cobblestone, iron ingots, oak logs and torches. Display walls are a recessed panel in a frame of the chest's material.

| Screenshot | Shows |
|---|---|
| `display-overview.png` | Both chests |
| `display-wood.png`, `display-iron.png` | Close-ups. Blocks show like an inventory icon (drawer style), flat items like in the inventory |
| `display-iron-item-frame-style.png` | With the client config `displayItemFrameStyle` on: blocks show front-on like an item frame, flat items are unchanged |
| `display-iron-night.png` | At night, items darken with the chest instead of glowing |

A Display wall at the top centre sits under the chest's latch, which hides part of it (kept as is).

## Per side options (display-1-face.txt)

An iron 3x3 with Compression, Lock and Void, holding 300 iron blocks in a locked, voiding slot. Right-clicking the Display wall with 10 iron ingots makes it show iron ingots and inserts them.

| Screenshot | Shows |
|---|---|
| `display-all-toggles.png` | All options of the front side on: 2.71K (300 blocks and 10 ingots counted as ingots), fill bar, and the Compression, Void and Lock indicators |
| `display-no-counts.png` | Counts off |
| `display-no-fill.png` | Fill levels off |
| `display-no-indicators.png` | Upgrade indicators off |
| `display-toggles.png` | The four states side by side (from before the plate had sides) |

## Clicks (display-2-clicks.txt)

Real input, read back from the inventory:

| Action | Result |
|---|---|
| Left-click, empty hand | 64 iron ingots |
| Sneak-left-click, empty hand | 1 iron ingot |
| Holding attack 60 ticks, empty hand | 64 iron ingots once, the wall does not break |
| Holding attack with an iron pickaxe | The wall breaks |

`break` with an empty hand reports a failure, which is the expected outcome, hence `--continue-on-error`. `hold-key ATTACK` does not register a click like a mouse press does, so clicks are held for 8 ticks, past vanilla's 5 tick repeat delay in creative; that also checks that a held click takes only once.

## Controls (display-4-controls.txt)

An iron 3x3 with a Display wall showing iron ingots. Run in a fresh world on NeoForge, also checked on Fabric. Real input, read back from the inventory:

| Action | Result |
|---|---|
| 5 quick sneak-left-clicks, 2 ticks apart (also 6 clicks 1 tick apart in survival) | 5 (6) iron ingots, the wall does not break |
| Holding sneak-attack for 40 (survival: 60) ticks | 1 iron ingot, not one per repeat |
| Sneak-right-click, empty hand | Nothing happens (PASS), no screen |
| Right-click with 64 ingots, then right away with the now empty hand | Both inventory stacks inserted |
| Right-click, empty hand, later | Opens the Display settings |

| Screenshot | Shows |
|---|---|
| `display-settings.png` | The settings: one ghost slot with the shown item (screenshot from Fabric, with the final spacing) |
| `display-settings-empty.png` | The tooltip of the empty slot (older 4px tighter spacing) |
| `display-plate-side.png`, `display-plate-corner.png` | The plate has sides down to the chest, so it no longer floats. Where two plates meet on a corner block, a half pixel notch remains along the edge |

In the settings, shift-clicking an inventory item shows it, clicking with an item shows it and keeps the cursor, clicking with an empty cursor clears it.

## Sides (display-5-faces.txt)

An iron 3x3 with a Display wall on its top left front corner. Real right-clicks with an item on each side set a different item per side.

| Screenshot | Shows |
|---|---|
| `display-faces.png` | Iron ingots on the front, cobblestone on the right side (the top, with torches, is out of view) |
| `display-faces-settings.png` | The settings: a column per side, named relative to the chest's front, with its item and a Shown/Hidden toggle |
| `display-faces-hidden-settings.png` | After clicking all three toggles: Top and Front hidden, the last side stays shown |
| `display-faces-front-hidden.png` | The hidden front shows the plain chest, the right side still shows cobblestone |

Right-clicking the hidden front with an empty hand opened the chest, like a plain wall.

## Options per side (display-6-options.txt)

The corner wall of display-5-faces.txt, with a Lock upgrade and its iron slot locked.

| Screenshot | Shows |
|---|---|
| `display-options-settings.png` | Each side has icon toggles for count, fill level and upgrades under its Shown/Hidden button; off is dimmed and struck through. Here: top upgrades, front count and right fill level off, with the tooltip of the front count |
| `display-options-world.png` | The front shows the fill bar and the Lock indicator but no count; the right side shows its count but no fill bar |
| `chest-settings-tab.png` | The chest's settings tab, which keeps only the lock and void buttons |
