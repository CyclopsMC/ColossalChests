package org.cyclops.colossalchests2.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Cores with contents are refused through game tests, which have the mod's item components.
 * @author rubensworks
 */
public class TestNestedChests extends BootstrapTest {

    private static ItemStack shulkerWith(ItemStack... contents) {
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(contents)));
        return shulker;
    }

    @Test
    public void testPlainItems() {
        assertTrue(NestedChests.canStore(new ItemStack(Items.STONE)));
        assertTrue(NestedChests.canStore(new ItemStack(Items.SHULKER_BOX)));
    }

    @Test
    public void testContainers() {
        assertTrue(NestedChests.canStore(shulkerWith(new ItemStack(Items.STONE, 64))));
        ItemStack bundle = new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(shulkerWith(new ItemStack(Items.STONE)))));
        assertTrue(NestedChests.canStore(bundle));
    }

    @Test
    public void testTooDeep() {
        ItemStack stack = new ItemStack(Items.STONE);
        for (int i = 0; i < 9; i++) {
            stack = shulkerWith(stack);
        }
        assertFalse(NestedChests.canStore(stack));
    }
}
