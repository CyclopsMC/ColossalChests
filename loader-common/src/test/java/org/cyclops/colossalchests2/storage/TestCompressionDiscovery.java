package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestCompressionDiscovery extends BootstrapTest {

    private static CompressionDiscovery.Conversion conversion(Item smaller, Item larger, int ratio) {
        return new CompressionDiscovery.Conversion(smaller, larger, ratio);
    }

    @Test
    public void testChainsIntoAFamily() {
        CompressionFamilies families = CompressionDiscovery.buildFamilies(List.of(
                conversion(Items.IRON_INGOT, Items.IRON_BLOCK, 9),
                conversion(Items.IRON_NUGGET, Items.IRON_INGOT, 9)));
        CompressionFamily iron = families.find(Items.IRON_NUGGET).orElseThrow();
        assertEquals(Items.IRON_BLOCK, iron.largest().item());
        assertEquals(81, iron.largest().baseUnits());
        assertEquals(Items.IRON_NUGGET, iron.smallest().item());
        assertEquals(1, families.getFamilies().size());
    }

    @Test
    public void testTwoFormFamilies() {
        CompressionFamilies families = CompressionDiscovery.buildFamilies(List.of(
                conversion(Items.WHEAT, Items.HAY_BLOCK, 9),
                conversion(Items.QUARTZ, Items.QUARTZ_BLOCK, 4)));
        assertEquals(4, families.find(Items.QUARTZ_BLOCK).orElseThrow().largest().baseUnits());
        assertEquals(2, families.getFamilies().size());
    }

    @Test
    public void testAmbiguousItemsAreLeftOut() {
        // Gold ingots compressing into two different items is ambiguous.
        CompressionFamilies families = CompressionDiscovery.buildFamilies(List.of(
                conversion(Items.GOLD_INGOT, Items.GOLD_BLOCK, 9),
                conversion(Items.GOLD_INGOT, Items.RAW_GOLD_BLOCK, 9),
                conversion(Items.GOLD_NUGGET, Items.GOLD_INGOT, 9)));
        assertEquals(Optional.empty(), families.find(Items.GOLD_BLOCK));
        assertEquals(Optional.empty(), families.find(Items.GOLD_INGOT));
    }

    @Test
    public void testDuplicatesAreNotAmbiguous() {
        CompressionFamilies families = CompressionDiscovery.buildFamilies(List.of(
                conversion(Items.COAL, Items.COAL_BLOCK, 9),
                conversion(Items.COAL, Items.COAL_BLOCK, 9)));
        assertTrue(families.find(Items.COAL).isPresent());
    }

    @Test
    public void testCyclesAreIgnored() {
        CompressionFamilies families = CompressionDiscovery.buildFamilies(List.of(
                conversion(Items.STONE, Items.COBBLESTONE, 9),
                conversion(Items.COBBLESTONE, Items.STONE, 9)));
        assertTrue(families.getFamilies().isEmpty());
    }

}
