# Phase 4 in-game checks

Run from the repository root on the Phase 4 branch. `phase4-1-open` starts from a fresh world, and the scripts after it continue in that world. `phase4-6-feedback` starts from another fresh world, with `--particles`.

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
for f in phase4-1-open phase4-2-clicks phase4-3-search-sort phase4-4-layouts phase4-5-over-capacity; do
  clientdevbridge batch <path-to>/phase4/$f.txt
done
clientdevbridge stop && clientdevbridge start --loader neoforge --particles
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase4/phase4-6-feedback.txt
```

Note: `clientdevbridge batch` keeps `--button` from the previous line, so every `click` passes it explicitly.

| Screenshot | Shows |
|---|---|
| `phase4-gui-27.png` | A wooden 3x3 opened from a wall: 27 slots, abbreviated counts (1,234 stone over two 16-stack slots: 1.02K and 210) |
| `phase4-fabric-gui-27.png` | The same on Fabric |
| `phase4-tooltip.png` | The exact count and capacity in the tooltip |
| `phase4-moved.png` | After the click checks: shift and ctrl click moved all 100 ender pearls into the inventory |
| `phase4-search.png` | Server-side search for "log" |
| `phase4-sort-count.png` | Sorting by count, the active sort button in yellow |
| `phase4-gui-54.png`, `phase4-gui-81.png` | The 54 slot layout, and 81 slots in 18 columns so the GUI fits a 240 pixel high screen |
| `phase4-gui-81-settings.png` | The settings tab: display wall toggles, and Lock all / Clear locks disabled until the Lock upgrade |
| `phase4-settings-27.png` | The settings tab fits the grid area of the smallest chest |
| `phase4-over-capacity.png` | After lowering the depth: red over-capacity slots, their tooltip, the warning next to the inventory label, and the chat message on opening |
| `phase4-feedback-blocks.png` | Right-clicking a wall of a broken build: the action bar explains it, red particles mark the missing and wrong walls |

Click rules read back from the client (cursor, slot 0) on NeoForge and Fabric: left `[64,960]`, left with cursor `[0,1024]`, right `[32,992]`, right with cursor `[31,993]`; shift `[16 pearls, 84 left]`, ctrl `[100 pearls, 0 left]`.

Follow-up checks (`phase4-0-lid-compare.txt` in a fresh world, then `phase4-7-build.txt`):

| Screenshot | Shows |
|---|---|
| `lid-compare-closed.png`, `lid-compare-open-profile.png` | The lock at vanilla proportions next to vanilla chests, closed and open |
| `phase4-marker-on-lock.png` | The core marker drawn on the front of the closed lock while sneaking |

`phase4-7-build.txt` places a wall against an unformed wall (it is placed instead of showing the diagnosis), and opens a formed chest with a block in hand.

GUI rework (`phase4-8-gui-v2.txt`, fresh world). The earlier search, sort and settings screenshots show the first version.

| Screenshot | Shows |
|---|---|
| `phase4-v2-gui.png` | Creative-style search field, the settings tab on the right, and the slot count and items per slot instead of the inventory label |
| `phase4-v2-search.png` | Searching "log" dims the other slots; nothing moves |
| `phase4-v2-capacity-tooltip.png` | The items per slot tooltip |
| `phase4-v2-dragged.png` | Right-dragging 64 dirt over 5 slots puts 1 in each, left-dragging the remaining 59 over 3 slots puts 19 in each |
| `phase4-v2-settings.png` | The open settings tab |
