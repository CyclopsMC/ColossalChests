package org.cyclops.colossalchests2.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestCompressionFamily extends BootstrapTest {

    static CompressionFamily iron() {
        return CompressionFamily.of(Items.IRON_BLOCK, 9, Items.IRON_INGOT, 9, Items.IRON_NUGGET);
    }

    @Test
    public void testOf() {
        CompressionFamily family = iron();
        assertEquals(3, family.size());
        assertEquals(new CompressionFamily.Form(Items.IRON_BLOCK, 81), family.largest());
        assertEquals(new CompressionFamily.Form(Items.IRON_INGOT, 9), family.get(1));
        assertEquals(new CompressionFamily.Form(Items.IRON_NUGGET, 1), family.smallest());
    }

    @Test
    public void testIndexOf() {
        CompressionFamily family = iron();
        assertEquals(0, family.indexOf(Items.IRON_BLOCK));
        assertEquals(2, family.indexOf(Items.IRON_NUGGET));
        assertEquals(-1, family.indexOf(Items.GOLD_INGOT));
        assertEquals(1, family.indexOf(new ItemStack(Items.IRON_INGOT, 5)));
        assertEquals(-1, family.indexOf(ItemStack.EMPTY));
        ItemStack named = new ItemStack(Items.IRON_INGOT);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        assertEquals(-1, family.indexOf(named));
    }

    @Test
    public void testConversionAcrossThreeForms() {
        CompressionFamily family = iron();
        assertEquals(81, family.toBaseUnits(0, 1));
        assertEquals(18, family.toBaseUnits(1, 2));
        assertEquals(5, family.toBaseUnits(2, 5));

        long base = family.toBaseUnits(0, 1234) + family.toBaseUnits(1, 5) + family.toBaseUnits(2, 7);
        assertEquals(1234, family.fromBaseUnits(0, base));
        assertEquals(1234 * 9 + 5, family.fromBaseUnits(1, base));
        assertEquals(base, family.fromBaseUnits(2, base));
        assertEquals(52, family.remainderBaseUnits(0, base));
        assertEquals(7, family.remainderBaseUnits(1, base));
        assertEquals(0, family.remainderBaseUnits(2, base));

        assertEquals(11106, family.convert(0, 1234, 1));
        assertEquals(1, family.convert(1, 17, 0));
        assertEquals(90, family.convert(1, 10, 2));
    }

    @Test
    public void testToBaseUnitsSaturates() {
        assertEquals(Long.MAX_VALUE, iron().toBaseUnits(0, Long.MAX_VALUE / 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewForms() {
        new CompressionFamily(List.of(new CompressionFamily.Form(Items.IRON_NUGGET, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmallestMustBeOne() {
        new CompressionFamily(List.of(new CompressionFamily.Form(Items.IRON_BLOCK, 18), new CompressionFamily.Form(Items.IRON_INGOT, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotWholeMultiple() {
        new CompressionFamily(List.of(new CompressionFamily.Form(Items.IRON_BLOCK, 10), new CompressionFamily.Form(Items.IRON_INGOT, 3), new CompressionFamily.Form(Items.IRON_NUGGET, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateForm() {
        new CompressionFamily(List.of(new CompressionFamily.Form(Items.IRON_INGOT, 9), new CompressionFamily.Form(Items.IRON_INGOT, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfOddArguments() {
        CompressionFamily.of(Items.IRON_BLOCK, 9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormNonPositive() {
        new CompressionFamily.Form(Items.IRON_INGOT, 0);
    }

}
