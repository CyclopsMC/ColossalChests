# Phase 3.5 in-game checks

Run from the repository root on the Phase 3.5 branch, each group starting from a fresh world (`clientdevbridge world-reset`):

```bash
clientdevbridge start --loader neoforge
clientdevbridge batch <path-to>/phase3.5/phase35-1-wood3.txt
clientdevbridge batch <path-to>/phase3.5/phase35-2-marker-lid.txt
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase3.5/phase35-3-materials.txt
clientdevbridge batch <path-to>/phase3.5/phase35-4-overlays.txt
clientdevbridge batch <path-to>/phase3.5/phase35-5-orientation.txt
```

`phase35-6-loader-smoke.txt` runs the same basic checks on Fabric (`clientdevbridge start --loader fabric`). clientdevbridge has no Forge support.

| Screenshot | Shows |
|---|---|
| `phase35-wood3-front.png` | A wooden 3x3 renders as one giant chest, lock plate on the core's face |
| `phase35-core-marker-sneaking.png` | While sneaking, the core marker is drawn on the core's face, in front of the lock |
| `phase35-lid-open.png` | A viewer opens the lid around its back hinge |
| `phase35-materials-south.png`, `phase35-materials-north.png` | Copper 2x2, iron 5x5, gold 4x4, diamond, obsidian and netherite 3x3: textures, scaling, and the chest facing the core's side (a core centered on the top faces south) |
| `phase35-item-overlay-closed.png` | A dev-only overlay registered at runtime draws an item on the copper core's outer faces (the core is in the lid layer) |
| `phase35-item-overlay-lid-open.png`, `phase35-lid-open-back.png` | Those overlays move along with the opening lid |
| `phase35-item-overlay-orientation.png` | A sword overlay on the iron core: not mirrored, same orientation as in a GUI |
| `phase35-neighbour-faces-lid-open.png` | A stone column next to the open lid: its faces still render and are lit |
| `phase35-iron-dormant.png` | Breaking a wall turns the iron chest back into separate blocks |
| `phase35-fabric-marker.png`, `phase35-fabric-lid-open.png` | The same on Fabric |
