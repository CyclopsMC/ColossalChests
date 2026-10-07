package org.cyclops.colossalchests2.block;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestMaterial {

    private static ChestMaterial material(String path, int tier) {
        return new ChestMaterial(ResourceLocation.fromNamespaceAndPath("othermod", path), SoundType.METAL, 1, true, 1, tier, MaterialProperties.DEFAULT);
    }

    @Test
    public void testBuiltInOrder() {
        assertEquals(ChestMaterial.BUILT_IN, ChestMaterial.getAll().stream().filter(ChestMaterial.BUILT_IN::contains).toList());
    }

    @Test
    public void testRegisterSortsByTier() {
        ChestMaterial ruby = material("material_test_ruby", 55);
        ChestMaterial.register(ruby);
        assertTrue(ChestMaterial.getAll().contains(ruby));
        assertEquals(ChestMaterial.getAll().indexOf(ChestMaterial.DIAMOND) + 1, ChestMaterial.getAll().indexOf(ruby));
        assertEquals(ruby, ChestMaterial.byId(ruby.id()).orElseThrow());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDuplicate() {
        ChestMaterial.register(ChestMaterial.IRON);
    }

    @Test
    public void testDisplayNameUsesNamespace() {
        assertEquals("material.othermod.ruby", ((TranslatableContents) material("ruby", 55).getDisplayName().getContents()).getKey());
        assertEquals("material.colossalchests2.iron", ((TranslatableContents) ChestMaterial.IRON.getDisplayName().getContents()).getKey());
    }

}
