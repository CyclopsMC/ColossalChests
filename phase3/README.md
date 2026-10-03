# Phase 3 in-game checks

Run from the repository root on the Phase 3 branch, in order, starting from a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
for f in phase3-1-form phase3-2-hopper phase3-3-dormant phase3-3b-resume phase3-4-move-core phase3-5-reform-smaller; do
  clientdevbridge batch <path-to>/phase3/$f.txt
done
```

| Script | Checks |
|---|---|
| `phase3-1-form.txt` | Walls by command, core placed by the player: the chest forms (`formed=true`, structure `min 2,4,4 size 3`) |
| `phase3-2-hopper.txt` | A hopper on the top wall moves 5 cobblestone into the core |
| `phase3-3-dormant.txt` | The player breaks a wall: dormant, the hopper keeps its 3 items, storage stays at 5. The player puts the wall back: re-formed, storage 8 |
| `phase3-3b-resume.txt` | Rebuild check when run separately |
| `phase3-4-move-core.txt` | The player breaks the core: it drops on the player's side with its contents as a data component, and placing it in a new chest restores them |
| `phase3-5-reform-smaller.txt` | 1000 items moved into a 2x2 chest (256 per slot): the slot keeps 1000 and is extract-only, new hopper items go to the next slot |
