package org.cyclops.colossalchests2;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.cyclopscore.config.DeferredHolderCommon;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolderCommon<Item, Item> ITEM_CHEST = DeferredHolderCommon.create(Registries.ITEM, ResourceLocation.parse("minecraft:chest"));

    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityChestCore>> BLOCK_ENTITY_CHEST_CORE = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests2:chest_core"));

    public static final DeferredHolderCommon<DataComponentType<?>, DataComponentType<ChestStorage.Contents>> COMPONENT_CHEST_CONTENTS = DeferredHolderCommon.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("colossalchests2:chest_contents"));

}
