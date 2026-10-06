# Phase 8 Material Upgrade Tool in-game checks

Run from the repository root on the material upgrades branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/phase8-material-upgrades/tool.txt
```

A wooden 3x3 with an Interface wall at the top middle, changed in survival mode. A change takes one wall of the new
material per plain block (24 walls and the core here, the Interface wall stays) and gives the old walls back.

| Screenshot | Shows |
|---|---|
| `overview.png` | The four main steps in one image |
| `tool-no-target.png` | Using the tool before picking a material |
| `tool-gui-empty.png` | The GUI, opened by using the tool in the air (the tooltip is from the mouse resting on a button) |
| `tool-gui-diamond.png` | After clicking Diamond: its button is disabled and the label shows the choice |
| `tool-missing.png` | With 20 diamond walls: "Needs 25x Diamond Chest Wall, missing 5", nothing changed |
| `tool-diamond.png` | With 26: the chest is Diamond. Inventory after: 1 diamond wall, 25 wooden walls given back |
| `tool-back-to-wood.png` | Wood picked in the GUI and applied: inventory after: 26 diamond walls, no wooden walls |

Block states read back after the Diamond step: core, a plain wall and the Interface wall are all formed, the plain
blocks are diamond and the Interface wall is unchanged.
