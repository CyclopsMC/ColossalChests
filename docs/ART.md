# Colossal Chests 2 art

Where each texture comes from, so placeholders can be found and replaced.
Paths are relative to `loader-common/src/main/resources/assets/colossalchests2/textures/`.

| Status | Meaning |
|---|---|
| CC1 | Art from Colossal Chests 1, reused as is |
| CC1, recoloured | CC1's design with new colours |
| Placeholder | Programmer art made for CC2, to be replaced by final art |

## Blocks

| Texture | Status | Notes |
|---|---|---|
| `block/chest_wall_{wood,copper,iron,gold,diamond,netherite}` | CC1 | |
| `block/chest_core_{wood,copper,iron,gold,diamond,netherite}` | CC1 | CC1's colossal chest core textures |
| `block/chest_wall_obsidian`, `block/chest_core_obsidian` | CC1, recoloured | CC1's obsidian was light cyan and looked like diamond; recoloured to an obsidian palette |
| `block/chest_wall_{interface,void,display,redstone,magnet}` | Placeholder | Unformed functional walls |
| `block/chest_wall_{interface,void,display,redstone,magnet}_icon` | Placeholder | Icons drawn on formed functional walls |
| `block/display_panel`, `block/display_bar` | Placeholder | Display wall plate and fill bar |
| `block/formed_member` | Placeholder | Transparent texture for formed members, which the giant chest draws over |

## Giant chest

| Texture | Status | Notes |
|---|---|---|
| vanilla `entity/chest/normal` | Vanilla | Wooden chests use the vanilla chest, including its Christmas texture |
| `entity/chest/{copper,iron,gold,diamond,netherite}` | Placeholder | Generated from the CC1 wall colours in the vanilla chest layout |
| `entity/chest/obsidian` | Placeholder, recoloured | As above, recoloured to the obsidian palette |

## Items

| Texture | Status | Notes |
|---|---|---|
| `item/material_upgrade_tool` | CC1 | CC1's upgrade tool |
| `item/upgrade_{slot_expansion,depth,lock,bundling,void,compression}` | Placeholder | |

The Uncolossal Chest renders the vanilla chest model and has no texture of its own.
