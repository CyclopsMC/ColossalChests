package org.cyclops.colossalchests2.capability;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.api.IDeepItemStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Capabilities added by this mod on NeoForge.
 * @author rubensworks
 */
public class CapabilitiesNeoForge {

    public static final BlockCapability<IDeepItemStorage, @Nullable Direction> DEEP_ITEM_STORAGE =
            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "deep_item_storage"), IDeepItemStorage.class);

}
