# Changelog for Minecraft 1.21.1
All notable changes to this project will be documented in this file.

<a name="1.21.1-1.9.0"></a>
## [1.21.1-1.9.0](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.14...1.21.1-1.9.0) - 2026-08-27 20:29:38


### Added
* Add Netherite Colossal Chests as new top tier, Closes #168

Netherite becomes the new highest chest material, above obsidian:
* Inventory multiplier of 5 (obsidian and diamond are 4)
* Blast-resistant, like obsidian
* Top of the chest upgrade/downgrade tool chain

Chest walls are obtained by upgrading diamond chest walls at a smithing
table with netherite scrap. The netherite upgrade smithing template is
deliberately not used as the template ingredient, since the smithing menu
consumes all three input slots, which would cost one template per wall.
A gold ingot is used instead, mirroring the scrap plus gold cost of a
netherite ingot. Cores and interfaces are crafted from netherite walls
just like the other materials.

<a name="1.21.1-1.8.14"></a>
## [1.21.1-1.8.14](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.13...1.21.1-1.8.14) - 2025-10-12 15:07:17 +0200


### Added
* Add config option to disable chest opening, Closes #167

<a name="1.21.1-1.8.13"></a>
## [1.21.1-1.8.13](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.12...1.21.1-1.8.13) - 2025-08-12 20:40:00 +0200


### Added
* Add translations through Crowdin (#189)
* Add PT_BR localization (#190)

### Fixed
* Fix item loss when replacing walls in Fabric, Closes #191
* Fix some spelling and grammar typos in lang (#187)

<a name="1.21.1-1.8.12"></a>
## [1.21.1-1.8.12](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.11...1.21.1-1.8.12) - 2025-02-23 10:32:21 +0100


### Added

### Changed

### Fixed
----- TODO: arrange -----
* Bump mod version

* Bump mod version

* Fix GUIs remaining open after external breakage, Closes #183

* Allow failures of Modrinth CI step

* Allow failures of Modrinth CI step

* Don't publish beta and release on the same commit

* Allow failures of Modrinth CI step

* Add translations through Crowdin (#182)

Co-authored-by: Crowdin Bot <support+bot@crowdin.com>
* Bump mod version

* Fix enchanted items not being displayed, Closes #181

* Add ja_jp translations

Co-authored-by: Crowdin Bot <support+bot@crowdin.com>
* Update to actions/cache@v4

* Update to actions/cache@v4

* Add tr_tr.json Turkish localization (#166)


* Revert "Fix entities sometimes spawning in chest, Closes #163"

This reverts commit 143b480a45c1344033aa3c6232ea0f19cd17acde.

* Revert "Fix entities sometimes spawning in chest, Closes #163"

This reverts commit 143b480a45c1344033aa3c6232ea0f19cd17acde.

Closes #165

<a name="1.21.1-1.8.11"></a>
## [1.21.1-1.8.11](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.10...1.21.1-1.8.11) - 2025-01-19 10:22:02 +0100


### Added
* Add cs_cz translations
* Add nl_nl and tr_tr translations

### Fixed
* Fix enchanted items not being displayed, Closes #181

<a name="1.21.1-1.8.10"></a>
## [1.21.1-1.8.10](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.9...1.21.1-1.8.10) - 2024-11-25 16:11:47 +0100


### Fixed
* Fix client disconnect when opening large chests

<a name="1.21.1-1.8.9"></a>
## [1.21.1-1.8.9](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.8...1.21.1-1.8.9) - 2024-11-19 10:36:01 +0100


### Fixed
* Fix Fabric crash on explosion, Closes #174

<a name="1.21.1-1.8.8"></a>
## [1.21.1-1.8.8](https://github.com/CyclopsMC/ColossalChests/compare/1.21.1-1.8.7...1.21.1-1.8.8) - 2024-10-16 17:51:43 +0200


### Fixed
* Fix crash when using Integrated Tunnels with chests, Closes #169

<a name="1.21.1-1.8.7"></a>
## [1.21.1-1.8.7] - 2024-10-09 17:06:29 +0200


### Changed
* Refactor code to be compatible with NeoForge, Forge, and Fabric.
