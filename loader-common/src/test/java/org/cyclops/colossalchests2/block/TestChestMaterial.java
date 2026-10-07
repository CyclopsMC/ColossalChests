package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestMaterial {

    private final Map<ChestMaterial, ResourceLocation> after = Maps.newHashMap();

    public TestChestMaterial() {
        for (int i = 1; i < ChestMaterial.BUILT_IN.size(); i++) {
            after.put(ChestMaterial.BUILT_IN.get(i), ChestMaterial.BUILT_IN.get(i - 1).id());
        }
    }

    private ChestMaterial material(String namespace, String path, ChestMaterial after) {
        return material(namespace, path, after.id());
    }

    private ChestMaterial material(String namespace, String path, ResourceLocation after) {
        ChestMaterial material = new ChestMaterial(ResourceLocation.fromNamespaceAndPath(namespace, path));
        this.after.put(material, after);
        return material;
    }

    private List<ChestMaterial> order(ChestMaterial... added) {
        return ChestMaterials.order(ImmutableList.<ChestMaterial>builder().add(added).addAll(ChestMaterial.BUILT_IN).build(), after::get);
    }

    private static List<ChestMaterial> with(ChestMaterial after, ChestMaterial... inserted) {
        List<ChestMaterial> expected = new ArrayList<>(ChestMaterial.BUILT_IN);
        expected.addAll(expected.indexOf(after) + 1, List.of(inserted));
        return expected;
    }

    @Test
    public void testBuiltInOrder() {
        assertEquals(ChestMaterial.BUILT_IN, order());
    }

    @Test
    public void testAfter() {
        ChestMaterial ruby = material("othermod", "ruby", ChestMaterial.COPPER);
        assertEquals(with(ChestMaterial.COPPER, ruby), order(ruby));
    }

    @Test
    public void testAfterLast() {
        ChestMaterial ruby = material("othermod", "ruby", ChestMaterial.NETHERITE);
        assertEquals(with(ChestMaterial.NETHERITE, ruby), order(ruby));
    }

    @Test
    public void testSameAfterOrderedById() {
        ChestMaterial ruby = material("zmod", "ruby", ChestMaterial.COPPER);
        ChestMaterial jade = material("amod", "jade", ChestMaterial.COPPER);
        assertEquals(with(ChestMaterial.COPPER, jade, ruby), order(ruby, jade));
    }

    @Test
    public void testAfterAddedMaterial() {
        ChestMaterial ruby = material("othermod", "ruby", ChestMaterial.COPPER);
        ChestMaterial sapphire = material("othermod", "sapphire", ruby);
        ChestMaterial jade = material("othermod", "jade", ChestMaterial.COPPER);
        assertEquals(with(ChestMaterial.COPPER, jade, ruby, sapphire), order(sapphire, ruby, jade));
    }

    @Test
    public void testWithoutAfterOrUnknownAfterLast() {
        ChestMaterial ruby = material("othermod", "ruby", (ResourceLocation) null);
        ChestMaterial jade = material("othermod", "jade", ResourceLocation.fromNamespaceAndPath("missingmod", "tin"));
        assertEquals(with(ChestMaterial.NETHERITE, jade, ruby), order(ruby, jade));
    }

    @Test
    public void testLoopLast() {
        ResourceLocation rubyId = ResourceLocation.fromNamespaceAndPath("othermod", "ruby");
        ChestMaterial jade = material("othermod", "jade", rubyId);
        ChestMaterial ruby = material("othermod", "ruby", jade);
        assertEquals(with(ChestMaterial.NETHERITE, jade, ruby), order(ruby, jade));
    }

    @Test
    public void testReorderedBuiltIn() {
        // A datapack can move this mod's materials too.
        after.put(ChestMaterial.COPPER, ChestMaterial.GOLD.id());
        after.put(ChestMaterial.IRON, ChestMaterial.WOOD.id());
        assertEquals(List.of(ChestMaterial.WOOD, ChestMaterial.IRON, ChestMaterial.GOLD, ChestMaterial.COPPER, ChestMaterial.DIAMOND,
                ChestMaterial.OBSIDIAN, ChestMaterial.NETHERITE), order());
    }

    @Test
    public void testDisplayNameUsesNamespace() {
        assertEquals("material.othermod.ruby",
                ((TranslatableContents) new ChestMaterial(ResourceLocation.fromNamespaceAndPath("othermod", "ruby")).getDisplayName().getContents()).getKey());
        assertEquals("material.colossalchests2.iron", ((TranslatableContents) ChestMaterial.IRON.getDisplayName().getContents()).getKey());
    }

}
