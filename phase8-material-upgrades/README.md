# Phase 8 Material Upgrades in-game checks

Run from the repository root on the material upgrades branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/phase8-material-upgrades/upgrade.txt
```

A wooden 3x3 with an Interface wall at the top middle, upgraded in survival mode with only the Material Upgrade items.
Each step costs 25 blocks' worth (24 plain walls and the core; the Interface wall stays), for example 200 copper ingots.

| Screenshot | Shows |
|---|---|
| `tiers-overview.png` | All of the below in one image |
| `tier-0-wood.png` | The starting wooden chest |
| `refused-missing.png` | The copper upgrade without ingots: refused, "missing: 200x Copper Ingot" |
| `tier-1-copper.png` .. `tier-6-netherite.png` | After each upgrade |
| `downgraded-obsidian.png` | Sneak-right-click with the netherite upgrade: back to obsidian, 25 netherite scrap and 25 gold ingots refunded |
| `netherite-gui.png` | Upgraded again with the refund, opened: a Netherite Chest |

Read back from the world after each step: the core, a plain wall and the Interface wall are all formed, the plain blocks
are the new material, the Interface wall is unchanged, and the inventory holds only the six upgrade items
(every cost item was used up exactly).
