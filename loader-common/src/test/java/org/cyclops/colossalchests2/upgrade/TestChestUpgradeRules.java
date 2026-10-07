package org.cyclops.colossalchests2.upgrade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestUpgradeRules extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);
    private static final ItemStack SWORD = new ItemStack(Items.DIAMOND_SWORD);

    private static UpgradeSet upgrades(ChestUpgrade upgrade, int count) {
        return new UpgradeSet(Map.of(upgrade, count));
    }

    private static ChestStorage storage(int size, UpgradeSet upgrades) {
        return new ChestStorage(ChestUpgradeRules.getSlotCount(upgrades), ChestUpgradeRules.createProfile(size, upgrades));
    }

    @Test
    public void testMaxCountsByMaterial() {
        int[] depth = {0, 1, 2, 3, 4, 5, 6};
        for (int i = 0; i < ChestMaterial.BUILT_IN.size(); i++) {
            ResourceLocation material = ChestMaterial.BUILT_IN.get(i).id();
            assertEquals(material.toString(), depth[i], ChestUpgradeRules.getMaxCount(ChestUpgrades.DEPTH, material));
            assertEquals(material.toString(), 3, ChestUpgradeRules.getMaxCount(ChestUpgrades.SLOT_EXPANSION, material));
            assertEquals(material.toString(), 1, ChestUpgradeRules.getMaxCount(ChestUpgrades.LOCK, material));
        }
    }

    @Test
    public void testSlotExpansionLimitedByMaxSlots() {
        int maxSlots = GeneralConfig.maxSlots;
        try {
            GeneralConfig.maxSlots = 54;
            assertEquals(1, ChestUpgradeRules.getMaxCount(ChestUpgrades.SLOT_EXPANSION, ChestMaterial.NETHERITE.id()));
            GeneralConfig.maxSlots = 27;
            assertEquals(0, ChestUpgradeRules.getMaxCount(ChestUpgrades.SLOT_EXPANSION, ChestMaterial.NETHERITE.id()));
        } finally {
            GeneralConfig.maxSlots = maxSlots;
        }
    }

    @Test
    public void testCanAdd() {
        ResourceLocation copper = ChestMaterial.COPPER.id();
        assertTrue(ChestUpgradeRules.canAdd(UpgradeSet.EMPTY, ChestUpgrades.DEPTH, copper));
        assertFalse(ChestUpgradeRules.canAdd(upgrades(ChestUpgrades.DEPTH, 1), ChestUpgrades.DEPTH, copper));
        assertFalse(ChestUpgradeRules.canAdd(UpgradeSet.EMPTY, ChestUpgrades.DEPTH, ChestMaterial.WOOD.id()));
        assertTrue(ChestUpgradeRules.canAdd(upgrades(ChestUpgrades.DEPTH, 1), ChestUpgrades.LOCK, copper));
        assertFalse(ChestUpgradeRules.canAdd(upgrades(ChestUpgrades.LOCK, 1), ChestUpgrades.LOCK, copper));
    }

    @Test
    public void testDepthDoublesDepthAndRaisesNonStackables() {
        CapacityProfile base = ChestUpgradeRules.createProfile(4, UpgradeSet.EMPTY);
        CapacityProfile upgraded = ChestUpgradeRules.createProfile(4, upgrades(ChestUpgrades.DEPTH, 2));
        assertEquals(64, base.depth());
        assertEquals(256, upgraded.depth());
        assertEquals(1, base.capacityFor(1));
        assertEquals(3, upgraded.capacityFor(1));
    }

    @Test
    public void testSlotExpansionAddsSlots() {
        assertEquals(27, ChestUpgradeRules.getSlotCount(UpgradeSet.EMPTY));
        assertEquals(54, ChestUpgradeRules.getSlotCount(upgrades(ChestUpgrades.SLOT_EXPANSION, 1)));
        assertEquals(81, ChestUpgradeRules.getSlotCount(upgrades(ChestUpgrades.SLOT_EXPANSION, 2)));
        assertEquals(108, ChestUpgradeRules.getSlotCount(upgrades(ChestUpgrades.SLOT_EXPANSION, 3)));
        assertEquals(108, ChestUpgradeRules.getSlotCount(upgrades(ChestUpgrades.SLOT_EXPANSION, 5)));
    }

    @Test
    public void testRemovingAnUpgradeThatIsNotInstalledIsAllowed() {
        ChestStorage storage = storage(4, UpgradeSet.EMPTY);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 4, UpgradeSet.EMPTY, ChestUpgrades.DEPTH).isOk());
    }

    @Test
    public void testDepthRemovalRefusedWhileContentsWouldNotFit() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.DEPTH, 1);
        ChestStorage storage = storage(4, upgrades);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 4, upgrades, ChestUpgrades.DEPTH).isOk());
        storage.insert(3, STONE, 64 * 64 + 1, false);
        assertEquals(List.of(3), ChestUpgradeRules.getRemovalProblems(storage, 4, upgrades, ChestUpgrades.DEPTH).offendingSlots());
        storage.extract(3, 1, false);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 4, upgrades, ChestUpgrades.DEPTH).isOk());
    }

    @Test
    public void testDepthRemovalRefusedForNonStackables() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.DEPTH, 1);
        ChestStorage storage = storage(4, upgrades);
        storage.insert(0, SWORD, 2, false);
        assertEquals(List.of(0), ChestUpgradeRules.getRemovalProblems(storage, 4, upgrades, ChestUpgrades.DEPTH).offendingSlots());
    }

    @Test
    public void testSlotExpansionRemovalRefusedWhileExtraSlotsHoldItems() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.SLOT_EXPANSION, 2);
        ChestStorage storage = storage(3, upgrades);
        storage.insert(60, STONE, 1, false);
        // The second expansion only covers slots 54 to 80.
        assertEquals(List.of(60), ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.SLOT_EXPANSION).offendingSlots());
        storage.extract(60, 1, false);
        storage.insert(40, STONE, 1, false);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.SLOT_EXPANSION).isOk());
        assertEquals(List.of(40), ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades.with(ChestUpgrades.SLOT_EXPANSION, -1),
                ChestUpgrades.SLOT_EXPANSION).offendingSlots());
    }

    @Test
    public void testSlotExpansionRemovalIgnoresOtherOverCapacitySlots() {
        // A slot over capacity from re-forming smaller only blocks upgrades that change capacity.
        UpgradeSet upgrades = upgrades(ChestUpgrades.SLOT_EXPANSION, 1);
        ChestStorage storage = storage(4, upgrades);
        storage.insert(0, STONE, 64 * 64, false);
        storage.forceProfile(ChestUpgradeRules.createProfile(3, upgrades));
        assertTrue(storage.isExtractOnly(0));
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.SLOT_EXPANSION).isOk());
    }

    @Test
    public void testLockRemovalNeverRefused() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.LOCK, 1);
        ChestStorage storage = storage(3, upgrades);
        storage.insert(0, STONE, 10, false);
        storage.lockAllFilled();
        storage.lockTo(1, STONE);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.LOCK).isOk());
    }

    @Test
    public void testBundlingWithDepth() {
        // Unstackables hold 2^Bundling * (1 + Depth upgrades) per slot.
        for (int bundling = 0; bundling <= 4; bundling++) {
            for (int depth = 0; depth <= 3; depth++) {
                UpgradeSet set = UpgradeSet.EMPTY.with(ChestUpgrades.BUNDLING, bundling).with(ChestUpgrades.DEPTH, depth);
                assertEquals("bundling " + bundling + ", depth " + depth, (1L << bundling) * (1 + depth),
                        ChestUpgradeRules.createProfile(4, set).capacityFor(1));
            }
        }
    }

    @Test
    public void testBundlingAndVoidLimits() {
        assertEquals(4, ChestUpgradeRules.getMaxCount(ChestUpgrades.BUNDLING, ChestMaterial.WOOD.id()));
        assertEquals(1, ChestUpgradeRules.getMaxCount(ChestUpgrades.VOID, ChestMaterial.NETHERITE.id()));
    }

    @Test
    public void testBundlingRemovalRefusedWhileUnstackablesWouldNotFit() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.BUNDLING, 1);
        ChestStorage storage = storage(3, upgrades);
        storage.insert(0, SWORD, 2, false);
        assertEquals(List.of(0), ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.BUNDLING).offendingSlots());
        storage.extract(0, 1, false);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.BUNDLING).isOk());
    }

    @Test
    public void testVoidRemovalNeverRefused() {
        UpgradeSet upgrades = upgrades(ChestUpgrades.VOID, 1);
        ChestStorage storage = storage(3, upgrades);
        storage.insert(0, STONE, 10, false);
        storage.setVoiding(0, true);
        assertTrue(ChestUpgradeRules.getRemovalProblems(storage, 3, upgrades, ChestUpgrades.VOID).isOk());
    }

    @Test
    public void testUpgradeSetCounts() {
        UpgradeSet set = UpgradeSet.EMPTY.with(ChestUpgrades.DEPTH, 2).with(ChestUpgrades.LOCK, 1).with(ChestUpgrades.DEPTH, -2);
        assertFalse(set.has(ChestUpgrades.DEPTH));
        assertEquals(1, set.count(ChestUpgrades.LOCK));
        assertEquals(Map.of(ChestUpgrades.LOCK, 1), set.counts());
    }

}
