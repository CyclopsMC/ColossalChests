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

`phase4-7-build.txt` places a wall against an unformed wall (it is placed instead of showing the diagnosis), and opens a formed chest with a block in hand.

GUI rework (`phase4-8-gui-v2.txt`, fresh world). The earlier search, sort and settings screenshots show the first version.

| Screenshot | Shows |
|---|---|
| `phase4-v2-gui.png` | Creative-style search field, the settings tab on the right, and the slot count and items per slot instead of the inventory label |
| `phase4-v2-search.png` | Searching "log" dims the other slots; nothing moves |
| `phase4-v2-capacity-tooltip.png` | The items per slot tooltip |
| `phase4-v2-dragged.png` | Right-dragging 64 dirt over 5 slots puts 1 in each, left-dragging the remaining 59 over 3 slots puts 19 in each |
| `phase4-v2-settings.png` | The open settings tab |

Header and info rework (`phase4-9-gui-v3.txt`, fresh world). Replaces the tab and the icon info of the previous rework.

| Screenshot | Shows |
|---|---|
| `phase4-v3-gui.png` | Settings button with the Cyclops config icon, search field aligned with the slot grid, "8/27 slots" and "1,024 per slot" |
| `phase4-v3-slots-tooltip.png`, `phase4-v3-capacity-tooltip.png` | Their tooltips |
| `phase4-v3-settings.png` | The settings view |
| `phase4-v3-over-capacity.png` | Over capacity: the slot info turns red and its tooltip explains why |

Search and seam checks (`phase4-10-search-v4.txt` opens the chest, then the search field is clicked at its `find search` position;):

| Screenshot | Shows |
|---|---|
| `phase4-v4-search-hit.png` | Search for "pearl": the match gets a yellow inner border, the rest is dimmed |
| `phase4-v4-search-selected.png` | Right-clicking the search field selects its text |
| `phase4-v4-search-replaced.png` | Typing "dia" then replaces it: two matches |

Search syntax and drag preview (`phase4-12-search-syntax.txt` and `phase4-13-drag-preview.txt`, each in a fresh world):

| Screenshot | Shows |
|---|---|
| `phase4-search-mod.png` | `@colossal`: only the chest walls |
| `phase4-search-tags.png` | `$logs\|$stone`: stone and logs by tag |
| `phase4-search-tooltip.png` | `#attack`: the sword, by its tooltip |
| `phase4-search-and.png` | `dia @minecraft`: both terms must match |
| `phase4-drag-preview-left.png` | Mid-drag of 37 stone over a full stone slot and 5 empty slots: 6 each, yellow count on the full slot, 7 left on the cursor |
| `phase4-drag-released.png` | After release: the same result |
| `phase4-search-border-behind.png` | `@minecraft`: the match border drawn behind items and counts |

Lid seam and slot info (`phase4-15-seam-repro.txt` and `phase4-14-full-slots.txt`, each in a fresh world):

| Screenshot | Shows |
|---|---|
| `seam-rows-front.png` | Diagnosis: texture rows painted red (14), green (18), blue (33) and yellow (42). The seam shows yellow over the hidden red, so rows 14 and 42 overlap |
| `seam-fixed-netherite-front.png`, `seam-fixed-angle.png`, `seam-fixed-iron-wood.png` | After copying row 42 into row 14 for every material |
| `phase4-slots-full-tooltip.png` | The slots tooltip with the number of full slots |
| `phase4-search-no-tooltip.png` | No tooltip on the search field |
| `phase4-capacity-label.png`, `phase4-capacity-tooltip.png` (`phase4-16-capacity.txt`) | "16 stacks per slot", with items per slot by stack size in the tooltip |
