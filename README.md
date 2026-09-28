# clientdevbridge screenshots and scripts

Screenshots and rerunnable scripts for the Colossal Chests 2 phase PRs, kept off the code branches.

Run a script from the repo root of a checked-out phase branch:

```bash
npm install -g cyclops-clientdevbridge-cli
clientdevbridge start --loader neoforge
clientdevbridge batch <path-to>/phaseN/<script>.txt
clientdevbridge stop
```

Forge is not supported by clientdevbridge; NeoForge is used for GUI validation.
