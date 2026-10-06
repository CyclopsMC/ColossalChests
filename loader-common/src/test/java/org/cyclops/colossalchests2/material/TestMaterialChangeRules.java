package org.cyclops.colossalchests2.material;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.config.MaterialCost;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestMaterialChangeRules extends BootstrapTest {

    private static MaterialCost cost(String item, int count) {
        return new MaterialCost(ResourceLocation.withDefaultNamespace(item), count);
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
    }

    @Test
    public void testCostPerTierStep() {
        Map<ChestMaterial, List<MaterialCost>> perBlock = Map.of(
                ChestMaterial.COPPER, List.of(cost("copper_ingot", 8)),
                ChestMaterial.IRON, List.of(cost("iron_ingot", 8)),
                ChestMaterial.GOLD, List.of(cost("gold_ingot", 8)),
                ChestMaterial.DIAMOND, List.of(cost("diamond", 8)),
                ChestMaterial.OBSIDIAN, List.of(cost("obsidian", 8)),
                ChestMaterial.NETHERITE, List.of(cost("netherite_scrap", 1), cost("gold_ingot", 1)));
        for (ChestMaterial from : ChestMaterial.VALUES) {
            if (from.next().isEmpty()) {
                continue;
            }
            ChestMaterial to = from.next().get();
            assertEquals(to.getName(), perBlock.get(to), to.getProperties().upgradeCost());
            // Every size the source material can form.
            for (int size = 2; size <= from.getProperties().maxSize(); size++) {
                int blocks = MaterialChangeRules.getShellBlocks(size);
                List<MaterialCost> expected = perBlock.get(to).stream().map(c -> new MaterialCost(c.item(), c.count() * blocks)).toList();
                assertEquals(to.getName() + " " + size, expected, MaterialChangeRules.getTotalCost(to.getProperties().upgradeCost(), blocks));
            }
        }
        assertEquals(List.of(), ChestMaterial.WOOD.getProperties().upgradeCost());
    }

    @Test
    public void testTotalCostMergesItems() {
        assertEquals(List.of(cost("gold_ingot", 30), cost("diamond", 10)),
                MaterialChangeRules.getTotalCost(List.of(cost("gold_ingot", 1), cost("diamond", 1), cost("gold_ingot", 2)), 10));
        assertEquals(List.of(), MaterialChangeRules.getTotalCost(List.of(cost("gold_ingot", 1)), 0));
    }

    @Test
    public void testUpgradesAlwaysAllowed() {
        for (ChestMaterial from : ChestMaterial.VALUES) {
            from.next().ifPresent(to -> {
                for (int size = 2; size <= from.getProperties().maxSize(); size++) {
                    assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(size, UpgradeSet.EMPTY, to));
                }
                assertEquals(MaterialChangeRules.Problem.NONE, MaterialChangeRules.check(from.getProperties().maxSize(),
                        upgrades(ChestUpgrades.DEPTH, ChestUpgrades.DEPTH.getMaxCount(from.id())), to));
            });
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
