package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestDeepSlot extends BootstrapTest {

    @Test
    public void testNormalizesToEmpty() {
        assertSame(DeepSlot.EMPTY, DeepSlot.of(ItemStack.EMPTY, 10));
        assertSame(DeepSlot.EMPTY, DeepSlot.of(new ItemStack(Items.STONE), 0));
        assertTrue(DeepSlot.EMPTY.isEmpty());
    }

    @Test
    public void testLockedAtZeroKeepsType() {
        DeepSlot slot = DeepSlot.of(new ItemStack(Items.STONE), 0, true, null);
        assertFalse(slot.isEmpty());
        assertTrue(slot.isLocked());
        assertEquals(0, slot.getCount());
        assertSame(DeepSlot.EMPTY, slot.withLocked(false));
    }

    @Test
    public void testPrototypeHasCountOne() {
        DeepSlot slot = DeepSlot.of(new ItemStack(Items.STONE, 32), 1000);
        assertEquals(1, slot.getPrototype().getCount());
        assertEquals(1000, slot.getCount());
    }

    @Test
    public void testMatches() {
        DeepSlot slot = DeepSlot.of(new ItemStack(Items.STONE), 5);
        assertTrue(slot.matches(new ItemStack(Items.STONE, 64)));
        assertFalse(slot.matches(new ItemStack(Items.DIRT)));
        assertFalse(DeepSlot.EMPTY.matches(new ItemStack(Items.STONE)));
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        damaged.setDamageValue(3);
        assertFalse(DeepSlot.of(new ItemStack(Items.IRON_PICKAXE), 1).matches(damaged));
    }

    @Test
    public void testWithers() {
        DeepSlot slot = DeepSlot.of(new ItemStack(Items.STONE), 5);
        assertEquals(7, slot.withCount(7).getCount());
        assertEquals(Optional.of(Items.IRON_INGOT), slot.withCompressionForm(Items.IRON_INGOT).getCompressionForm());
        assertEquals(Optional.empty(), slot.withCompressionForm(Items.IRON_INGOT).withCompressionForm(null).getCompressionForm());
        assertEquals(Optional.empty(), slot.withCompressionForm(Items.AIR).getCompressionForm());
        assertTrue(slot.withLocked(true).isLocked());
    }

    @Test
    public void testEqualsAndHashCode() {
        DeepSlot a = DeepSlot.of(new ItemStack(Items.STONE), 5);
        DeepSlot b = DeepSlot.of(new ItemStack(Items.STONE, 3), 5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, a.withCount(6));
        assertNotEquals(a, a.withLocked(true));
        assertNotEquals(a, DeepSlot.of(new ItemStack(Items.DIRT), 5));
        assertNotEquals(a, a.withCompressionForm(Items.IRON_INGOT));
        assertTrue(a.toString().contains("x5"));
        assertTrue(a.withCompressionForm(Items.IRON_INGOT).toString().contains("form="));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCount() {
        DeepSlot.of(new ItemStack(Items.STONE), -1);
    }

}
