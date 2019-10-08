package org.cyclops.colossalchests;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.colossalchests.tileentity.TileInterface;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder("minecraft:chest")
    public static final Item ITEM_CHEST = null;

    @ObjectHolder("colossalchests:uncolossal_chest")
    public static final Block BLOCK_UNCOLOSSAL_CHEST = null;

    @ObjectHolder("colossalchests:colossal_chest")
    public static final TileEntityType<TileColossalChest> TILE_ENTITY_COLOSSAL_CHEST = null;
    @ObjectHolder("colossalchests:interface")
    public static final TileEntityType<TileInterface> TILE_ENTITY_INTERFACE = null;
    @ObjectHolder("colossalchests:uncolossal_chest")
    public static final TileEntityType<TileColossalChest> TILE_ENTITY_UNCOLOSSAL_CHEST = null;

    @ObjectHolder("colossalchests:colossal_chest")
    public static final ContainerType<ContainerColossalChest> CONTAINER_COLOSSAL_CHEST = null;
    @ObjectHolder("colossalchests:uncolossal_chest")
    public static final ContainerType<ContainerUncolossalChest> CONTAINER_UNCOLOSSAL_CHEST = null;

}
