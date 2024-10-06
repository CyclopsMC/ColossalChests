package org.cyclops.colossalchests;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests.advancement.criterion.ChestFormedTrigger;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.config.DeferredHolderCommon;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolderCommon<Item, Item> ITEM_CHEST = DeferredHolderCommon.create(Registries.ITEM, ResourceLocation.parse("minecraft:chest"));

    public static final DeferredHolderCommon<Block, Block> BLOCK_UNCOLOSSAL_CHEST = DeferredHolderCommon.create(Registries.BLOCK, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityColossalChest>> BLOCK_ENTITY_COLOSSAL_CHEST = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:colossal_chest"));
    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityInterface>> BLOCK_ENTITY_INTERFACE = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:interface"));
    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityUncolossalChest>> BLOCK_ENTITY_UNCOLOSSAL_CHEST = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerColossalChest>> CONTAINER_COLOSSAL_CHEST = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests:colossal_chest"));
    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerUncolossalChest>> CONTAINER_UNCOLOSSAL_CHEST = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests:uncolossal_chest"));

    public static final DeferredHolderCommon<CriterionTrigger<?>, ChestFormedTrigger> TRIGGER_CHEST_FORMED = DeferredHolderCommon.create(Registries.TRIGGER_TYPE, ResourceLocation.parse("colossalchests:chest_formed"));

}
