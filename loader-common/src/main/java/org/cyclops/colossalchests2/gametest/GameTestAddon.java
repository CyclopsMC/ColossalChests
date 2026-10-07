package org.cyclops.colossalchests2.gametest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.ChestMaterial;

/**
 * Content registered like an addon would, only on game test servers, to test the extension points.
 * @author rubensworks
 */
public final class GameTestAddon {

    /**
     * Defined by data/colossalchests2test/colossalchests2/material/test_addon.json: after gold, with its depth limit set
     * by the material instead of the upgrade. That file does nothing outside game tests, as the blocks are missing.
     * Its namespace differs from its blocks', like a material of another mod.
     */
    public static final ChestMaterial MATERIAL = new ChestMaterial(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID + "test", "test_addon"));

    private GameTestAddon() {
    }

    /**
     * @return If this is a game test server of any loader.
     */
    public static boolean isEnabled() {
        return Boolean.getBoolean("neoforge.gameTestServer") || Boolean.getBoolean("forge.gameTestServer")
                || System.getProperty("fabric-api.gametest") != null;
    }

    /**
     * @return New properties for a wall or core of the test material.
     */
    public static Block.Properties createProperties() {
        return Block.Properties.of().strength(2.5F, 6.0F).sound(SoundType.AMETHYST);
    }

}
