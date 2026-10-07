# Phase 8.5 Uncolossal Chest in-game checks

Run from the repository root on the uncolossal chest branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/phase8.5-uncolossal/uncolossal.txt
```

| Screenshot | Shows |
|---|---|
| `uncolossal-facings.png` | Four uncolossal chests facing north, east, south and west, next to a vanilla chest for scale |
| `uncolossal-lid-open.png` | The lid open (driven on the client with the same block event the server sends) |
| `uncolossal-waterlogged.png` | A waterlogged one inside a glass box |
| `uncolossal-gui.png` | The GUI: the vanilla hopper screen with five slots, titled "Uncolossal Chest" |
| `uncolossal-item-hand.png` | The item in the hotbar (slot 1) next to a vanilla chest (slot 2), and held |
