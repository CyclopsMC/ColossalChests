package org.cyclops.colossalchests2.gametest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;

import java.util.Map;

/**
 * Content registered like an addon would, only on game test servers, to test the extension points.
 * @author rubensworks
 */
public final class GameTestAddon {

    /**
     * Between gold and diamond, with its depth limit set by the material instead of the upgrade.
     */
    public static final ChestMaterial MATERIAL = new ChestMaterial(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "test_addon"), SoundType.AMETHYST, 2.5F, false, 6.0F, 45,
            new MaterialProperties(4, 5, false, Map.of(ChestUpgrades.DEPTH.getId(), 3)));

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
     * Register the addon materials, after which the caller registers their blocks.
     */
    public static void registerMaterials() {
        ChestMaterial.register(MATERIAL);
    }

}
