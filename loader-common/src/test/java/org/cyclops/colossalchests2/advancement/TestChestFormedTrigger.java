package org.cyclops.colossalchests2.advancement;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestFormedTrigger extends BootstrapTest {

    private static ChestFormedTrigger.Instance parse(String json) {
        return ChestFormedTrigger.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }

    @Test
    public void testMatches() {
        ChestFormedTrigger.Instance any = new ChestFormedTrigger.Instance(Optional.empty(), Optional.empty(), Optional.empty());
        assertTrue(any.matches(ChestMaterial.WOOD, 2));
        ChestFormedTrigger.Instance largeIron = new ChestFormedTrigger.Instance(Optional.empty(), Optional.of(ChestMaterial.IRON), Optional.of(5));
        assertTrue(largeIron.matches(ChestMaterial.IRON, 5));
        assertFalse(largeIron.matches(ChestMaterial.IRON, 4));
        assertFalse(largeIron.matches(ChestMaterial.GOLD, 6));
    }

    @Test
    public void testCodec() {
        assertEquals(Optional.of(ChestMaterial.OBSIDIAN), parse("{\"material\": \"colossalchests2:obsidian\", \"minimum_size\": 8}").material());
        assertEquals(Optional.of(8), parse("{\"material\": \"colossalchests2:obsidian\", \"minimum_size\": 8}").minimumSize());
        assertTrue(ChestFormedTrigger.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"material\": \"colossalchests2:tin\"}")).isError());
    }

    @Test
    public void testMaterialChangedMatches() {
        MaterialChangedTrigger.Instance toDiamond = new MaterialChangedTrigger.Instance(Optional.empty(), Optional.empty(), Optional.of(ChestMaterial.DIAMOND));
        assertTrue(toDiamond.matches(ChestMaterial.WOOD, ChestMaterial.DIAMOND));
        assertFalse(toDiamond.matches(ChestMaterial.DIAMOND, ChestMaterial.WOOD));
    }

}
