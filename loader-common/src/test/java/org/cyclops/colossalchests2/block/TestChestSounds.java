package org.cyclops.colossalchests2.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestSounds extends BootstrapTest {

    @Test
    public void testPitchDropsWithSize() {
        // A single block chest sounds higher than a vanilla chest, the largest ones at the lowest pitch Minecraft plays.
        assertEquals(1.25F, ChestSounds.getPitch(1), 0.001F);
        assertTrue(ChestSounds.getPitch(2) < 1.0F && ChestSounds.getPitch(2) > 0.9F);
        assertTrue(ChestSounds.getPitch(10) >= 0.5F && ChestSounds.getPitch(10) < 0.55F);
        for (int size = 2; size <= 10; size++) {
            assertTrue("size " + size, ChestSounds.getPitch(size) < ChestSounds.getPitch(size - 1));
        }
        assertEquals(0.5F, ChestSounds.getPitch(100), 0.001F);
        assertEquals(ChestSounds.getPitch(1), ChestSounds.getPitch(0), 0.001F);
    }

    @Test
    public void testVolumeGrowsWithSize() {
        assertEquals(0.5F, ChestSounds.getVolume(1), 0.001F);
        for (int size = 2; size <= 10; size++) {
            assertTrue("size " + size, ChestSounds.getVolume(size) > ChestSounds.getVolume(size - 1));
        }
        // Heard from about 30 blocks away at size 10, instead of 16.
        assertTrue(ChestSounds.getVolume(10) * 16 > 28);
    }

    @Test
    public void testLidSpeed() {
        for (int size = 1; size <= ChestSounds.VANILLA_LID_MAX_SIZE; size++) {
            assertEquals(0.1F, ChestSounds.getLidSpeed(size), 0.0001F);
        }
        for (int size = ChestSounds.VANILLA_LID_MAX_SIZE + 1; size <= 10; size++) {
            assertTrue("size " + size, ChestSounds.getLidSpeed(size) < ChestSounds.getLidSpeed(size - 1));
        }
        // Under a second for the largest chests.
        assertTrue(Math.ceil(1 / ChestSounds.getLidSpeed(10)) <= 20);
    }

    @Test
    public void testLayersByMaterial() {
        assertEquals(List.of(new ChestSounds.Layer(SoundEvents.CHEST_OPEN, 1.0F)), ChestSounds.getLayers(ChestMaterial.WOOD, true));
        assertEquals(List.of(new ChestSounds.Layer(SoundEvents.CHEST_CLOSE, 1.0F), new ChestSounds.Layer(SoundEvents.COPPER_TRAPDOOR_CLOSE, 0.4F)),
                ChestSounds.getLayers(ChestMaterial.COPPER, false));
        assertEquals(List.of(new ChestSounds.Layer(SoundEvents.CHEST_OPEN, 1.0F), new ChestSounds.Layer(SoundEvents.IRON_TRAPDOOR_OPEN, 0.4F)),
                ChestSounds.getLayers(ChestMaterial.DIAMOND, true));
        assertEquals(List.of(new ChestSounds.Layer(SoundEvents.ENDER_CHEST_CLOSE, 1.0F)), ChestSounds.getLayers(ChestMaterial.NETHERITE, false));
        for (ChestMaterial material : ChestMaterial.VALUES) {
            assertFalse(material.getName(), ChestSounds.getLayers(material, true).isEmpty());
            assertNotEquals(material.getName(), ChestSounds.getLayers(material, true), ChestSounds.getLayers(material, false));
        }
        assertEquals(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, ChestSounds.getThud(ChestMaterial.WOOD));
        assertEquals(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, ChestSounds.getThud(ChestMaterial.OBSIDIAN));
    }

    @Test
    public void testPitchVariation() {
        RandomSource random = RandomSource.create(0);
        for (int i = 0; i < 1000; i++) {
            float variation = ChestSounds.getPitchVariation(random);
            assertTrue(variation >= 0.95F && variation <= 1.05F);
        }
    }

}
