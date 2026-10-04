package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestStructure {

    private static final BlockPos MIN = new BlockPos(10, 20, 30);
    private static final ChestStructure STRUCTURE = new ChestStructure(MIN, 3);

    @Test
    public void testMax() {
        assertEquals(MIN.offset(2, 2, 2), STRUCTURE.max());
    }

    @Test
    public void testContainsBounds() {
        assertTrue(STRUCTURE.contains(MIN));
        assertTrue(STRUCTURE.contains(MIN.offset(2, 2, 2)));
        assertFalse(STRUCTURE.contains(MIN.offset(-1, 0, 0)));
        assertFalse(STRUCTURE.contains(MIN.offset(3, 0, 0)));
        assertFalse(STRUCTURE.contains(MIN.offset(0, -1, 0)));
        assertFalse(STRUCTURE.contains(MIN.offset(0, 3, 0)));
        assertFalse(STRUCTURE.contains(MIN.offset(0, 0, -1)));
        assertFalse(STRUCTURE.contains(MIN.offset(0, 0, 3)));
    }

    @Test
    public void testIsOnShell() {
        assertTrue(STRUCTURE.isOnShell(MIN.offset(0, 1, 1)));
        assertTrue(STRUCTURE.isOnShell(MIN.offset(2, 1, 1)));
        assertTrue(STRUCTURE.isOnShell(MIN.offset(1, 0, 1)));
        assertTrue(STRUCTURE.isOnShell(MIN.offset(1, 2, 1)));
        assertTrue(STRUCTURE.isOnShell(MIN.offset(1, 1, 0)));
        assertTrue(STRUCTURE.isOnShell(MIN.offset(1, 1, 2)));
        assertFalse(STRUCTURE.isOnShell(MIN.offset(1, 1, 1)));
        assertFalse(STRUCTURE.isOnShell(MIN.offset(3, 1, 1)));
    }

    @Test
    public void testShell() {
        List<BlockPos> shell = Lists.newArrayList(STRUCTURE.shell());
        assertEquals(26, shell.size());
        assertFalse(shell.contains(MIN.offset(1, 1, 1)));
        assertEquals(98, Lists.newArrayList(new ChestStructure(MIN, 5).shell()).size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testShellIteratorPastEnd() {
        Iterator<BlockPos> it = STRUCTURE.shell().iterator();
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

}
