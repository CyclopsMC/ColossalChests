package org.cyclops.colossalchests;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder(registryName = "item", value = "minecraft:chest")
    public static final Item ITEM_CHEST = null;

    @ObjectHolder(registryName = "block", value = "colossalchests:uncolossal_chest")
    public static final Block BLOCK_UNCOLOSSAL_CHEST = null;

    @ObjectHolder(registryName = "block_entity_type", value = "colossalchests:colossal_chest")
    public static final BlockEntityType<BlockEntityColossalChest> BLOCK_ENTITY_COLOSSAL_CHEST = null;
    @ObjectHolder(registryName = "block_entity_type", value = "colossalchests:interface")
    public static final BlockEntityType<BlockEntityInterface> BLOCK_ENTITY_INTERFACE = null;
    @ObjectHolder(registryName = "block_entity_type", value = "colossalchests:uncolossal_chest")
    public static final BlockEntityType<BlockEntityUncolossalChest> BLOCK_ENTITY_UNCOLOSSAL_CHEST = null;

    @ObjectHolder(registryName = "menu", value = "colossalchests:colossal_chest")
    public static final MenuType<ContainerColossalChest> CONTAINER_COLOSSAL_CHEST = null;
    @ObjectHolder(registryName = "menu", value = "colossalchests:uncolossal_chest")
    public static final MenuType<ContainerUncolossalChest> CONTAINER_UNCOLOSSAL_CHEST = null;

}
