package org.cyclops.colossalchests2.capability;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.api.IDeepItemStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Block API lookups added by this mod on Fabric.
 * @author rubensworks
 */
public class CapabilitiesFabric {

    public static final BlockApiLookup<IDeepItemStorage, @Nullable Direction> DEEP_ITEM_STORAGE =
            BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "deep_item_storage"), IDeepItemStorage.class, Direction.class);

}
