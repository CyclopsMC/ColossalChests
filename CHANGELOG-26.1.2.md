# Changelog for Minecraft 26.1.2
All notable changes to this project will be documented in this file.

<a name="26.1.2-1.9.0"></a>
## [26.1.2-1.9.0](https://github.com/CyclopsMC/ColossalChests/compare/26.1.2-1.8.16...26.1.2-1.9.0) - 2026-08-27 20:29:56


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

<a name="26.1.2-1.8.16"></a>
## [26.1.2-1.8.16](https://github.com/CyclopsMC/ColossalChests/compare/26.1.2-1.8.15...26.1.2-1.8.16) - 2026-05-06 19:11:18 +0200


### Added
* New Translations (#201)

### Fixed
* Fix crash when getting capabilities on the client-side

<a name="26.1.2-1.8.15"></a>
## [26.1.2-1.8.15](https://github.com/CyclopsMC/ColossalChests/compare/26.1.2-1.8.14...26.1.2-1.8.15) - 2026-04-22 20:10:45 +0200


### Fixed
* Fix CyclopsCore version check failure on Fabric

<a name="26.1.2-1.8.14"></a>
## [26.1.2-1.8.14] - 2026-04-22 19:57:07 +0200


Initial 26.1.2 release
