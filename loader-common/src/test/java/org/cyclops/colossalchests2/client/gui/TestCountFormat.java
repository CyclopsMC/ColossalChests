package org.cyclops.colossalchests2.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestCountFormat {

    @Test
    public void testFull() {
        assertEquals("0", CountFormat.full(0));
        assertEquals("999", CountFormat.full(999));
        assertEquals("16,777,216", CountFormat.full(16777216));
    }

    @Test
    public void testCompactSmall() {
        assertEquals("0", CountFormat.compact(0));
        assertEquals("999", CountFormat.compact(999));
    }

    @Test
    public void testCompact() {
        assertEquals("1K", CountFormat.compact(1000));
        assertEquals("1.02K", CountFormat.compact(1024));
        assertEquals("262K", CountFormat.compact(262144));
        assertEquals("1.05M", CountFormat.compact(1048576));
        assertEquals("16.8M", CountFormat.compact(16777216));
        assertEquals("4.29B", CountFormat.compact(4294967296L));
    }

    @Test
    public void testCompactRoundsUpToNextSuffix() {
        assertEquals("1M", CountFormat.compact(999999));
        assertEquals("1M", CountFormat.compact(999500));
        assertEquals("999K", CountFormat.compact(999499));
    }

}
