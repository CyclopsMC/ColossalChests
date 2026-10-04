# Phase 5 in-game checks

Run from the repository root on the Phase 5 branch, on NeoForge unless noted. GUI coordinates assume the default 854x480 window at GUI scale 2.

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase5/phase5-1-open.txt
clientdevbridge batch <path-to>/phase5/phase5-2-interactions.txt
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase5/phase5-3-views.txt
clientdevbridge stop && clientdevbridge start --loader fabric
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase5/phase5-4-fabric.txt
```

| Screenshot | Shows |
|---|---|
| `phase5-gui.png` | A copper 4x4: the upgrade column with the material's 2 slots, upgrades in the hotbar |
| `phase5-depth-installed.png` | After clicking a Depth upgrade in: 128 stacks per slot instead of 64 |
| `phase5-removal-refused.png` | Slot 0 holds more than fits without Depth: the tooltip refuses removal, and clicking leaves it in place |
| `phase5-locks.png` | Slot 0 locked (padlock) and slot 6 reserved for dirt by a right click (padlock and ghost) |
| `phase5-locked-tooltip.png` | The locked slot's tooltip |
| `phase5-slot-expansion.png` | After swapping Lock for Slot Expansion: the GUI reopened with 54 slots, locks cleared, the remaining upgrade still on the cursor |
| `phase5-upgrade-item-tooltip.png` | The upgrade item tooltip: its effect, and shift for info |
| `phase5-settings-locks.png` | Lock all and Clear locks enabled with the Lock upgrade |
| `phase5-81-slots.png` | Two Slot Expansions: 81 slots in 18 columns |
| `phase5-fabric-depth-installed.png` | The upgrade column and Depth on Fabric |

Gaps:
- Alt can't be faked by clientdevbridge (the screen reads the keyboard state), so the alt-click toggle is sent as its click packet through `eval` and read back from the server's block entity: slot 0 locked, contents kept. The same goes for the shift-for-info tooltip.
- The hopper, Lock all and Clear locks behaviour is covered by game tests.

Why an upgrade does not go in (`phase5-5-insert-reasons.txt` in a fresh world):

| Screenshot | Shows |
|---|---|
| `phase5-empty-upgrade-slot.png` | An empty upgrade slot lists what this copper chest takes |
| `phase5-upgrade-item-refused.png` | A second Depth upgrade in the inventory: why it does not fit, and that a better material takes more |
| `phase5-upgrade-item-takes.png` | The Lock upgrade: this chest takes 1 more |
| `phase5-carried-upgrade-refused.png` | Holding the refused Depth upgrade over the free slot |
