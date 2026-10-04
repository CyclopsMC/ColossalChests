# Phase 7a in-game checks

Run from the repository root on the Phase 7a branch, on NeoForge, in a fresh world, in this order:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase7a/phase7a-1-walls.txt
clientdevbridge batch <path-to>/phase7a/phase7a-2-settings.txt
```

| Screenshot | Shows |
|---|---|
| `phase7a-unformed.png` | A wood 3x3 front with two Interfaces (left, right) and a Void interface (top), before the core is placed |
| `phase7a-formed.png` | The formed giant chest: functional walls are hidden, like the core |
| `phase7a-formed-sneaking.png` | While sneaking, each functional wall's icon shows on its face, next to the core marker |
| `phase7a-settings-empty.png` | The Interface settings, opened by sneak-clicking: 9 filter slots and the direction button |
| `phase7a-settings-set.png` | Stone clicked in and nuggets shift-clicked in, direction set to Input only, with its tooltip. The inventory still holds all 16 stone and 4 nuggets |
| `phase7a-filter-tooltip.png` | An empty filter slot's tooltip |

The script also checks that a plain click on the Interface opens the chest (`ContainerScreenChest`) and a sneak-click opens its settings (`ContainerScreenInterface`).

After the clicks, the wall's rules read on the server were
`WallAccess[mode=INPUT, filter=[1 minecraft:stone, 1 minecraft:iron_nugget], voidFull=false]`.

Sneaking is done by pressing the sneak key binding through `eval`. Hopper behaviour is covered by game tests.
