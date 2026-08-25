package org.cyclops.colossalchests.gametest;

import com.google.common.collect.Sets;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.*;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rubensworks
 */
public class GameTestsCommon {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 0, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalPlacementDirection(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS, Direction.SOUTH);

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value(), POS.south());
            helper.assertBlockProperty(POS.south(), UncolossalChest.FACING, Direction.NORTH);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalPlacementDirectionOpposite(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS, Direction.NORTH);

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value(), POS.north());
            helper.assertBlockProperty(POS.north(), UncolossalChest.FACING, Direction.NORTH);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalHoppers(GameTestHelper helper) {
        // Place hopper above and below chest
        helper.setBlock(POS.above(), Blocks.HOPPER);
        helper.setBlock(POS, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value());
        helper.setBlock(POS.below(), Blocks.HOPPER);

        // Throw apple in hopper
        helper.spawnItem(Items.APPLE, POS.above().above());

        // Expect apple to be in final hopper
        helper.succeedWhen(() -> assertHopperContains(helper, POS.below(), new ItemStack(Items.APPLE)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood2x2(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.WOOD, 2);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.WOOD, 2));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.WOOD, 3);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.WOOD, 3));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.WOOD, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.WOOD, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.WOOD, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.WOOD, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3MissingCore(GameTestHelper helper) {
        Set<BlockPos> excluded = Sets.newHashSet(POS);
        createChest(helper, POS, ChestMaterial.WOOD, 3, excluded);

        helper.succeedWhen(() -> assertChestInvalid(helper, POS, ChestMaterial.WOOD, 3, excluded, Sets.newHashSet()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3MissingWall(GameTestHelper helper) {
        Set<BlockPos> excluded = Sets.newHashSet(POS.above());
        createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(excluded));

        helper.succeedWhen(() -> assertChestInvalid(helper, POS, ChestMaterial.WOOD, 3, excluded, Sets.newHashSet()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3MissingWallMultiple(GameTestHelper helper) {
        Set<BlockPos> excluded = Sets.newHashSet(POS.above(), POS.above().above(), POS.above().north());
        createChest(helper, POS, ChestMaterial.WOOD, 3, excluded);

        helper.succeedWhen(() -> assertChestInvalid(helper, POS, ChestMaterial.WOOD, 3, excluded, Sets.newHashSet()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalCopper5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.COPPER, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.COPPER, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalCopper9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.COPPER, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.COPPER, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalIron5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.IRON, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.IRON, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalIron9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.IRON, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.IRON, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalSilver5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.SILVER, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.SILVER, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalSilver9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.SILVER, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.SILVER, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalGold5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.GOLD, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.GOLD, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalGold9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.GOLD, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.GOLD, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalDiamond5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.DIAMOND, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.DIAMOND, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalDiamond9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.DIAMOND, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.DIAMOND, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalObsidian5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.OBSIDIAN, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.OBSIDIAN, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalObsidian9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.OBSIDIAN, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.OBSIDIAN, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalNetherite5x5(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.NETHERITE, 5);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.NETHERITE, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalNetherite9x9(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.NETHERITE, 9);

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.NETHERITE, 9));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5HopperInsert(GameTestHelper helper) {
        // For some unknown reason, this test does not work in Fabric (does work in-game only if the player is close)
        // TODO: try to re-enable later
        if (isFabric()) {
            helper.succeed();
            return;
        }

        createChest(helper, POS.above().south(), ChestMaterial.WOOD, 5);

        // Place hopper towards core
        helper.setBlock(POS.above(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.SOUTH));

        // Throw items in hopper
        helper.spawnItem(Items.APPLE, POS.above().above());
        helper.spawnItem(Items.WHITE_WOOL, POS.above().above());
        helper.spawnItem(Items.ACACIA_LEAVES, POS.above().above());

        // Expect items to be in chest
        helper.succeedWhen(() -> {
            assertCoreContains(helper, POS.above().south(), new ItemStack(Items.APPLE));
            assertCoreContains(helper, POS.above().south(), new ItemStack(Items.WHITE_WOOL));
            assertCoreContains(helper, POS.above().south(), new ItemStack(Items.ACACIA_LEAVES));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5HopperExtract(GameTestHelper helper) {
        BlockEntityColossalChest core = createChest(helper, POS.above().south(), ChestMaterial.WOOD, 5);

        // Place hopper below core
        helper.setBlock(POS.south(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.SOUTH));

        // Initialize items in core
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));
        core.getInventory().setItem(1, new ItemStack(Items.WHITE_WOOL));
        core.getInventory().setItem(2, new ItemStack(Items.ACACIA_LEAVES));

        // Expect items to be in hopper
        helper.succeedWhen(() -> {
            assertHopperContains(helper, POS.south(), new ItemStack(Items.APPLE));
            assertHopperContains(helper, POS.south(), new ItemStack(Items.WHITE_WOOL));
            assertHopperContains(helper, POS.south(), new ItemStack(Items.ACACIA_LEAVES));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5HopperInsertExtract(GameTestHelper helper) {
        // For some unknown reason, this test does not work in Fabric (does work in-game only if the player is close)
        // TODO: try to re-enable later
        if (isFabric()) {
            helper.succeed();
            return;
        }

        BlockEntityColossalChest core = createChest(helper, POS.above().south(), ChestMaterial.WOOD, 5);

        // Place hopper towards core
        helper.setBlock(POS.above(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.SOUTH));

        // Place hopper below core
        helper.setBlock(POS.south(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.SOUTH));

        // Throw items in hopper
        helper.spawnItem(Items.APPLE, POS.above().above());
        helper.spawnItem(Items.WHITE_WOOL, POS.above().above());
        helper.spawnItem(Items.ACACIA_LEAVES, POS.above().above());

        // Expect items to be in hopper
        helper.succeedWhen(() -> {
            assertHopperContains(helper, POS.south(), new ItemStack(Items.APPLE));
            assertHopperContains(helper, POS.south(), new ItemStack(Items.WHITE_WOOL));
            assertHopperContains(helper, POS.south(), new ItemStack(Items.ACACIA_LEAVES));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5Interfaces(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.WOOD, 5, Sets.newHashSet(), Sets.newHashSet(POS.north().above(), POS.south().above()));

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.WOOD, 5, Sets.newHashSet(POS.north().above(), POS.south().above()), true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalDiamond5x5Interfaces(GameTestHelper helper) {
        createChest(helper, POS, ChestMaterial.DIAMOND, 5, Sets.newHashSet(), Sets.newHashSet(POS.north().above(), POS.south().above()));

        helper.succeedWhen(() -> assertChestValid(helper, POS, ChestMaterial.DIAMOND, 5, Sets.newHashSet(POS.north().above(), POS.south().above()), true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3InterfacesMissingWall(GameTestHelper helper) {
        Set<BlockPos> excluded = Sets.newHashSet(POS.above());
        createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(excluded), Sets.newHashSet(POS.north()));

        helper.succeedWhen(() -> assertChestInvalid(helper, POS, ChestMaterial.WOOD, 3, excluded, Sets.newHashSet(POS.north())));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood5x5InterfacesHopperInsertExtract(GameTestHelper helper) {
        // For some unknown reason, this test does not work in Fabric (does work in-game only if the player is close)
        // TODO: try to re-enable later
        if (isFabric()) {
            helper.succeed();
            return;
        }

        createChest(helper, POS.above(), ChestMaterial.WOOD, 5, Sets.newHashSet(), Sets.newHashSet(POS.offset(0, 5, 0), POS.above().south()));

        // Place hopper towards top interface
        helper.setBlock(POS.offset(0, 5, 0).above(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.DOWN));

        // Place hopper below bottom interface
        helper.setBlock(POS.south(), Blocks.HOPPER
                .defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.DOWN));

        // Throw items in top hopper
        helper.spawnItem(Items.APPLE, POS.offset(0, 5, 0).above().above());
        helper.spawnItem(Items.WHITE_WOOL, POS.offset(0, 5, 0).above().above());
        helper.spawnItem(Items.ACACIA_LEAVES, POS.offset(0, 5, 0).above().above());

        // Expect items to be in hopper
        helper.succeedWhen(() -> {
            assertHopperContains(helper, POS.south(), new ItemStack(Items.APPLE));
            assertHopperContains(helper, POS.south(), new ItemStack(Items.WHITE_WOOL));
            assertHopperContains(helper, POS.south() , new ItemStack(Items.ACACIA_LEAVES));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.COPPER, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3UpgradeInsufficientItems(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockWall(), 22));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult == InteractionResult.FAIL, "Interaction must fail");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockWall(), 22));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 22));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalCopper3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.COPPER, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.IRON, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalIron3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.IRON, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.SILVER, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalSilver3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.SILVER, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.GOLD, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalGold3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.GOLD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.DIAMOND, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalDiamond3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.DIAMOND, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.OBSIDIAN, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalObsidian3x3Upgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.OBSIDIAN, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.NETHERITE.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.NETHERITE.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.NETHERITE.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.NETHERITE, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalCopper3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.COPPER, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalCopper3x3DowngradeInsufficientItems(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.COPPER, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.WOOD.getBlockWall(), 22));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult == InteractionResult.FAIL, "Interaction must fail");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.COPPER, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 22));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.WOOD.getBlockWall(), 22));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalIron3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.IRON, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.COPPER, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.COPPER.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalSilver3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.SILVER, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.IRON, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.IRON.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalGold3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.GOLD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.SILVER, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.SILVER.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalDiamond3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.DIAMOND, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.GOLD, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.GOLD.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalObsidian3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.OBSIDIAN, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.DIAMOND, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.DIAMOND.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalNetherite3x3Downgrade(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.NETHERITE, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));

        // Upgrade
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_UPGRADE_TOOL_REVERSE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
        player.getInventory().add(new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(POS.getCenter(), Direction.NORTH, helper.absolutePos(POS), false)));
        helper.assertTrue(interactionResult.indicateItemUse(), "Interaction must succeed");

        helper.succeedWhen(() -> {
            // Chest must be transformed and keep inventory
            assertChestValid(helper, POS, ChestMaterial.OBSIDIAN, 3, interfaces, false);
            assertCoreContains(helper, POS, new ItemStack(Items.APPLE));

            // Player items must be swapped
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockCore(), 1));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockInterface(), 2));
            assertPlayerInventoryNotContains(helper, player, new ItemStack(ChestMaterial.OBSIDIAN.getBlockWall(), 23));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockCore(), 1));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockInterface(), 2));
            assertPlayerInventoryContains(helper, player, new ItemStack(ChestMaterial.NETHERITE.getBlockWall(), 23));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceWall(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));

        // Break wall
        destroyBlock(helper, POS.east());

        // Replace wall
        helper.setBlock(POS.east(), ChestMaterial.WOOD.getBlockWall());

        helper.succeedWhen(() -> {
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);

            // Chest must still contain item, and there must be no dropped items on the ground
            assertCoreContains(helper, POS, new ItemStack(Items.DIAMOND));
            helper.assertItemEntityNotPresent(Items.DIAMOND);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceWallAsPlayer(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));

        // Break wall
        destroyBlock(helper, POS.east());

        // Replace wall
        setBlockAsPlayer(helper, POS.east(), ChestMaterial.WOOD.getBlockWall());

        helper.succeedWhen(() -> {
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);

            // Chest must still contain item, and there must be no dropped items on the ground
            assertCoreContains(helper, POS, new ItemStack(Items.DIAMOND));
            helper.assertItemEntityNotPresent(Items.DIAMOND);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceInterface(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));

        // Break interface
        destroyBlock(helper, POS.east().above());

        // Replace interface
        helper.setBlock(POS.east().above(), ChestMaterial.WOOD.getBlockInterface());

        helper.succeedWhen(() -> {
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);

            // Chest must still contain item, and there must be no dropped items on the ground
            assertCoreContains(helper, POS, new ItemStack(Items.DIAMOND));
            helper.assertItemEntityNotPresent(Items.DIAMOND);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceInterfaceAsPlayer(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS, ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));

        // Break interface
        destroyBlock(helper, POS.east().above());

        // Replace interface
        setBlockAsPlayer(helper, POS.east().above(), ChestMaterial.WOOD.getBlockInterface());

        helper.succeedWhen(() -> {
            assertChestValid(helper, POS, ChestMaterial.WOOD, 3, interfaces, false);

            // Chest must still contain item, and there must be no dropped items on the ground
            assertCoreContains(helper, POS, new ItemStack(Items.DIAMOND));
            helper.assertItemEntityNotPresent(Items.DIAMOND);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceCore(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.offset(4, 1, 4).east().above(), POS.offset(4, 1, 4).south().above());
        BlockEntityColossalChest core = createChest(helper, POS.offset(4, 1, 4), ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        core.getInventory().setItem(1, new ItemStack(Items.ACACIA_LEAVES));
        core.getInventory().setItem(2, new ItemStack(Items.WHITE_WOOL));

        // Break core
        destroyBlock(helper, POS.offset(4, 1, 4));

        // Replace core
        helper.setBlock(POS.offset(4, 1, 4), ChestMaterial.WOOD.getBlockCore());

        helper.succeedWhen(() -> {
            // Chest must not contain item, and all items must be dropped on the ground
            assertChestValid(helper, POS.offset(4, 1, 4), ChestMaterial.WOOD, 3, interfaces, true);
            helper.assertItemEntityPresent(Items.DIAMOND);
            helper.assertItemEntityPresent(Items.ACACIA_LEAVES);
            helper.assertItemEntityPresent(Items.WHITE_WOOL);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3BreakPlaceCoreAsPlayer(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.offset(4, 1, 4).east().above(), POS.offset(4, 1, 4).south().above());
        BlockEntityColossalChest core = createChest(helper, POS.offset(4, 1, 4), ChestMaterial.WOOD, 3, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        core.getInventory().setItem(1, new ItemStack(Items.ACACIA_LEAVES));
        core.getInventory().setItem(2, new ItemStack(Items.WHITE_WOOL));

        // Break core
        destroyBlock(helper, POS.offset(4, 1, 4));

        // Replace core
        setBlockAsPlayer(helper, POS.offset(4, 1, 4), ChestMaterial.WOOD.getBlockCore());

        helper.succeedWhen(() -> {
            // Chest must not contain item, and all items must be dropped on the ground
            assertChestValid(helper, POS.offset(4, 1, 4), ChestMaterial.WOOD, 3, interfaces, true);
            helper.assertItemEntityPresent(Items.DIAMOND);
            helper.assertItemEntityPresent(Items.ACACIA_LEAVES);
            helper.assertItemEntityPresent(Items.WHITE_WOOL);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalWood3x3DestroyTnt(GameTestHelper helper) {
        HashSet<BlockPos> interfaces = Sets.newHashSet(POS.east().above(), POS.south().above());
        BlockEntityColossalChest core = createChest(helper, POS.above(), ChestMaterial.WOOD, 2, Sets.newHashSet(), interfaces);

        // Insert item
        core.getInventory().setItem(0, new ItemStack(Items.APPLE));
        core.getInventory().setItem(1, new ItemStack(Items.ACACIA_LEAVES));

        // Destroy chest with TNT
        helper.setBlock(POS.above().offset(2, 0, 0), Blocks.TNT);
        helper.setBlock(POS.above().offset(2, 1, 0), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            // Chest and contents must be dropped
            helper.assertItemEntityPresent(Items.APPLE);
            helper.assertItemEntityPresent(Items.ACACIA_LEAVES);
            helper.assertItemEntityPresent(ChestMaterial.WOOD.getBlockCore().asItem());
            helper.assertItemEntityPresent(ChestMaterial.WOOD.getBlockInterface().asItem());
            helper.assertItemEntityPresent(ChestMaterial.WOOD.getBlockWall().asItem());
        });
    }

    protected BlockEntityColossalChest createChest(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension) {
        return this.createChest(helper, pos, material, dimension, Sets.newHashSet());
    }

    protected BlockEntityColossalChest createChest(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension, Set<BlockPos> exclude) {
        return createChest(helper, pos, material, dimension, exclude, Sets.newHashSet());
    }

    protected BlockEntityColossalChest createChest(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension, Set<BlockPos> exclude, Set<BlockPos> interfaces) {
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    BlockPos poso = pos.offset(x, y, z);
                    if (!exclude.contains(poso)) {
                        if (x == 0 && y == 0 && z == 0) {
                            helper.setBlock(poso, material.getBlockCore());
                        } else if (x == 0 || y == 0 || z == 0 || x == dimension - 1 || y == dimension - 1 || z == dimension - 1) {
                            if (interfaces.contains(poso)) {
                                helper.setBlock(poso, material.getBlockInterface());
                            } else {
                                helper.setBlock(poso, material.getBlockWall());
                            }
                        }
                    }
                }
            }
        }
        return exclude.contains(pos) ? null : helper.getBlockEntity(pos);
    }

    protected int chestSize(int dimension, ChestMaterial material) {
        return (int) Math.ceil((Math.pow(dimension, 3) * 27) * material.getInventoryMultiplier() / 9) * 9;
    }

    protected void assertChestValid(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension) {
        assertChestValid(helper, pos, material, dimension, Sets.newHashSet(), true);
    }

    protected void assertChestValid(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension, Set<BlockPos> interfaces, boolean mustBeEmpty) {
        int inventorySize = chestSize(dimension, material);
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    BlockPos poso = pos.offset(x, y, z);
                    if (x == 0 && y == 0 && z == 0) {
                        helper.assertBlockPresent(material.getBlockCore(), poso);
                        helper.assertBlockProperty(poso, ColossalChest.ENABLED, true);
                        helper.assertBlockEntityData(poso, (BlockEntityColossalChest be) -> be.getInventory().getContainerSize() == inventorySize, () -> "Inventory is not of size " + inventorySize);
                        if (mustBeEmpty) {
                            helper.assertBlockEntityData(poso, (BlockEntityColossalChest be) -> be.getInventory().isEmpty(), () -> "Inventory is not empty");
                        }
                    } else if (x == 0 || y == 0 || z == 0 || x == dimension - 1 || y == dimension - 1 || z == dimension - 1) {
                        if (interfaces.contains(poso)) {
                            helper.assertBlockPresent(material.getBlockInterface(), poso);
                            helper.assertBlockProperty(poso, Interface.ENABLED, true);
                        } else {
                            helper.assertBlockPresent(material.getBlockWall(), poso);
                            helper.assertBlockProperty(poso, ChestWall.ENABLED, true);
                        }
                    }
                }
            }
        }
    }

    protected void assertChestInvalid(GameTestHelper helper, BlockPos pos, ChestMaterial material, int dimension, Set<BlockPos> exclude, Set<BlockPos> interfaces) {
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    BlockPos poso = pos.offset(x, y, z);
                    if (exclude.contains(poso)) {
                        helper.assertBlockPresent(Blocks.AIR, poso);
                    } else {
                        if (x == 0 && y == 0 && z == 0) {
                            helper.assertBlockPresent(material.getBlockCore(), poso);
                            helper.assertBlockProperty(poso, ColossalChest.ENABLED, false);
                            helper.assertBlockEntityData(poso, (BlockEntityColossalChest be) -> be.getInventory().getContainerSize() == 0, () -> "Inventory is not of size zero");
                            helper.assertBlockEntityData(poso, (BlockEntityColossalChest be) -> be.getInventory().isEmpty(), () -> "Inventory is not empty");
                        } else if (x == 0 || y == 0 || z == 0 || x == dimension - 1 || y == dimension - 1 || z == dimension - 1) {
                            if (interfaces.contains(poso)) {
                                helper.assertBlockPresent(material.getBlockInterface(), poso);
                                helper.assertBlockProperty(poso, Interface.ENABLED, false);
                            } else {
                                helper.assertBlockPresent(material.getBlockWall(), poso);
                                helper.assertBlockProperty(poso, ChestWall.ENABLED, false);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void assertHopperContains(GameTestHelper helper, BlockPos pos, ItemStack itemStack) {
        helper.assertBlockEntityData(pos, (HopperBlockEntity be) -> {
            for (int i = 0; i < be.getContainerSize(); i++) {
                if (ItemStack.isSameItemSameComponents(be.getItem(i), itemStack)) {
                    return true;
                }
            }
            return false;
        }, () -> "Hopper does not contain item");
    }

    protected void assertCoreContains(GameTestHelper helper, BlockPos pos, ItemStack itemStack) {
        helper.assertBlockEntityData(pos, (BlockEntityColossalChest be) -> {
            for (int i = 0; i < be.getInventory().getContainerSize(); i++) {
                if (ItemStack.isSameItemSameComponents(be.getInventory().getItem(i), itemStack)) {
                    return true;
                }
            }
            return false;
        }, () -> "Colossal chest core does not contain item");
    }

    protected void assertPlayerInventoryContains(GameTestHelper helper, Player player, ItemStack itemStack) {
        boolean contains = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (ItemStack.isSameItemSameComponents(player.getInventory().getItem(i), itemStack)) {
                contains = true;
                break;
            }
        }
        helper.assertTrue(contains, "Player does not contain item");
    }

    protected void assertPlayerInventoryNotContains(GameTestHelper helper, Player player, ItemStack itemStack) {
        boolean contains = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (ItemStack.isSameItemSameComponents(player.getInventory().getItem(i), itemStack)) {
                contains = true;
                break;
            }
        }
        helper.assertFalse(contains, "Player does contain item");
    }

    protected void destroyBlock(GameTestHelper helper, BlockPos pos) {
        BlockState blockState = helper.getBlockState(pos);
        boolean removed = helper.getLevel().removeBlock(helper.absolutePos(pos), false);
        if (removed) {
            blockState.getBlock().destroy(helper.getLevel(), helper.absolutePos(pos), blockState);
        }
    }

    private void setBlockAsPlayer(GameTestHelper helper, BlockPos pos, Block block) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(block);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        InteractionResult interactionResult = itemStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(pos.getCenter(), Direction.DOWN, helper.absolutePos(pos), false)));
        helper.assertTrue(interactionResult.consumesAction(), "Block placement as player failed");
    }

    protected boolean isFabric() {
        try {
            Class.forName("net.fabricmc.fabric.api.transfer.v1.item.ItemStorage");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRecipeChestWallNetherite(GameTestHelper helper) {
        assertRecipeResult(helper, RecipeType.SMITHING, new SmithingRecipeInput(
                new ItemStack(Items.GOLD_INGOT),
                new ItemStack(ChestMaterial.DIAMOND.getBlockWall()),
                new ItemStack(Items.NETHERITE_SCRAP)
        ), new ItemStack(ChestMaterial.NETHERITE.getBlockWall()));

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRecipeColossalChestNetherite(GameTestHelper helper) {
        assertRecipeResult(helper, RecipeType.CRAFTING, CraftingInput.of(2, 1, List.of(
                new ItemStack(ChestMaterial.NETHERITE.getBlockWall()),
                new ItemStack(Items.IRON_INGOT)
        )), new ItemStack(ChestMaterial.NETHERITE.getBlockCore()));

        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRecipeInterfaceNetherite(GameTestHelper helper) {
        ItemStack cobblestone = new ItemStack(Items.COBBLESTONE);
        assertRecipeResult(helper, RecipeType.CRAFTING, CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, cobblestone, ItemStack.EMPTY,
                cobblestone, new ItemStack(ChestMaterial.NETHERITE.getBlockWall()), cobblestone,
                ItemStack.EMPTY, cobblestone, ItemStack.EMPTY
        )), new ItemStack(ChestMaterial.NETHERITE.getBlockInterface()));

        helper.succeed();
    }

    private <I extends RecipeInput, T extends Recipe<I>> void assertRecipeResult(GameTestHelper helper, RecipeType<T> recipeType,
                                                                                I input, ItemStack expected) {
        ItemStack result = helper.getLevel().getRecipeManager()
                .getRecipeFor(recipeType, input, helper.getLevel())
                .map(recipe -> recipe.value().assemble(input, helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(ItemStack.isSameItem(result, expected),
                "Expected recipe result " + expected.getItem() + ", but got " + result.getItem());
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRootNegative(GameTestHelper helper) {
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        serverPlayer.getInventory().add(new ItemStack(Items.DIRT));
        CriteriaTriggers.INVENTORY_CHANGED.trigger(serverPlayer, serverPlayer.getInventory(), new ItemStack(Items.DIRT));

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, serverPlayer, ResourceLocation.parse("colossalchests:root")));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRoot(GameTestHelper helper) {
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        serverPlayer.getInventory().add(new ItemStack(Items.CHEST));
        CriteriaTriggers.INVENTORY_CHANGED.trigger(serverPlayer, serverPlayer.getInventory(), new ItemStack(Items.CHEST));

        helper.succeedWhen(() -> assertAdvancementDone(helper, serverPlayer, ResourceLocation.parse("colossalchests:root")));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementUncolossalNegative(GameTestHelper helper) {
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, POS, Blocks.CHEST);

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, serverPlayer, ResourceLocation.parse("colossalchests:uncolossal")));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementUncolossal(GameTestHelper helper) {
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, POS, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value());

        helper.succeedWhen(() -> assertAdvancementDone(helper, serverPlayer, ResourceLocation.parse("colossalchests:uncolossal")));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseWoodNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.COPPER, "colossalchests:base/wood");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseCopperNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/copper");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseIronNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/iron");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseSilverNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/silver");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseGoldNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/gold");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseDiamondNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/diamond");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseObsidianNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/obsidian");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseNetheriteNegative(GameTestHelper helper) {
        testAdvancementBaseNegative(helper, ChestMaterial.WOOD, "colossalchests:base/netherite");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseWood(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.WOOD, "colossalchests:base/wood");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseCopper(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.COPPER, "colossalchests:base/copper");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseIron(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.IRON, "colossalchests:base/iron");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseSilver(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.SILVER, "colossalchests:base/silver");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseGold(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.GOLD, "colossalchests:base/gold");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseDiamond(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.DIAMOND, "colossalchests:base/diamond");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseObsidian(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.OBSIDIAN, "colossalchests:base/obsidian");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementBaseNetherite(GameTestHelper helper) {
        testAdvancementBase(helper, ChestMaterial.NETHERITE, "colossalchests:base/netherite");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeWoodNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.WOOD, "colossalchests:size/wood");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeCopperNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.COPPER, "colossalchests:size/copper");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeIronNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.IRON, "colossalchests:size/iron");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeSilverNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.SILVER, "colossalchests:size/silver");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeGoldNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.GOLD, "colossalchests:size/gold");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeDiamondNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.DIAMOND, "colossalchests:size/diamond");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeObsidianNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.OBSIDIAN, "colossalchests:size/obsidian");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeNetheriteNegative(GameTestHelper helper) {
        testAdvancementSizeNegative(helper, ChestMaterial.NETHERITE, "colossalchests:size/netherite");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeWood(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.WOOD, "colossalchests:size/wood");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeCopper(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.COPPER, "colossalchests:size/copper");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeIron(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.IRON, "colossalchests:size/iron");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeSilver(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.SILVER, "colossalchests:size/silver");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeGold(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.GOLD, "colossalchests:size/gold");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeDiamond(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.DIAMOND, "colossalchests:size/diamond");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeObsidian(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.OBSIDIAN, "colossalchests:size/obsidian");
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementSizeNetherite(GameTestHelper helper) {
        testAdvancementSize(helper, ChestMaterial.NETHERITE, "colossalchests:size/netherite");
    }

    private void testAdvancementBaseNegative(GameTestHelper helper, ChestMaterial material, String advancementId) {
        BlockPos excludedWall = POS.offset(1, 0, 0);
        createChest(helper, POS, material, 2, Sets.newHashSet(excludedWall));
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, excludedWall, material.getBlockWall());

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, serverPlayer, ResourceLocation.parse(advancementId)));
    }

    private void testAdvancementSizeNegative(GameTestHelper helper, ChestMaterial material, String advancementId) {
        BlockPos excludedWall = POS.offset(1, 0, 0);
        createChest(helper, POS, material, 2, Sets.newHashSet(excludedWall));
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, excludedWall, material.getBlockWall());

        helper.succeedWhen(() -> assertAdvancementNotDone(helper, serverPlayer, ResourceLocation.parse(advancementId)));
    }

    private void testAdvancementBase(GameTestHelper helper, ChestMaterial material, String advancementId) {
        BlockPos excludedWall = POS.offset(1, 0, 0);
        createChest(helper, POS, material, 2, Sets.newHashSet(excludedWall));
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, excludedWall, material.getBlockWall());

        helper.succeedWhen(() -> assertAdvancementDone(helper, serverPlayer, ResourceLocation.parse(advancementId)));
    }

    private void testAdvancementSize(GameTestHelper helper, ChestMaterial material, String advancementId) {
        BlockPos chestStart = BlockPos.ZERO;
        BlockPos excludedWall = chestStart.offset(9, 0, 0);
        createChest(helper, chestStart, material, 10, Sets.newHashSet(excludedWall));
        ServerPlayer serverPlayer = makeMockServerPlayer(helper);
        placeBlockAsServerPlayer(helper, serverPlayer, excludedWall, material.getBlockWall());

        helper.succeedWhen(() -> assertAdvancementDone(helper, serverPlayer, ResourceLocation.parse(advancementId)));
    }

    @SuppressWarnings("removal")
    private ServerPlayer makeMockServerPlayer(GameTestHelper helper) {
        return helper.makeMockServerPlayerInLevel();
    }

    private void placeBlockAsServerPlayer(GameTestHelper helper, ServerPlayer serverPlayer, BlockPos pos, Block block) {
        ItemStack itemStack = new ItemStack(block);
        serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        itemStack.useOn(new UseOnContext(serverPlayer, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.DOWN, helper.absolutePos(pos), false)));
    }

    private void assertAdvancementNotDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(advancementId);
        helper.assertTrue(holder != null, "Advancement " + advancementId + " not found");
        helper.assertTrue(
                !player.getAdvancements().getOrStartProgress(holder).isDone(),
                "Advancement " + advancementId + " should not have been obtained");
    }

    private void assertAdvancementDone(GameTestHelper helper, ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(advancementId);
        helper.assertTrue(holder != null, "Advancement " + advancementId + " not found");
        helper.assertTrue(
                player.getAdvancements().getOrStartProgress(holder).isDone(),
                "Advancement " + advancementId + " has not been obtained");
    }

}
