# Nested chest cores in-game check

Run from the repository root on the branch, on NeoForge, in a fresh world:

```bash
clientdevbridge start --loader neoforge
clientdevbridge world-reset
clientdevbridge batch --continue-on-error <path-to>/nested-cores/nested-1.txt
```

A wood 2x2 holding 100 stone is broken, which drops its core with the stone in it. The player gets that core, a shulker box holding a copy of it, and a shulker box holding stone, then shift-clicks all three into an iron 3x3.

Read back from the world (no screenshots, the result is in the state):

| Item | Result |
|---|---|
| Core carrying items | Stays in the inventory |
| Shulker box holding that core | Stays in the inventory |
| Shulker box holding stone | Stored in the chest |
