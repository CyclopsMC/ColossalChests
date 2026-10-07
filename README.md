## Colossal Chests

[![Build Status](https://github.com/CyclopsMC/ColossalChests/workflows/CI/badge.svg)](https://github.com/CyclopsMC/ColossalChests/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/CyclopsMC/ColossalChests/badge.svg)](https://coveralls.io/github/CyclopsMC/ColossalChests)
[![Crowdin](https://badges.crowdin.net/cyclopsmc-colossalchests/localized.svg)](https://crowdin.com/project/cyclopsmc-colossalchests)
[![Download](https://img.shields.io/static/v1?label=Maven&message=GitHub%20Packages&color=blue)](https://github.com/CyclopsMC/packages/packages/770015)
[![CurseForge](http://cf.way2muchnoise.eu/full_237875_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/colossal-chests)
[![Discord](https://img.shields.io/discord/386052815128100865.svg?colorB=7289DA&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHYAAABWAgMAAABnZYq0AAAACVBMVEUAAB38%2FPz%2F%2F%2F%2Bm8P%2F9AAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfhBxwQJhxy2iqrAAABoElEQVRIx7WWzdGEIAyGgcMeKMESrMJ6rILZCiiBg4eYKr%2Fd1ZAfgXFm98sJfAyGNwno3G9sLucgYGpQ4OGVRxQTREMDZjF7ILSWjoiHo1n%2BE03Aw8p7CNY5IhkYd%2F%2F6MtO3f8BNhR1QWnarCH4tr6myl0cWgUVNcfMcXACP1hKrGMt8wcAyxide7Ymcgqale7hN6846uJCkQxw6GG7h2MH4Czz3cLqD1zHu0VOXMfZjHLoYvsdd0Q7ZvsOkafJ1P4QXxrWFd14wMc60h8JKCbyQvImzlFjyGoZTKzohwWR2UzSONHhYXBQOaKKsySsahwGGDnb%2FiYPJw22sCqzirSULYy1qtHhXGbtgrM0oagBV4XiTJok3GoLoDNH8ooTmBm7ZMsbpFzi2bgPGoXWXME6XT%2BRJ4GLddxJ4PpQy7tmfoU2HPN6cKg%2BledKHBKlF8oNSt5w5g5o8eXhu1IOlpl5kGerDxIVT%2BztzKepulD8utXqpChamkzzuo7xYGk%2FkpSYuviLXun5bzdRf0Krejzqyz7Z3p0I1v2d6HmA07dofmS48njAiuMgAAAAASUVORK5CYII%3D)](https://discord.gg/9yDxubB)

Chests that are quite colossal, and can be upgraded.

All stable releases (including deobfuscated builds) can be found on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/colossal-chests/files).

[Development builds](https://github.com/CyclopsMC/packages/packages/) are hosted as GitHub packages.

### Extending
Addons can add chest materials, upgrades and functional walls.
Register blocks and items while your mod is constructed.
[`GameTestAddon`](loader-common/src/main/java/org/cyclops/colossalchests2/gametest/GameTestAddon.java) contains an example of each.

Depend on this mod and Cyclops Core from [GitHub Packages](https://github.com/CyclopsMC/packages/packages/).
This needs a [Maven token](https://github.com/settings/tokens/new?scopes=read:packages&description=GPR%20for%20Gradle).
Add it to `~/.gradle/gradle.properties`:
```
gpr.user=<YOUR GITHUB USERNAME>
gpr.key=<YOUR TOKEN>
```
Alternatively, use the environment variables `MAVEN_USERNAME` (your github username) and `MAVEN_KEY` (your token).

Then add the dependencies, replacing `neoforge` with `fabric`, `forge` or `common` (for multi-loader common code) as needed:
```groovy
repositories {
    maven {
        url "https://maven.pkg.github.com/CyclopsMC/packages"
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("MAVEN_USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("MAVEN_KEY")
        }
    }
}

dependencies {
    implementation "org.cyclops.colossalchests2:colossalchests2-1.21.1-neoforge:<version>:deobf"
    implementation "org.cyclops.cyclopscore:cyclopscore-1.21.1-neoforge:<version>:deobf"
}
```

Materials:
* Register a `BlockChestWall` and a `BlockChestCore` with `new ChestMaterial(id)`, for example with `BlockChestWallConfig` and `BlockChestCoreConfig`.
* Define the material in `data/<ns>/colossalchests2/material/<name>.json`, with `after` (such as `colossalchests2:copper`), `max_size`, `upgrade_slots`, `blast_resistant` and `upgrade_limits`. This mod's materials are defined the same way, so datapacks can change or reorder them.
* Add the giant chest texture `textures/entity/chest/<name>.png` and the lang key `material.<ns>.<name>`.

Upgrades:
* `ChestUpgrades.register(new ChestUpgrade(id) { ... })`, overriding hooks such as `applyProfile`, `getExtraSlots`, `canInsert` and `tick`.
* Register the item `<ns>:upgrade_<name>`, for example with `ItemChestUpgradeConfig`.
* Define its limits and strength in `data/<ns>/colossalchests2/upgrade/<name>.json`, with `max_count`, `max_count_by_material` and `value`. Without this file, the upgrade is disabled.

Functional walls:
* Extend `BlockChestWall` with the `BlockChestWall(Properties)` constructor, so it fits any material.
* Get the chest with `ChestCoreIndex.findFormedCore`, and override `onChestContentsChanged` to react to changes.
* Optionally draw on the giant chest with `ChestOverlays.register`.

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
