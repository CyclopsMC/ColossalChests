## Colossal Chests

[![Build Status](https://travis-ci.org/CyclopsMC/ColossalChests.svg?branch=master-1.9)](https://travis-ci.org/rubensworks/ColossalChests)
[![Download](https://api.bintray.com/packages/cyclopsmc/dev/ColossalChests/images/download.svg) ](https://bintray.com/cyclopsmc/dev/ColossalChests/_latestVersion)

For when regular chests are too small

All stable releases (including deobfuscated builds) can be found on [CurseForge](http://minecraft.curseforge.com/mc-mods/colossal-chests/files).

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

This mod uses [Project Lombok](http://projectlombok.org/) -- an annotation processor that allows us you to generate constructors, getters and setters using annotations -- to speed up recurring tasks and keep part of our codebase clean at the same time. Because of this it is advised that you install a plugin for your IDE that supports Project Lombok. Should you encounter any weird errors concerning missing getter or setter methods, it's probably because your code has not been processed by Project Lombok's processor. A list of Project Lombok plugins can be found [here](http://projectlombok.org/download.htm).

### License
All code and images are licenced under the [MIT License](https://github.com/CyclopsMC/ColossalChests/blob/master-1.8/LICENSE.txt)
