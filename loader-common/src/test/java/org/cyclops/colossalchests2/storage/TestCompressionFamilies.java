package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestCompressionFamilies extends BootstrapTest {

    @Test
    public void testRegisterAndFind() {
        CompressionFamilies families = new CompressionFamilies();
        CompressionFamily iron = TestCompressionFamily.iron();
        families.register(iron);
        assertEquals(Optional.of(iron), families.find(Items.IRON_NUGGET));
        assertEquals(Optional.of(iron), families.find(new ItemStack(Items.IRON_BLOCK)));
        assertEquals(Optional.empty(), families.find(Items.GOLD_INGOT));
        assertEquals(Optional.empty(), families.find(ItemStack.EMPTY));
        assertEquals(1, families.getFamilies().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverlapRejected() {
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        families.register(CompressionFamily.of(Items.IRON_INGOT, 9, Items.GOLD_NUGGET));
    }

    @Test
    public void testClear() {
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        families.clear();
        assertTrue(families.getFamilies().isEmpty());
        assertEquals(Optional.empty(), families.find(Items.IRON_INGOT));
    }

}
