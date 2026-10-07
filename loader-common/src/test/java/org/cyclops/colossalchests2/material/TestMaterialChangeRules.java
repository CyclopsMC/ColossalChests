package org.cyclops.colossalchests2.material;

import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.ShippedTables;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestMaterialChangeRules extends BootstrapTest {

    @Before
    public void loadTables() {
        ChestTablesLoader.set(ShippedTables.load());
    }

    @After
    public void resetTables() {
        ChestTablesLoader.set(ChestTables.DEFAULT);
    }

    private static UpgradeSet upgrades(ChestUpgrade upgrade, int count) {
        return new UpgradeSet(Map.of(upgrade, count));
    }

    @Test
    public void testShellBlocks() {
        assertEquals(8, MaterialChangeRules.getShellBlocks(2));
        assertEquals(26, MaterialChangeRules.getShellBlocks(3));
        assertEquals(56, MaterialChangeRules.getShellBlocks(4));
        assertEquals(488, MaterialChangeRules.getShellBlocks(10));
        // The walls a change takes at each size: the whole shell, the core counting as a wall.
        int[] expected = {8, 26, 56, 98, 152, 218, 296, 386, 488};
        for (int size = 2; size <= 10; size++) {
            assertEquals(expected[size - 2], MaterialChangeRules.getShellBlocks(size));
        }
    }

    @Test
    public void testUpgradesAlwaysAllowed() {
        // Any jump upward, at every size and with every upgrade the lower material allows.
        for (int i = 0; i < ChestMaterial.BUILT_IN.size(); i++) {
            ChestMaterial from = ChestMaterial.BUILT_IN.get(i);
            for (ChestMaterial to : ChestMaterial.BUILT_IN.subList(i + 1, ChestMaterial.BUILT_IN.size())) {
                for (int size = 2; size <= from.getProperties().maxSize(); size++) {
                    assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(size, UpgradeSet.EMPTY, to));
                }
                assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(from.getProperties().maxSize(),
                        upgrades(ChestUpgrades.DEPTH, ChestUpgrades.DEPTH.getMaxCount(from.id())), to));
            }
        }
    }

    @Test
    public void testDowngradeRefusals() {
        assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(3, upgrades(ChestUpgrades.SLOT_EXPANSION, 1), ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.TOO_LARGE, MaterialChangeRules.check(4, UpgradeSet.EMPTY, ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.UPGRADE_SLOTS, MaterialChangeRules.check(3, upgrades(ChestUpgrades.SLOT_EXPANSION, 2), ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.UPGRADE_SLOTS, MaterialChangeRules.check(3,
                new UpgradeSet(Map.of(ChestUpgrades.LOCK, 1, ChestUpgrades.VOID, 1)), ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.UPGRADE_LIMIT, MaterialChangeRules.check(3, upgrades(ChestUpgrades.DEPTH, 1), ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.UPGRADE_LIMIT, MaterialChangeRules.check(3, upgrades(ChestUpgrades.COMPRESSION, 1), ChestMaterial.WOOD));
        assertEquals(MaterialChangeRules.Problem.UPGRADE_LIMIT, MaterialChangeRules.check(5, upgrades(ChestUpgrades.DEPTH, 6), ChestMaterial.OBSIDIAN));
        assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(8, upgrades(ChestUpgrades.DEPTH, 5), ChestMaterial.OBSIDIAN));
        assertEquals(MaterialChangeRules.Problem.TOO_LARGE, MaterialChangeRules.check(9, upgrades(ChestUpgrades.DEPTH, 5), ChestMaterial.OBSIDIAN));
    }

}
