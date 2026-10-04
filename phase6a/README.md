# Phase 6a in-game checks

Run from the repository root on the Phase 6a branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch <path-to>/phase6a/phase6a-1-void-bundling.txt
```

| Screenshot | Shows |
|---|---|
| `phase6a-void-marks.png` | An iron 3x3 with Void and two Bundling upgrades: voiding cobblestone, 4 swords in one slot, and a reserved and voiding gravel slot |
| `phase6a-voiding-tooltip.png` | The voiding slot's tooltip |
| `phase6a-bundling-capacity.png` | 4 unstackable items per slot with two Bundling upgrades |
| `phase6a-settings.png` | The settings view with Void all and Clear voids, four rows fitting the 27 slot chest |

Alt can't be faked by clientdevbridge, so the alt-right-click toggles are sent as their click packets. Hopper voiding is covered by game tests.
