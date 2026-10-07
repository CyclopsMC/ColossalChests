package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestMaterial {

    private static ChestMaterial material(String namespace, String path, ChestMaterial after) {
        return material(namespace, path, after == null ? null : after.id());
    }

    private static ChestMaterial material(String namespace, String path, ResourceLocation after) {
        return new ChestMaterial(ResourceLocation.fromNamespaceAndPath(namespace, path), SoundType.METAL, 1, true, 1, after, MaterialProperties.DEFAULT);
    }

    private static List<ChestMaterial> order(ChestMaterial... added) {
        return ChestMaterial.order(ImmutableList.<ChestMaterial>builder().addAll(ChestMaterial.BUILT_IN).add(added).build());
    }

    private static List<ChestMaterial> with(ChestMaterial after, ChestMaterial... inserted) {
        List<ChestMaterial> expected = new ArrayList<>(ChestMaterial.BUILT_IN);
        expected.addAll(expected.indexOf(after) + 1, List.of(inserted));
        return expected;
    }

    @Test
    public void testBuiltInOrder() {
        assertEquals(ChestMaterial.BUILT_IN, ChestMaterial.order(ChestMaterial.BUILT_IN));
        assertEquals(ChestMaterial.BUILT_IN, ChestMaterial.getAll().stream().filter(ChestMaterial.BUILT_IN::contains).toList());
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
    public void testRegister() {
        ChestMaterial ruby = material("othermod", "material_test_ruby", ChestMaterial.DIAMOND);
        ChestMaterial.register(ruby);
        assertEquals(ChestMaterial.getAll().indexOf(ChestMaterial.DIAMOND) + 1, ChestMaterial.getAll().indexOf(ruby));
        assertEquals(ruby, ChestMaterial.byId(ruby.id()).orElseThrow());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDuplicate() {
        ChestMaterial.register(ChestMaterial.IRON);
    }

    @Test
    public void testDisplayNameUsesNamespace() {
        assertEquals("material.othermod.ruby",
                ((TranslatableContents) material("othermod", "ruby", ChestMaterial.COPPER).getDisplayName().getContents()).getKey());
        assertEquals("material.colossalchests2.iron", ((TranslatableContents) ChestMaterial.IRON.getDisplayName().getContents()).getKey());
    }

}
