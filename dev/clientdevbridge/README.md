# clientdevbridge scripts

Scripts in this directory drive a real dev client through
[clientdevbridge-cli](https://github.com/CyclopsMC/clientdevbridge-cli) to verify what game tests cannot:
GUI rendering, click behaviour, tooltips and visual toggles.

## Loaders

clientdevbridge supports NeoForge and Fabric. Forge is not supported, so GUI validation runs on
**NeoForge** by default. Fabric can be used by passing `--loader fabric` to `start`.

## Setup

```bash
npm install -g cyclops-clientdevbridge-cli
clientdevbridge doctor
```

On a headless Linux machine this needs `xvfb` and Mesa's software GL (`doctor` prints the exact
install command if anything is missing).

## Running

From the repository root:

```bash
clientdevbridge start --loader neoforge   # first boot takes a few minutes
clientdevbridge batch dev/clientdevbridge/<script>.txt
clientdevbridge stop
```

Each script starts from the state its header comment names (usually the title screen, or a freshly
reset world via `world-reset`). Screenshots land in `.clientdevbridge/screenshots/`, named after the
script's `screenshot --name` lines.

After changing code, prefer `clientdevbridge restart` (or `start --jdwp-port` plus `hotswap` for
method-body changes) over a full stop and start.

## Scripts

| Script | Start state | Verifies |
|---|---|---|
| `phase0-modlist.txt` | title screen | the mod loads as `colossalchests2` |
