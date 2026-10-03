package org.cyclops.colossalchests2.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.capability.LoaderCapabilities;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.storage.ChestStorage;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author rubensworks
 */
public class GameTestsCommon {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 0, 1);

    private static final BlockPos MIN_A = new BlockPos(1, 1, 1);
    private static final BlockPos MIN_B = new BlockPos(5, 1, 5);
    private static final ItemStack STONE = new ItemStack(Items.STONE);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testHarness(GameTestHelper helper) {
        // Proves the mod's template and classes load on this loader.
        helper.setBlock(POS, Blocks.CHEST);
        helper.assertBlockPresent(Blocks.CHEST, POS);
        helper.assertValueEqual(ChestTablesLoader.get().materials().size(), ChestTables.DEFAULT.materials().size(), "material count");
        helper.succeed();
    }

    // Helpers

    public static Block wall(ChestMaterial material) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_wall_" + material.getName()));
    }

    public static Block core(ChestMaterial material) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_core_" + material.getName()));
    }

    /**
     * Build the walls of a cube, leaving out the core position and any skipped positions.
     * @return The relative core position.
     */
    public static BlockPos buildWalls(GameTestHelper helper, BlockPos min, int size, ChestMaterial material, BlockPos coreOffset, Set<BlockPos> skipped) {
        BlockPos corePos = min.offset(coreOffset);
        for (BlockPos pos : new ChestStructure(min, size).shell()) {
            if (!pos.equals(corePos) && !skipped.contains(pos)) {
                helper.setBlock(pos, wall(material));
            }
        }
        return corePos;
    }

    /**
     * Build a full chest with its core on the middle of the side facing negative z.
     * @return The relative core position.
     */
    public static BlockPos buildChest(GameTestHelper helper, BlockPos min, int size, ChestMaterial material) {
        BlockPos corePos = buildWalls(helper, min, size, material, new BlockPos(size / 2, size / 2, 0), Set.of());
        helper.setBlock(corePos, core(material));
        return corePos;
    }

    public static BlockEntityChestCore getCore(GameTestHelper helper, BlockPos corePos) {
        BlockEntity blockEntity = helper.getBlockEntity(corePos);
        if (!(blockEntity instanceof BlockEntityChestCore core)) {
            throw new IllegalStateException("No chest core at " + corePos);
        }
        return core;
    }

    public static void assertFormed(GameTestHelper helper, BlockPos corePos, BlockPos min, int size) {
        BlockEntityChestCore core = getCore(helper, corePos);
        helper.assertTrue(core.isFormed(), "Expected the chest to be formed");
        helper.assertValueEqual(core.getStructure(), new ChestStructure(helper.absolutePos(min), size), "structure");
        helper.assertBlockProperty(corePos, BlockChestCore.FORMED, true);
        helper.assertBlockProperty(min, BlockChestWall.FORMED, true);
        helper.assertBlockProperty(min.offset(size - 1, size - 1, size - 1), BlockChestWall.FORMED, true);
    }

    public static void assertDormant(GameTestHelper helper, BlockPos corePos) {
        BlockEntityChestCore core = getCore(helper, corePos);
        helper.assertFalse(core.isFormed(), "Expected the chest to be dormant");
        helper.assertBlockProperty(corePos, BlockChestCore.FORMED, false);
    }

    /**
     * @return Where a hopper must point into: a wall where the loader supports it, the core otherwise.
     */
    public static BlockPos getHopperTarget(BlockPos corePos, BlockPos wallPos) {
        return LoaderCapabilities.blockCapabilitiesSupported ? wallPos : corePos;
    }

    private static ItemStack breakCoreAndPickUp(GameTestHelper helper, BlockPos corePos) {
        // Unlike GameTestHelper#destroyBlock, this runs the loot table like a player breaking the block.
        BlockPos absolute = helper.absolutePos(corePos);
        helper.getLevel().destroyBlock(absolute, true);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(absolute).inflate(2));
        helper.assertValueEqual(items.size(), 1, "dropped item entities");
        ItemStack stack = items.get(0).getItem().copy();
        items.get(0).discard();
        return stack;
    }

    private static void placeCore(GameTestHelper helper, ItemStack stack, BlockPos corePos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, corePos.below(), Direction.UP);
    }

    // Formation

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFormWood3x3(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.succeedWhen(() -> assertFormed(helper, corePos, MIN_A, 3));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFormWood2x2(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 2, ChestMaterial.WOOD);
        helper.succeedWhen(() -> assertFormed(helper, corePos, MIN_A, 2));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFormCoreFirst(GameTestHelper helper) {
        BlockPos corePos = MIN_A.offset(1, 1, 0);
        helper.setBlock(corePos, core(ChestMaterial.WOOD));
        helper.runAfterDelay(5, () -> buildWalls(helper, MIN_A, 3, ChestMaterial.WOOD, new BlockPos(1, 1, 0), Set.of()));
        helper.succeedWhen(() -> assertFormed(helper, corePos, MIN_A, 3));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNotFormedMissingWall(GameTestHelper helper) {
        BlockPos corePos = buildWalls(helper, MIN_A, 3, ChestMaterial.WOOD, new BlockPos(1, 1, 0), Set.of(MIN_A.offset(2, 2, 2)));
        helper.setBlock(corePos, core(ChestMaterial.WOOD));
        helper.runAfterDelay(10, () -> {
            assertDormant(helper, corePos);
            helper.assertBlockProperty(MIN_A, BlockChestWall.FORMED, false);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNotFormedOversize(GameTestHelper helper) {
        // Wood chests are limited to size 3 by the shipped material data.
        BlockPos corePos = buildChest(helper, MIN_A, 4, ChestMaterial.WOOD);
        helper.runAfterDelay(10, () -> {
            assertDormant(helper, corePos);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFormIron5x5(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 5, ChestMaterial.IRON);
        helper.succeedWhen(() -> assertFormed(helper, corePos, MIN_A, 5));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdjacentChestsBothForm(GameTestHelper helper) {
        BlockPos coreA = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos minNext = MIN_A.offset(3, 0, 0);
        BlockPos coreNext = buildChest(helper, minNext, 3, ChestMaterial.WOOD);
        helper.succeedWhen(() -> {
            assertFormed(helper, coreA, MIN_A, 3);
            assertFormed(helper, coreNext, minNext, 3);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBlockInsideBreaksStructure(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> helper.setBlock(MIN_A.offset(1, 1, 1), Blocks.STONE))
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> helper.setBlock(MIN_A.offset(1, 1, 1), Blocks.AIR))
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenSucceed();
    }

    // Item access

    @GameTest(template = TEMPLATE_EMPTY)
    public void testHopperInsertsIntoChest(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos target = getHopperTarget(corePos, MIN_A.offset(1, 2, 1));
        BlockPos hopperPos = LoaderCapabilities.blockCapabilitiesSupported ? target.above() : target.north();
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,
                LoaderCapabilities.blockCapabilitiesSupported ? Direction.DOWN : Direction.SOUTH));
        ((HopperBlockEntity) helper.getBlockEntity(hopperPos)).setItem(0, new ItemStack(Items.STONE, 3));
        helper.succeedWhen(() -> helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 3L, "stored count"));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testBreakWallStopsHopperAndRestoreResumes(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos target = getHopperTarget(corePos, MIN_A.offset(1, 2, 1));
        BlockPos hopperPos = LoaderCapabilities.blockCapabilitiesSupported ? target.above() : target.north();
        BlockPos brokenWall = MIN_A.offset(0, 1, 1);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,
                LoaderCapabilities.blockCapabilitiesSupported ? Direction.DOWN : Direction.SOUTH));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> helper.setBlock(brokenWall, Blocks.AIR))
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> hopper.setItem(0, new ItemStack(Items.STONE, 2)))
                .thenIdle(40)
                .thenExecute(() -> {
                    helper.assertValueEqual(hopper.getItem(0).getCount(), 2, "items left in hopper while dormant");
                    helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 0L, "stored count while dormant");
                    helper.setBlock(brokenWall, wall(ChestMaterial.WOOD));
                })
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenWaitUntil(() -> {
                    helper.assertTrue(hopper.isEmpty(), "Expected the hopper to be empty");
                    helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 2L, "stored count");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testComparatorReadsCore(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos comparatorPos = corePos.north();
        helper.setBlock(comparatorPos.below(), Blocks.STONE);
        helper.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.SOUTH));
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> helper.assertValueEqual(((ComparatorBlockEntity) helper.getBlockEntity(comparatorPos)).getOutputSignal(), 0, "empty signal"))
                .thenExecute(() -> getCore(helper, corePos).getStorage().insert(STONE, 1024, false))
                .thenWaitUntil(() -> helper.assertValueEqual(((ComparatorBlockEntity) helper.getBlockEntity(comparatorPos)).getOutputSignal(), 1, "signal of one full slot of 27"))
                .thenSucceed();
    }

    // Persistence

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBrokenCoreKeepsContents(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos newCorePos = buildWalls(helper, MIN_B, 3, ChestMaterial.WOOD, new BlockPos(1, 1, 0), Set.of());
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getStorage().insert(STONE, 100, false);
                    ItemStack dropped = breakCoreAndPickUp(helper, corePos);
                    ChestStorage.Contents contents = dropped.get(RegistryEntries.COMPONENT_CHEST_CONTENTS.value());
                    helper.assertTrue(contents != null && contents.entries().size() == 1, "Expected the dropped core to carry its contents");
                    helper.assertBlockProperty(MIN_A, BlockChestWall.FORMED, false);
                    placeCore(helper, dropped, newCorePos);
                })
                .thenWaitUntil(() -> assertFormed(helper, newCorePos, MIN_B, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, newCorePos).getStorage();
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 100L, "restored count");
                    helper.assertTrue(storage.getSlot(0).matches(STONE), "Expected stone in the first slot");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testEmptyCoreDropsWithoutContents(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ItemStack dropped = breakCoreAndPickUp(helper, corePos);
                    helper.assertTrue(dropped.is(core(ChestMaterial.WOOD).asItem()), "Expected a wooden core");
                    helper.assertTrue(dropped.get(RegistryEntries.COMPONENT_CHEST_CONTENTS.value()) == null, "Expected no contents on an empty core");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testReformSmallerMakesSlotsExtractOnly(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos newCorePos = buildWalls(helper, MIN_B, 2, ChestMaterial.WOOD, BlockPos.ZERO, Set.of());
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    // A size 3 wooden chest holds 16 stacks per slot, a size 2 one only 4.
                    helper.assertValueEqual(getCore(helper, corePos).getStorage().insert(0, STONE, 1000, false), 1000L, "inserted count");
                    placeCore(helper, breakCoreAndPickUp(helper, corePos), newCorePos);
                })
                .thenWaitUntil(() -> assertFormed(helper, newCorePos, MIN_B, 2))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, newCorePos).getStorage();
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 1000L, "kept count");
                    helper.assertValueEqual(storage.getCapacity(0), 256L, "capacity");
                    helper.assertTrue(storage.isExtractOnly(0), "Expected the over-capacity slot to be extract-only");
                    helper.assertValueEqual(storage.insert(0, STONE, 1, true), 0L, "insert into extract-only slot");
                    helper.assertValueEqual(storage.extract(0, 64, false), 64L, "extract from extract-only slot");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSaveLoadRoundTrip(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(STONE, 600, false);
                    core.getStorage().lockTo(5, new ItemStack(Items.DIRT));
                    assertRoundTrip(helper, core);
                    helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> assertRoundTrip(helper, getCore(helper, corePos)))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLoweredConfigMakesSlotsExtractOnly(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(0, STONE, 1000, false);
                    CompoundTag tag = core.saveWithFullMetadata(helper.getLevel().registryAccess());
                    int oldDepth = GeneralConfig.depthSize3;
                    try {
                        // Simulates loading the chest after the depth of size 3 was lowered from 16 to 4 stacks.
                        GeneralConfig.depthSize3 = 4;
                        BlockEntityChestCore loaded = (BlockEntityChestCore) BlockEntity.loadStatic(core.getBlockPos(), core.getBlockState(), tag, helper.getLevel().registryAccess());
                        helper.assertValueEqual(loaded.getStorage().getSlot(0).getCount(), 1000L, "kept count");
                        helper.assertTrue(loaded.getStorage().isExtractOnly(0), "Expected the over-capacity slot to be extract-only");
                    } finally {
                        GeneralConfig.depthSize3 = oldDepth;
                    }
                })
                .thenSucceed();
    }

    private static void assertRoundTrip(GameTestHelper helper, BlockEntityChestCore core) {
        CompoundTag tag = core.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loadedEntity = BlockEntity.loadStatic(core.getBlockPos(), core.getBlockState(), tag, helper.getLevel().registryAccess());
        if (!(loadedEntity instanceof BlockEntityChestCore loaded)) {
            throw new IllegalStateException("Expected a chest core after loading");
        }
        helper.assertTrue(Objects.equals(loaded.getStructure(), core.getStructure()), "Expected the structure to survive a round trip");
        helper.assertValueEqual(loaded.getLastSize(), core.getLastSize(), "last size");
        helper.assertValueEqual(loaded.getStorage().getSlotCount(), core.getStorage().getSlotCount(), "slot count");
        helper.assertValueEqual(loaded.getStorage().getProfile(), core.getStorage().getProfile(), "profile");
        for (int slot = 0; slot < core.getStorage().getSlotCount(); slot++) {
            helper.assertValueEqual(loaded.getStorage().getSlot(slot), core.getStorage().getSlot(slot), "slot " + slot);
        }
    }

}
