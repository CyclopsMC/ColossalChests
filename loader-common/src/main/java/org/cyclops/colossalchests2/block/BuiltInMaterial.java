package org.cyclops.colossalchests2.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/**
 * The block properties of this mod's materials. Everything else about them is in their data files.
 * @author rubensworks
 */
public enum BuiltInMaterial {
    WOOD(ChestMaterial.WOOD, SoundType.WOOD, 2.5F, false, 2.5F),
    COPPER(ChestMaterial.COPPER, SoundType.COPPER, 3.0F, true, 6.0F),
    IRON(ChestMaterial.IRON, SoundType.METAL, 5.0F, true, 6.0F),
    GOLD(ChestMaterial.GOLD, SoundType.METAL, 3.0F, true, 6.0F),
    DIAMOND(ChestMaterial.DIAMOND, SoundType.METAL, 5.0F, true, 6.0F),
    OBSIDIAN(ChestMaterial.OBSIDIAN, SoundType.STONE, 10.0F, true, 1200.0F),
    NETHERITE(ChestMaterial.NETHERITE, SoundType.NETHERITE_BLOCK, 10.0F, true, 1200.0F);

    private final ChestMaterial material;
    private final SoundType soundType;
    private final float hardness;
    private final boolean needsPickaxe;
    private final float blastResistance;

    BuiltInMaterial(ChestMaterial material, SoundType soundType, float hardness, boolean needsPickaxe, float blastResistance) {
        this.material = material;
        this.soundType = soundType;
        this.hardness = hardness;
        this.needsPickaxe = needsPickaxe;
        this.blastResistance = blastResistance;
    }

    public ChestMaterial getMaterial() {
        return material;
    }

    /**
     * @return New properties for a wall or core of this material.
     */
    public Block.Properties createProperties() {
        Block.Properties properties = Block.Properties.of()
                .strength(hardness, blastResistance)
                .sound(soundType)
                .isValidSpawn((state, level, pos, entityType) -> false);
        if (needsPickaxe) {
            properties.requiresCorrectToolForDrops();
        }
        return properties;
    }
}
