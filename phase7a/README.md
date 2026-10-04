# Phase 7a in-game checks

Run from the repository root on the Phase 7a branch, on NeoForge, in a fresh world, in this order:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase7a/phase7a-1-walls.txt
clientdevbridge batch <path-to>/phase7a/phase7a-2-filter-gui.txt
clientdevbridge batch <path-to>/phase7a/phase7a-3-sneak-opens-chest.txt
```

| Screenshot | Shows |
|---|---|
| `phase7a-unformed.png` | A wood 3x3 front with an Interface (right), Filtered Interface (left) and Void interface (top), before the core is placed |
| `phase7a-formed.png` | The formed giant chest, with each functional wall's icon on its face |
| `phase7a-filter-empty.png` | The Filtered Interface GUI: 9 filter slots, the form slot and the direction button |
| `phase7a-filter-set.png` | Stone clicked in and nuggets shift-clicked in, mode set to Input only, with its tooltip. The inventory still holds all 16 stone and 4 nuggets |
| `phase7a-form-tooltip.png` | The empty form slot's tooltip |
| `phase7a-form-set.png` | Nuggets as the extraction form |
| `phase7a-sneak-opens-chest.png` | Sneak-clicking the Filtered Interface with an empty hand opens the chest |

After the clicks, the wall's rules read on the server were
`WallAccess[mode=INPUT, filter=[1 minecraft:stone, 1 minecraft:iron_nugget], extractionForm=minecraft:iron_nugget, voidFull=false]`.

Sneak cannot be held by clientdevbridge during a click, so it is set on the server player. Hopper behaviour is covered by game tests.
