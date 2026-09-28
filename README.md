## Colossal Chests 2

[![Build Status](https://github.com/CyclopsMC/ColossalChests/workflows/CI/badge.svg)](https://github.com/CyclopsMC/ColossalChests/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/CyclopsMC/ColossalChests/badge.svg)](https://coveralls.io/github/CyclopsMC/ColossalChests)

Few slots, deep slots. A multiblock bulk storage chest, shipped as the separate mod `colossalchests2`.
Colossal Chests 1 stays in maintenance mode on the `master-*` branches.

This branch is under active development and not yet playable.

### Contributing
* Before submitting a pull request containing a new feature, please discuss this first with one of the lead developers.
* When fixing an accepted bug, make sure to declare this in the issue so that no duplicate fixes exist.
* All code must comply to our coding conventions, be clean and must be well documented.

### Issues
* All bug reports and other issues are appreciated. If the issue is a crash, please include the FULL Forge log.
* Before submission, first check for duplicates, including already closed issues since those can then be re-opened.

### Branching Strategy

For every major Minecraft version, two branches exist:

* `master-{mc_version}`: Latest (potentially unstable) development.
* `release-{mc_version}`: Latest stable release for that Minecraft version. This is also tagged with all mod releases.

### Building and setting up a development environment

This mod uses [Project Lombok](http://projectlombok.org/) -- an annotation processor that allows us you to generate constructors, getters and setters using annotations -- to speed up recurring tasks and keep part of our codebase clean at the same time. Because of this it is advised that you install a plugin for your IDE that supports Project Lombok. Should you encounter any weird errors concerning missing getter or setter methods, it's probably because your code has not been processed by Project Lombok's processor. A list of Project Lombok plugins can be found [here](http://projectlombok.org/download.html).

### License
All code and images are licenced under the [MIT License](https://github.com/CyclopsMC/ColossalChests/blob/master-1.8/LICENSE.txt)
