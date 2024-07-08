package org.cyclops.colossalchests;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.cyclops.colossalchests.advancement.criterion.ChestFormedTrigger;
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

    public static final DeferredHolder<Item, Item> ITEM_CHEST = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("minecraft:chest"));

    public static final DeferredHolder<Block, Block> BLOCK_UNCOLOSSAL_CHEST = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityColossalChest>> BLOCK_ENTITY_COLOSSAL_CHEST = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:colossal_chest"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityInterface>> BLOCK_ENTITY_INTERFACE = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:interface"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityUncolossalChest>> BLOCK_ENTITY_UNCOLOSSAL_CHEST = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerColossalChest>> CONTAINER_COLOSSAL_CHEST = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("colossalchests:colossal_chest"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerUncolossalChest>> CONTAINER_UNCOLOSSAL_CHEST = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolder<CriterionTrigger<?>, ChestFormedTrigger> TRIGGER_CHEST_FORMED = DeferredHolder.create(Registries.TRIGGER_TYPE, ResourceLocation.parse("colossalchests:chest_formed"));

}
