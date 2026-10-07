package org.cyclops.colossalchests2;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.advancement.ChestFormedTrigger;
import org.cyclops.colossalchests2.advancement.MaterialChangedTrigger;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChest;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.inventory.ContainerInterface;
import org.cyclops.colossalchests2.inventory.ContainerMagnet;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeTool;
import org.cyclops.colossalchests2.inventory.ContainerRedstone;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.cyclopscore.config.DeferredHolderCommon;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolderCommon<Item, Item> ITEM_CHEST = DeferredHolderCommon.create(Registries.ITEM, ResourceLocation.parse("minecraft:chest"));

    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityChestCore>> BLOCK_ENTITY_CHEST_CORE = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests2:chest_core"));

    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityChestWall>> BLOCK_ENTITY_CHEST_WALL = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests2:chest_wall"));

    public static final DeferredHolderCommon<CriterionTrigger<?>, ChestFormedTrigger> TRIGGER_CHEST_FORMED = DeferredHolderCommon.create(Registries.TRIGGER_TYPE, ResourceLocation.parse("colossalchests2:chest_formed"));

    public static final DeferredHolderCommon<CriterionTrigger<?>, MaterialChangedTrigger> TRIGGER_MATERIAL_CHANGED = DeferredHolderCommon.create(Registries.TRIGGER_TYPE, ResourceLocation.parse("colossalchests2:material_changed"));

    public static final DeferredHolderCommon<Block, Block> BLOCK_UNCOLOSSAL_CHEST = DeferredHolderCommon.create(Registries.BLOCK, ResourceLocation.parse("colossalchests2:uncolossal_chest"));

    public static final DeferredHolderCommon<BlockEntityType<?>, BlockEntityType<BlockEntityUncolossalChest>> BLOCK_ENTITY_UNCOLOSSAL_CHEST = DeferredHolderCommon.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("colossalchests2:uncolossal_chest"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerInterface>> MENU_INTERFACE = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:interface"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerMagnet>> MENU_MAGNET = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:magnet"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerRedstone>> MENU_REDSTONE = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:redstone"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerDisplay>> MENU_DISPLAY = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:display"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerMaterialUpgradeTool>> MENU_MATERIAL_UPGRADE_TOOL = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:material_upgrade_tool"));

    public static final DeferredHolderCommon<MenuType<?>, MenuType<ContainerChest>> MENU_CHEST = DeferredHolderCommon.create(Registries.MENU, ResourceLocation.parse("colossalchests2:chest"));

    public static final DeferredHolderCommon<DataComponentType<?>, DataComponentType<ItemContainerContents>> COMPONENT_CHEST_UPGRADES = DeferredHolderCommon.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("colossalchests2:chest_upgrades"));

    public static final DeferredHolderCommon<DataComponentType<?>, DataComponentType<ChestStorage.Contents>> COMPONENT_CHEST_CONTENTS = DeferredHolderCommon.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("colossalchests2:chest_contents"));

    public static final DeferredHolderCommon<DataComponentType<?>, DataComponentType<ResourceLocation>> COMPONENT_MATERIAL_TARGET = DeferredHolderCommon.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("colossalchests2:material_target"));

}
