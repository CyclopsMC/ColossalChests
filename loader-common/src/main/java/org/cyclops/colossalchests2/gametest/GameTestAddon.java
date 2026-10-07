package org.cyclops.colossalchests2.gametest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.cyclops.colossalchests2.config.UpgradeProperties;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;

import java.util.Map;

/**
 * Content registered like an addon would, only on game test servers, to test the extension points.
 * @author rubensworks
 */
public final class GameTestAddon {

    /**
     * After gold, with its depth limit set by the material instead of the upgrade.
     */
    public static final ChestMaterial MATERIAL = new ChestMaterial(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "test_addon"), SoundType.AMETHYST, 2.5F, false, 6.0F, ChestMaterial.GOLD.id(),
            new MaterialProperties(4, 5, false, Map.of(ChestUpgrades.DEPTH.getId(), 3)));

    /**
     * Refuses dirt, and adds a cobblestone every second, without a data file.
     */
    public static final ChestUpgrade UPGRADE = new ChestUpgrade(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "test_addon"), new UpgradeProperties(1, Map.of(), 1)) {
        @Override
        public boolean canInsert(BlockEntityChestCore core, ItemStack type, int count) {
            return !type.is(Items.DIRT);
        }

        @Override
        public void tick(BlockEntityChestCore core, int count) {
            if (core.getLevel().getGameTime() % 20 == 0) {
                core.getStorage().insert(new ItemStack(Items.COBBLESTONE), count, false);
            }
        }
    };

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
     * Register the addon content, after which the caller registers its blocks and items.
     */
    public static void register() {
        ChestMaterial.register(MATERIAL);
        ChestUpgrades.register(UPGRADE);
    }

}
