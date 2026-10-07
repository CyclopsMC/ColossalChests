package org.cyclops.colossalchests2.gametest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.BlockUncolossalChest;
import org.cyclops.colossalchests2.block.ChestInteractions;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.block.ChestSounds;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChest;
import org.cyclops.colossalchests2.blockentity.DisplayOption;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.cyclops.colossalchests2.capability.WallAccess;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ChestClickLogic;
import org.cyclops.colossalchests2.inventory.ChestSearch;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.inventory.ContainerInterface;
import org.cyclops.colossalchests2.inventory.ContainerMagnet;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeTool;
import org.cyclops.colossalchests2.inventory.ContainerRedstone;
import org.cyclops.colossalchests2.inventory.ContainerUncolossalChest;
import org.cyclops.colossalchests2.material.ItemMaterialUpgradeTool;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.multiblock.StructureDiagnosis;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestTablesPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestFormPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundDisplayTakePacket;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.CompressionFamilies;
import org.cyclops.colossalchests2.storage.CompressionFamiliesCache;
import org.cyclops.colossalchests2.storage.CompressionFamily;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.cyclops.colossalchests2.storage.NestedChests;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeInventory;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeRules;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.cyclops.cyclopscore.network.PacketBase;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        for (ChestMaterial material : ChestMaterial.BUILT_IN) {
            helper.assertTrue(ChestTablesLoader.get().materials().containsKey(material.id()), "Expected the data file of " + material.id());
        }
        helper.succeed();
    }

    // Helpers

    public static Block wall(ChestMaterial material) {
        return material.getWallBlock();
    }

    public static Block core(ChestMaterial material) {
        return material.getCoreBlock();
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
     * Replace a wall by a functional wall.
     * @return The position.
     */
    public static BlockPos placeWall(GameTestHelper helper, BlockPos pos, WallType type) {
        helper.setBlock(pos, functionalWall(type));
        return pos;
    }

    public static Block functionalWall(WallType type) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, type.getRegistryName()));
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
    public void testGrowFormedChest(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.IRON);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getStorage().insert(STONE, 100, false);
                    buildWalls(helper, MIN_A, 4, ChestMaterial.IRON, corePos.subtract(MIN_A), Set.of());
                    for (BlockPos pos : BlockPos.betweenClosed(MIN_A.offset(1, 1, 1), MIN_A.offset(2, 2, 2))) {
                        helper.setBlock(pos, Blocks.AIR);
                    }
                })
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 4))
                .thenExecute(() -> {
                    helper.assertValueEqual(getCore(helper, corePos).getLastSize(), 4, "last size");
                    helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 100L, "kept count");
                    helper.assertBlockProperty(MIN_A, BlockChestWall.FORMED, true);
                })
                .thenSucceed();
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
        BlockPos hopperPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE).above();
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        ((HopperBlockEntity) helper.getBlockEntity(hopperPos)).setItem(0, new ItemStack(Items.STONE, 3));
        helper.succeedWhen(() -> helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 3L, "stored count"));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testBreakWallStopsHopperAndRestoreResumes(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos hopperPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE).above();
        BlockPos brokenWall = MIN_A.offset(0, 1, 1);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
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

    @GameTest(template = TEMPLATE_EMPTY)
    public void testViewersReceiveDirtySlots(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.addViewer(player);
                    helper.assertTrue(core.getViewers().contains(player), "Expected the player to be a viewer");
                    helper.assertTrue(core.getStorage().hasDirtySlots(), "Expected a new viewer to mark all slots dirty");
                })
                .thenWaitUntil(() -> helper.assertFalse(getCore(helper, corePos).getStorage().hasDirtySlots(), "Expected dirty slots to be sent"))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.addViewer(player);
                    helper.assertFalse(core.getStorage().hasDirtySlots(), "Expected an existing viewer not to resend all slots");
                    core.removeViewer(player);
                    helper.assertTrue(core.getViewers().isEmpty(), "Expected no viewers");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCoreIndexFindsFormedCore(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    helper.assertTrue(ChestCoreIndex.findFormedCore(helper.getLevel(), helper.absolutePos(MIN_A)).orElse(null) == core,
                            "Expected a wall to find its core");
                    helper.assertTrue(ChestCoreIndex.findFormedCore(helper.getLevel(), helper.absolutePos(MIN_A.offset(1, 1, 1))).isEmpty(),
                            "Expected the interior to have no core");
                    helper.assertTrue(ChestCoreIndex.findFormedCore(helper.getLevel(), helper.absolutePos(MIN_A).offset(GeneralConfig.HARD_MAX_SIZE, 0, 0)).isEmpty(),
                            "Expected a far position to have no core");
                    core.getStorage().insert(STONE, 10, false);
                    helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> {
                    helper.assertTrue(ChestCoreIndex.findFormedCore(helper.getLevel(), helper.absolutePos(MIN_A)).isEmpty(),
                            "Expected a dormant core not to be found");
                    helper.assertValueEqual(getCore(helper, corePos).getComparatorSignal(), 0, "comparator signal of a dormant core");
                })
                .thenSucceed();
    }

    // Rendering

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFormedMembersRenderAsGiantChest(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    assertRenderedAsGiantChest(helper, MIN_A, true);
                    assertRenderedAsGiantChest(helper, corePos, true);
                    helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> {
                    assertRenderedAsGiantChest(helper, MIN_A, false);
                    assertRenderedAsGiantChest(helper, corePos, false);
                })
                .thenSucceed();
    }

    private static void assertRenderedAsGiantChest(GameTestHelper helper, BlockPos pos, boolean formed) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolute);
        // Formed members have an invisible model, which vanilla still draws the breaking crack on.
        helper.assertValueEqual(state.getRenderShape(), RenderShape.MODEL, "render shape at " + pos);
        helper.assertValueEqual(state.getOcclusionShape(helper.getLevel(), absolute).isEmpty(), formed, "empty occlusion at " + pos);
        helper.assertValueEqual(state.getLightBlock(helper.getLevel(), absolute), formed ? 0 : helper.getLevel().getMaxLightLevel(), "light block at " + pos);
        helper.assertValueEqual(state.propagatesSkylightDown(helper.getLevel(), absolute), formed, "skylight through " + pos);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUpdateTagCarriesStructureButNoContents(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(STONE, 10, false);
                    helper.assertValueEqual(core.getDecoratedPositions(), List.of(helper.absolutePos(corePos)), "decorated positions");
                    CompoundTag tag = core.getUpdateTag(helper.getLevel().registryAccess());
                    helper.assertFalse(tag.contains("storage"), "Expected no contents in the update tag");

                    BlockEntityChestCore client = new BlockEntityChestCore(core.getBlockPos(), core.getBlockState());
                    client.loadWithComponents(tag, helper.getLevel().registryAccess());
                    helper.assertValueEqual(client.getStructure(), core.getStructure(), "synced structure");
                    helper.assertValueEqual(client.getDecoratedPositions(), core.getDecoratedPositions(), "synced decorated positions");
                    helper.assertValueEqual(client.getFacing(), Direction.NORTH, "synced facing");
                    helper.assertTrue(client.getStorage().getSlot(0).isEmpty(), "Expected no synced contents");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testViewersOpenAndCloseLid(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> getCore(helper, corePos).addViewer(player))
                // The lid only animates on clients, so tick it here as a client would after the block event.
                .thenWaitUntil(() -> {
                    tickLid(helper, corePos);
                    helper.assertTrue(getCore(helper, corePos).getOpenness(1) == 1, "Expected an open lid");
                })
                .thenExecute(() -> getCore(helper, corePos).removeViewer(player))
                .thenWaitUntil(() -> {
                    tickLid(helper, corePos);
                    helper.assertTrue(getCore(helper, corePos).getOpenness(1) == 0, "Expected a closed lid");
                })
                .thenSucceed();
    }

    private static void tickLid(GameTestHelper helper, BlockPos corePos) {
        BlockEntityChestCore core = getCore(helper, corePos);
        BlockEntityChestCore.clientTick(helper.getLevel(), core.getBlockPos(), core.getBlockState(), core);
    }

    // GUI

    private static ServerPlayer makeViewer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos near = helper.absolutePos(MIN_A.offset(1, 0, -2));
        player.moveTo(near.getX() + 0.5, near.getY(), near.getZ() + 0.5);
        return player;
    }

    private static void use(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        helper.getLevel().getBlockState(absolute).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.NORTH, absolute, false));
    }

    /**
     * Open the menu like a click on the given member does. The menu is created directly, as NeoForge refuses
     * to send the open packet to mock players.
     */
    private static ContainerChest openChest(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockEntityChestCore core = ChestInteractions.findFormedCore(helper.getLevel().getBlockState(absolute), helper.getLevel(), absolute)
                .orElseThrow(() -> new GameTestAssertException("Expected a formed chest to open from " + pos));
        ContainerChest menu = new ContainerChest(100, player.getInventory(), core);
        player.containerMenu = menu;
        return menu;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuOpensFromEveryMember(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    for (BlockPos pos : new ChestStructure(MIN_A, 3).shell()) {
                        ContainerChest menu = openChest(helper, player, pos);
                        helper.assertValueEqual(menu.getCorePos(), helper.absolutePos(corePos), "core of the menu opened from " + pos);
                        helper.assertTrue(getCore(helper, corePos).getViewers().contains(player), "Expected the player to view the chest");
                        player.closeContainer();
                        helper.assertTrue(getCore(helper, corePos).getViewers().isEmpty(), "Expected closing to remove the viewer");
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuDoesNotOpenWhenDormant(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR))
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    use(helper, player, MIN_A);
                    use(helper, player, corePos);
                    helper.assertFalse(player.containerMenu instanceof ContainerChest, "Expected no menu for a dormant chest");
                    StructureDiagnosis.Result result = ChestInteractions.explain(player, helper.getLevel(), helper.absolutePos(MIN_A));
                    helper.assertValueEqual(result.getProblem(), StructureDiagnosis.Problem.BLOCKS, "diagnosed problem");
                    helper.assertValueEqual(result.missing(), List.of(helper.absolutePos(MIN_A.offset(0, 1, 1))), "missing walls");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testHeldItemPlacesAgainstUnformedChest(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos looseWall = MIN_B;
        helper.setBlock(looseWall, wall(ChestMaterial.WOOD));
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    ItemStack held = new ItemStack(wall(ChestMaterial.WOOD));
                    // An unformed wall lets the held item be used, so walls can be placed against it.
                    helper.assertValueEqual(useWithItem(helper, player, held, looseWall), ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, "unformed wall with an item");
                    helper.assertValueEqual(useWithItem(helper, player, ItemStack.EMPTY, looseWall), ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "unformed wall without an item");
                    // A formed chest still opens.
                    helper.assertValueEqual(useWithItem(helper, player, held, MIN_A), ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "formed wall with an item");
                    helper.assertValueEqual(useWithItem(helper, player, held, corePos), ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "formed core with an item");
                })
                .thenSucceed();
    }

    private static ItemInteractionResult useWithItem(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return helper.getLevel().getBlockState(absolute).useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuClickRules(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    player.getInventory().clearContent();
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    storage.insert(0, STONE, 1000, false);
                    ContainerChest menu = openChest(helper, player, corePos);

                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_STACK);
                    helper.assertValueEqual(menu.getCarried().getCount(), 64, "cursor after left click");
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 936L, "count after left click");
                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_STACK);
                    helper.assertTrue(menu.getCarried().isEmpty(), "Expected the cursor to go back in");
                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_HALF);
                    helper.assertValueEqual(menu.getCarried().getCount(), 32, "cursor after right click");
                    menu.setCarried(ItemStack.EMPTY);
                    menu.handleChestClick(player, 0, ChestClickAction.MOVE_STACK);
                    helper.assertValueEqual(player.getInventory().countItem(Items.STONE), 64, "stone in inventory after shift click");
                    // The remaining 904 fit in the other 35 inventory slots.
                    menu.handleChestClick(player, 0, ChestClickAction.MOVE_ALL);
                    helper.assertValueEqual(player.getInventory().countItem(Items.STONE), 968, "stone in inventory after ctrl click");
                    helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected ctrl click to empty the slot");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuShiftClickFromPlayerInventory(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    player.getInventory().clearContent();
                    player.getInventory().setItem(0, new ItemStack(Items.DIRT, 32));
                    ContainerChest menu = openChest(helper, player, corePos);
                    // Menu slots 27 to 35 are the hotbar.
                    menu.quickMoveStack(player, 27);
                    helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "Expected the hotbar slot to be emptied");
                    helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 32L, "dirt in the chest");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuViewersSeeChanges(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        ServerPlayer[] players = new ServerPlayer[2];
        ContainerChest[] menus = new ContainerChest[2];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    for (int i = 0; i < 2; i++) {
                        players[i] = makeViewer(helper);
                        menus[i] = openChest(helper, players[i], corePos);
                    }
                    getCore(helper, corePos).getStorage().insert(4, STONE, 100, false);
                })
                // The core hands changed slots to open menus on its tick, the menus send them on broadcast.
                .thenIdle(1)
                .thenExecute(() -> {
                    for (int i = 0; i < 2; i++) {
                        menus[i].broadcastChanges();
                        helper.assertValueEqual(menus[i].getChestSlot(4).getCount(), 100L, "count synced to viewer " + i);
                    }
                    for (ServerPlayer player : players) {
                        player.closeContainer();
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuDragSpreadsCursor(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.setCarried(STONE.copyWithCount(10));
                    menu.handleChestDrag(new int[]{0, 1, 2}, false);
                    helper.assertValueEqual(menu.getCarried().getCount(), 1, "cursor after an even drag");
                    helper.assertValueEqual(storage.getSlot(2).getCount(), 3L, "count after an even drag");
                    menu.setCarried(STONE.copyWithCount(10));
                    // Invalid and repeated slots are ignored.
                    menu.handleChestDrag(new int[]{3, 3, 4, -1, 999}, true);
                    helper.assertValueEqual(menu.getCarried().getCount(), 8, "cursor after a one-each drag");
                    helper.assertValueEqual(storage.getSlot(4).getCount(), 1L, "count after a one-each drag");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuDragPreviewMatchesDrag(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
                    storage.insert(1, STONE, storage.getCapacity(STONE) - 2, false);
                    storage.insert(2, pearl, 1, false);
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.broadcastChanges();
                    // The space the GUI sees equals what the storage would accept.
                    for (ItemStack type : List.of(STONE, pearl)) {
                        for (int slot = 0; slot < 4; slot++) {
                            helper.assertValueEqual(menu.getChestSlotSpace(slot, type), storage.insert(slot, type, Long.MAX_VALUE, true),
                                    "space for " + type + " in slot " + slot);
                        }
                    }
                    // The preview predicts the drag, including the slot that fills up.
                    int[] slots = {0, 1, 2, 3};
                    ItemStack cursor = STONE.copyWithCount(20);
                    long[] predicted = new long[4];
                    ItemStack predictedCursor = ChestClickLogic.drag(slots, false, cursor, (slot, amount) -> {
                        predicted[slot] = Math.min(amount, menu.getChestSlotSpace(slot, cursor));
                        return predicted[slot];
                    });
                    long[] before = new long[4];
                    for (int slot = 0; slot < 4; slot++) {
                        before[slot] = storage.getSlot(slot).getCount();
                    }
                    menu.setCarried(cursor);
                    menu.handleChestDrag(slots, false);
                    helper.assertValueEqual(menu.getCarried().getCount(), predictedCursor.getCount(), "cursor after the drag");
                    for (int slot = 0; slot < 4; slot++) {
                        helper.assertValueEqual(storage.getSlot(slot).getCount() - before[slot], predicted[slot], "added to slot " + slot);
                    }
                    helper.assertValueEqual(predicted[1], 2L, "the nearly full slot is capped");
                    helper.assertValueEqual(predicted[2], 0L, "the pearl slot is skipped");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuClosesWhenDormant(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        ServerPlayer[] player = new ServerPlayer[1];
        ContainerChest[] menu = new ContainerChest[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    player[0] = makeViewer(helper);
                    menu[0] = openChest(helper, player[0], corePos);
                    helper.assertTrue(menu[0].stillValid(player[0]), "Expected a valid menu");
                    helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> {
                    helper.assertFalse(menu[0].stillValid(player[0]), "Expected the menu to close on a dormant chest");
                    player[0].closeContainer();
                })
                .thenSucceed();
    }

    // Upgrades

    private static ItemStack upgradeItem(ChestUpgrade upgrade) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "upgrade_" + upgrade.getId().getPath())));
    }

    /**
     * Click an upgrade slot in the menu like a player would.
     */
    private static void clickUpgradeSlot(ContainerChest menu, ServerPlayer player, int upgradeSlot) {
        menu.clicked(menu.getUpgradeSlotsStart() + upgradeSlot, 0, ClickType.PICKUP, player);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDepthUpgradeOnCopper4x4(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 4, ChestMaterial.COPPER);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 4))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    helper.assertValueEqual(menu.getUpgradeSlotCount(), 2, "upgrade slots of a copper chest");
                    helper.assertValueEqual(storage.getProfile().depth(), 64L, "depth without upgrades");

                    // Insert: the capacity doubles.
                    menu.setCarried(upgradeItem(ChestUpgrades.DEPTH));
                    clickUpgradeSlot(menu, player, 0);
                    helper.assertTrue(menu.getCarried().isEmpty(), "Expected the upgrade to be placed");
                    helper.assertValueEqual(storage.getProfile().depth(), 128L, "depth with a Depth upgrade");
                    helper.assertValueEqual(storage.getCapacity(STONE), 128L * 64, "stone capacity with a Depth upgrade");

                    // A second one does not fit a copper chest.
                    menu.setCarried(upgradeItem(ChestUpgrades.DEPTH));
                    clickUpgradeSlot(menu, player, 1);
                    helper.assertValueEqual(menu.getCarried().getCount(), 1, "rejected second Depth upgrade on the cursor");
                    helper.assertTrue(menu.getUpgradeInsertProblem(menu.getCarried()) != null, "Expected a reason for the rejected Depth upgrade");
                    menu.setCarried(ItemStack.EMPTY);

                    // Fill a slot past the capacity without it: removal is refused.
                    storage.insert(0, STONE, 64 * 64 + 1, false);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 1, "slots keeping the upgrade in");
                    clickUpgradeSlot(menu, player, 0);
                    helper.assertTrue(menu.getCarried().isEmpty(), "Expected the removal to be refused");
                    helper.assertValueEqual(storage.getProfile().depth(), 128L, "depth after a refused removal");

                    // Once it fits, removal works.
                    storage.extract(0, 1, false);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 0, "slots keeping the upgrade in after emptying");
                    clickUpgradeSlot(menu, player, 0);
                    helper.assertTrue(menu.getCarried().is(upgradeItem(ChestUpgrades.DEPTH).getItem()), "Expected the upgrade on the cursor");
                    helper.assertValueEqual(storage.getProfile().depth(), 64L, "depth after removal");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSlotExpansionUpgrade(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    // Shift-clicking an upgrade from the inventory installs it.
                    player.getInventory().setItem(9, upgradeItem(ChestUpgrades.SLOT_EXPANSION));
                    menu.quickMoveStack(player, 0);
                    helper.assertValueEqual(storage.getSlotCount(), 54, "slots with a Slot Expansion");
                    storage.insert(40, STONE, 1, false);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 1, "slots keeping the expansion in");
                    menu.quickMoveStack(player, menu.getUpgradeSlotsStart());
                    helper.assertValueEqual(storage.getSlotCount(), 54, "slots after a refused removal");
                    storage.extract(40, 1, false);
                    menu.quickMoveStack(player, menu.getUpgradeSlotsStart());
                    helper.assertValueEqual(storage.getSlotCount(), 27, "slots after removal");
                    helper.assertTrue(player.getInventory().contains(upgradeItem(ChestUpgrades.SLOT_EXPANSION)), "Expected the upgrade back in the inventory");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUpgradeSlotsRejectWhatDoesNotFit(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestUpgradeInventory upgrades = getCore(helper, corePos).getUpgrades();
                    helper.assertValueEqual(upgrades.getContainerSize(), 1, "upgrade slots of a wooden chest");
                    helper.assertFalse(upgrades.canPlaceItem(0, upgradeItem(ChestUpgrades.DEPTH)), "Expected a wooden chest to refuse Depth");
                    helper.assertFalse(upgrades.canPlaceItem(0, STONE), "Expected upgrade slots to refuse other items");
                    helper.assertTrue(upgrades.canPlaceItem(0, upgradeItem(ChestUpgrades.LOCK)), "Expected a wooden chest to take Lock");
                    upgrades.setItem(0, upgradeItem(ChestUpgrades.LOCK));
                    helper.assertFalse(upgrades.canPlaceItem(0, upgradeItem(ChestUpgrades.SLOT_EXPANSION)), "Expected a full slot to refuse");
                    helper.assertFalse(upgrades.canTakeItem(upgrades, 0, upgrades.getItem(0)), "Expected automation to never take upgrades");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLockActions(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    storage.insert(0, STONE, 10, false);
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);

                    // Without the Lock upgrade, lock clicks do nothing.
                    menu.handleChestClick(player, 0, ChestClickAction.TOGGLE_LOCK);
                    helper.assertFalse(storage.getSlot(0).isLocked(), "Expected no lock without the Lock upgrade");

                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.LOCK));
                    menu.handleChestClick(player, 0, ChestClickAction.TOGGLE_LOCK);
                    helper.assertTrue(storage.getSlot(0).isLocked(), "Expected alt-click to lock");
                    menu.handleChestClick(player, 0, ChestClickAction.TOGGLE_LOCK);
                    helper.assertFalse(storage.getSlot(0).isLocked(), "Expected alt-click to unlock");

                    // A right click with a cursor item reserves an empty slot without inserting.
                    menu.setCarried(new ItemStack(Items.DIRT, 5));
                    menu.handleChestClick(player, 3, ChestClickAction.LOCK_TO_CURSOR);
                    helper.assertTrue(storage.getSlot(3).isLocked() && storage.getSlot(3).matches(new ItemStack(Items.DIRT)), "Expected a dirt reservation");
                    helper.assertValueEqual(storage.getSlot(3).getCount(), 0L, "reserved count");
                    helper.assertValueEqual(menu.getCarried().getCount(), 5, "cursor after reserving");
                    menu.setCarried(ItemStack.EMPTY);
                    helper.assertValueEqual(storage.insert(new ItemStack(Items.DIRT), 4, false), 4L, "dirt inserted");
                    helper.assertValueEqual(storage.getSlot(3).getCount(), 4L, "dirt goes to its reserved slot");

                    menu.handleChestClick(player, 0, ChestClickAction.LOCK_ALL);
                    helper.assertTrue(storage.getSlot(0).isLocked(), "Expected Lock all to lock filled slots");
                    menu.handleChestClick(player, 0, ChestClickAction.CLEAR_LOCKS);
                    helper.assertFalse(storage.getSlot(0).isLocked() || storage.getSlot(3).isLocked(), "Expected Clear locks to unlock all");

                    // Removing the Lock upgrade is never refused and clears locks.
                    menu.handleChestClick(player, 0, ChestClickAction.LOCK_ALL);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 0, "slots keeping the Lock upgrade in");
                    clickUpgradeSlot(menu, player, 0);
                    helper.assertTrue(menu.getCarried().is(upgradeItem(ChestUpgrades.LOCK).getItem()), "Expected the Lock upgrade on the cursor");
                    helper.assertFalse(storage.getSlot(0).isLocked(), "Expected locks to be cleared with the upgrade");
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 10L, "contents after clearing locks");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLockedSlotsRejectOtherTypesFromHopper(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos hopperPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE).above();
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.LOCK));
                    // Every slot is reserved for stone, so dirt has nowhere to go.
                    for (int slot = 0; slot < core.getStorage().getSlotCount(); slot++) {
                        core.getStorage().lockTo(slot, STONE);
                    }
                    helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
                    hopper.setItem(0, new ItemStack(Items.DIRT, 2));
                    hopper.setItem(1, new ItemStack(Items.STONE, 2));
                })
                .thenWaitUntil(() -> helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 2L, "stone in its locked slot"))
                .thenExecute(() -> {
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
                    helper.assertValueEqual(hopper.getItem(0).getCount(), 2, "dirt left in the hopper");
                    helper.assertTrue(hopper.getItem(0).is(Items.DIRT), "Expected dirt to stay in the hopper");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUpgradesTravelWithCore(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos newCorePos = buildWalls(helper, MIN_B, 3, ChestMaterial.WOOD, new BlockPos(1, 1, 0), Set.of());
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.SLOT_EXPANSION));
                    core.getStorage().insert(50, STONE, 7, false);
                    assertRoundTrip(helper, core);
                    ItemStack dropped = breakCoreAndPickUp(helper, corePos);
                    helper.assertTrue(dropped.has(RegistryEntries.COMPONENT_CHEST_UPGRADES.value()), "Expected upgrades on the item");
                    placeCore(helper, dropped, newCorePos);
                })
                .thenWaitUntil(() -> assertFormed(helper, newCorePos, MIN_B, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, newCorePos);
                    helper.assertValueEqual(core.getUpgradeSet().count(ChestUpgrades.SLOT_EXPANSION), 1, "restored Slot Expansion");
                    helper.assertValueEqual(core.getStorage().getSlotCount(), 54, "restored slot count");
                    helper.assertValueEqual(core.getStorage().getSlot(50).getCount(), 7L, "restored contents in an expanded slot");
                })
                .thenSucceed();
    }

    private static long countStored(ChestStorage storage, ItemStack type) {
        long count = 0;
        for (int slot = 0; slot < storage.getSlotCount(); slot++) {
            if (storage.getSlot(slot).matches(type)) {
                count += storage.getSlot(slot).getCount();
            }
        }
        return count;
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testVoidDestroysHopperOverflowButNotPlayerInserts(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos hopperPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE).above();
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.VOID));
                    storage.insert(0, STONE, storage.getCapacity(STONE), false);
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.handleChestClick(player, 0, ChestClickAction.TOGGLE_VOID);
                    helper.assertTrue(storage.getSlot(0).isVoiding(), "Expected the slot to be voiding");
                    player.closeContainer();
                    helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
                    hopper.setItem(0, STONE.copyWithCount(3));
                    hopper.setItem(1, new ItemStack(Items.DIRT, 2));
                })
                .thenWaitUntil(() -> {
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
                    helper.assertTrue(hopper.getItem(0).isEmpty(), "Expected the hopper's stone to be gone");
                    helper.assertTrue(hopper.getItem(1).isEmpty(), "Expected the hopper's dirt to be inserted");
                })
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    // The hopper's stone was destroyed instead of taking a new slot, the dirt was stored.
                    helper.assertValueEqual(countStored(storage, STONE), storage.getCapacity(STONE), "stone after voiding");
                    helper.assertValueEqual(countStored(storage, new ItemStack(Items.DIRT)), 2L, "dirt is not voided");
                    // A player putting stone on the full voiding slot never loses it: it goes to a free slot.
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.setCarried(STONE.copyWithCount(5));
                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_STACK);
                    helper.assertTrue(menu.getCarried().isEmpty(), "Expected the player's stone to be stored");
                    helper.assertValueEqual(countStored(storage, STONE), storage.getCapacity(STONE) + 5, "stone after a player insert");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testVoidMarksNeedTheUpgrade(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    storage.insert(0, STONE, 10, false);
                    storage.insert(1, new ItemStack(Items.DIRT), 10, false);
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.handleChestClick(player, 0, ChestClickAction.TOGGLE_VOID);
                    menu.handleChestClick(player, 0, ChestClickAction.VOID_ALL);
                    helper.assertFalse(storage.getSlot(0).isVoiding(), "Expected no void marks without the Void upgrade");

                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.VOID));
                    menu.handleChestClick(player, 0, ChestClickAction.VOID_ALL);
                    helper.assertTrue(storage.getSlot(0).isVoiding() && storage.getSlot(1).isVoiding(), "Expected Void all to mark filled slots");
                    menu.handleChestClick(player, 1, ChestClickAction.TOGGLE_VOID);
                    helper.assertFalse(storage.getSlot(1).isVoiding(), "Expected the toggle to unmark");
                    menu.handleChestClick(player, 0, ChestClickAction.CLEAR_VOIDS);
                    helper.assertFalse(storage.getSlot(0).isVoiding(), "Expected Clear voids to unmark all");

                    // Removing the Void upgrade clears the marks and is never refused.
                    menu.handleChestClick(player, 0, ChestClickAction.VOID_ALL);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 0, "slots keeping the Void upgrade in");
                    clickUpgradeSlot(menu, player, 0);
                    helper.assertFalse(storage.getSlot(0).isVoiding(), "Expected marks to be cleared with the upgrade");
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 10L, "contents after clearing marks");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBundlingRaisesUnstackableCapacity(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.IRON);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ItemStack sword = new ItemStack(Items.IRON_SWORD);
                    helper.assertValueEqual(core.getStorage().getCapacity(sword), 1L, "swords per slot without upgrades");
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.BUNDLING));
                    core.getUpgrades().setItem(1, upgradeItem(ChestUpgrades.BUNDLING));
                    helper.assertValueEqual(core.getStorage().getCapacity(sword), 4L, "swords per slot with two Bundling upgrades");
                    core.getUpgrades().setItem(2, upgradeItem(ChestUpgrades.DEPTH));
                    helper.assertValueEqual(core.getStorage().getCapacity(sword), 8L, "swords per slot with Bundling and Depth");
                    helper.assertValueEqual(core.getStorage().insert(sword, 8, false), 8L, "swords inserted into one slot");
                    helper.assertValueEqual(core.getStorage().getSlot(0).getCount(), 8L, "swords in the first slot");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCompressionFamiliesFromVanillaRecipes(GameTestHelper helper) {
        CompressionFamilies families = CompressionFamiliesCache.get(helper.getLevel());
        CompressionFamily iron = families.find(Items.IRON_NUGGET).orElseThrow(() -> new GameTestAssertException("Expected an iron family"));
        helper.assertValueEqual(iron.size(), 3, "iron forms");
        helper.assertTrue(iron.largest().item() == Items.IRON_BLOCK, "Expected iron blocks as the largest form");
        helper.assertValueEqual(iron.largest().baseUnits(), 81L, "nuggets per iron block");
        helper.assertTrue(families.find(Items.HAY_BLOCK).isPresent(), "Expected wheat and hay bales to compress");
        helper.assertTrue(families.find(Items.NETHERITE_BLOCK).isPresent(), "Expected netherite to compress");
        // One-way recipes never compress.
        helper.assertTrue(families.find(Items.QUARTZ_BLOCK).isEmpty(), "Expected quartz blocks not to compress");
        helper.assertTrue(families.find(Items.COBBLESTONE).isEmpty(), "Expected cobblestone not to compress");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testCompressionOnIron5x5(GameTestHelper helper) {
        // The core sits in the bottom row, so a hopper below can pull from it on every loader.
        BlockPos corePos = buildWalls(helper, MIN_A, 5, ChestMaterial.IRON, new BlockPos(2, 0, 0), Set.of());
        helper.setBlock(corePos, core(ChestMaterial.IRON));
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 5))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.COMPRESSION));
                    helper.assertTrue(storage.isCompressing(), "Expected the chest to compress");
                    // Nuggets, ingots and blocks all go into one slot as blocks.
                    helper.assertValueEqual(storage.insertAutomated(new ItemStack(Items.IRON_NUGGET), 5, false), 5L, "nuggets inserted");
                    helper.assertValueEqual(storage.insertAutomated(new ItemStack(Items.IRON_INGOT), 10, false), 10L, "ingots inserted");
                    helper.assertValueEqual(storage.insertAutomated(new ItemStack(Items.IRON_BLOCK), 2, false), 2L, "blocks inserted");
                    helper.assertTrue(storage.getSlot(0).matches(new ItemStack(Items.IRON_BLOCK)), "Expected the slot to hold blocks");
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 3L, "whole blocks");
                    helper.assertValueEqual(storage.getSlot(0).getRemainder(), 14L, "nuggets of remainder");
                    helper.assertTrue(storage.getSlot(1).isEmpty(), "Expected one slot to hold all forms");
                    // Removal is refused while a remainder is left.
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    menu.broadcastChanges();
                    helper.assertValueEqual(menu.getUpgradeRemovalProblems(0), 1, "slots keeping Compression in");
                    // Pick ingots, then a hopper pulling from the chest gets ingots.
                    menu.handleForm(0, new ItemStack(Items.IRON_INGOT));
                    player.closeContainer();
                    helper.setBlock(corePos.below(), Blocks.HOPPER.defaultBlockState());
                })
                .thenWaitUntil(() -> {
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(corePos.below());
                    helper.assertTrue(hopper.getItem(0).is(Items.IRON_INGOT) && hopper.getItem(0).getCount() >= 2, "Expected the hopper to pull ingots");
                })
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(corePos.below());
                    int pulled = 0;
                    for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
                        pulled += hopper.getItem(slot).getCount();
                    }
                    // 257 nuggets minus 9 per pulled ingot.
                    helper.assertValueEqual(storage.getAvailable(0, new ItemStack(Items.IRON_NUGGET)), 257L - 9L * pulled, "nuggets left");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCompressionClicksTakeTheChosenForm(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.IRON);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ChestStorage storage = core.getStorage();
                    storage.insert(0, new ItemStack(Items.IRON_INGOT), 64, false);
                    // Installing Compression converts the ingots: 7 blocks and 1 ingot.
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.COMPRESSION));
                    helper.assertValueEqual(storage.getSlot(0).getCount(), 7L, "blocks after installing Compression");
                    ServerPlayer player = makeViewer(helper);
                    ContainerChest menu = openChest(helper, player, corePos);
                    // Picked through the packet the screen sends.
                    roundTrip(helper, new ServerboundChestFormPacket(menu.containerId, 0, new ItemStack(Items.IRON_NUGGET)), ServerboundChestFormPacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_STACK);
                    helper.assertTrue(menu.getCarried().is(Items.IRON_NUGGET) && menu.getCarried().getCount() == 64, "Expected a stack of nuggets");
                    // Putting them back goes into the same slot.
                    menu.handleChestClick(player, 0, ChestClickAction.TAKE_STACK);
                    helper.assertTrue(menu.getCarried().isEmpty(), "Expected the nuggets to go back");
                    helper.assertValueEqual(storage.getAvailable(0, new ItemStack(Items.IRON_INGOT)), 64L, "ingots after putting nuggets back");
                    // A form outside the family is ignored.
                    roundTrip(helper, new ServerboundChestFormPacket(menu.containerId, 0, new ItemStack(Items.GOLD_INGOT)), ServerboundChestFormPacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertTrue(storage.getExtractionType(0).is(Items.IRON_NUGGET), "Expected the chosen form to stay");
                    player.closeContainer();
                })
                .thenSucceed();
    }

    private static <T extends PacketBase<T>> T roundTrip(GameTestHelper helper, T packet, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        codec.encode(buf, packet);
        T decoded = codec.decode(buf);
        helper.assertValueEqual(buf.readableBytes(), 0, "unread bytes of " + packet.type().id());
        return decoded;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenuPacketsRoundTrip(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    ServerPlayer player = makeViewer(helper);
                    player.getInventory().clearContent();
                    ContainerChest menu = openChest(helper, player, corePos);
                    storage.insert(0, STONE, 1000, false);

                    // The server handles clicks from the client.
                    roundTrip(helper, new ServerboundChestClickPacket(menu.containerId, 0, ChestClickAction.TAKE_STACK), ServerboundChestClickPacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(menu.getCarried().getCount(), 64, "cursor after a click packet");
                    player.containerMenu.setCarried(STONE.copyWithCount(6));
                    roundTrip(helper, new ServerboundChestDragPacket(menu.containerId, new int[]{5, 6}, false), ServerboundChestDragPacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(storage.getSlot(6).getCount(), 3L, "count after a drag packet");
                    menu.setCarried(STONE.copyWithCount(64));
                    // Packets for another menu are ignored.
                    roundTrip(helper, new ServerboundChestClickPacket(menu.containerId + 1, 0, ChestClickAction.TAKE_STACK), ServerboundChestClickPacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(menu.getCarried().getCount(), 64, "cursor after a click for another menu");

                    // A client menu takes what the server sends.
                    FriendlyByteBuf openData = new FriendlyByteBuf(Unpooled.buffer());
                    ContainerChest.writeOpenData(openData, getCore(helper, corePos));
                    ContainerChest client = new ContainerChest(menu.containerId, player.getInventory(), openData);
                    helper.assertValueEqual(client.getUpgradeSlotCount(), 1, "upgrade slots of a wooden chest");
                    helper.assertValueEqual(client.getMaxUpgradeCount(ChestUpgrades.DEPTH), 0, "depth upgrades a wooden chest takes");
                    // Depends on the max slots config, which a run directory may have saved.
                    helper.assertValueEqual(client.getMaxUpgradeCount(ChestUpgrades.SLOT_EXPANSION),
                            ChestUpgradeRules.getMaxCount(ChestUpgrades.SLOT_EXPANSION, ChestMaterial.WOOD.id()), "slot expansions a chest takes");
                    // The client explains why an upgrade does not go in.
                    Component noDepth = client.getUpgradeInsertProblem(upgradeItem(ChestUpgrades.DEPTH));
                    helper.assertTrue(noDepth != null && client.doesBetterMaterialTakeMore(upgradeItem(ChestUpgrades.DEPTH)),
                            "Expected a reason and the better material hint for Depth");
                    helper.assertFalse(client.doesBetterMaterialTakeMore(upgradeItem(ChestUpgrades.LOCK)), "Expected no better material hint for Lock");
                    helper.assertTrue(client.getUpgradeInsertProblem(upgradeItem(ChestUpgrades.LOCK)) == null, "Expected a wooden chest to take Lock");
                    helper.assertTrue(client.getUpgradeInsertProblem(STONE) != null, "Expected non-upgrades to be refused");
                    player.containerMenu = client;
                    DeepSlot locked = DeepSlot.of(STONE, 0, true, null);
                    roundTrip(helper, new ClientboundChestSlotsPacket(menu.containerId, new int[]{0, 3},
                            new DeepSlot[]{DeepSlot.of(STONE, 5000), locked}, new long[]{1024, 64}), ClientboundChestSlotsPacket.CODEC)
                            .actionClient(helper.getLevel(), player);
                    helper.assertValueEqual(client.getChestSlot(0).getCount(), 5000L, "synced count");
                    helper.assertTrue(client.getChestSlot(3).isLocked() && client.getChestSlot(3).matches(STONE), "Expected a synced locked slot");
                    helper.assertTrue(client.isChestSlotOverCapacity(0), "Expected 5000 of 1024 to be over capacity");
                    helper.assertValueEqual(client.getChestSlotCapacity(3), 64L, "synced capacity");
                    CapacityProfile profile = new CapacityProfile(7, 300, true, 3);
                    roundTrip(helper, new ClientboundChestStatePacket(menu.containerId, profile, new int[]{4}), ClientboundChestStatePacket.CODEC)
                            .actionClient(helper.getLevel(), player);
                    helper.assertValueEqual(client.getProfile(), profile, "synced capacity profile");
                    helper.assertValueEqual(client.getUpgradeRemovalProblems(0), 4, "synced upgrade removal problems");
                    // An empty slot takes what the profile allows for the type.
                    helper.assertValueEqual(client.getChestSlotSpace(5, new ItemStack(Items.DIAMOND_SWORD)), 3L, "space for unstackables");
                    helper.assertValueEqual(client.getChestSlotSpace(5, new ItemStack(Items.ENDER_PEARL)), 7L * 16, "space for 16-stacks");
                    helper.assertValueEqual(client.getChestSlotSpace(5, STONE), 300L, "space capped per slot");
                    player.containerMenu = menu;
                    player.closeContainer();
                    client.removed(player);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCoreMenuProviderAndOverCapacityWarning(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    ServerPlayer player = makeViewer(helper);
                    helper.assertValueEqual(core.getDisplayName(), Component.translatable("container.colossalchests2.chest",
                            Component.translatable("material.colossalchests2.wood")), "title");
                    if (!(core.createMenu(5, player.getInventory(), player) instanceof ContainerChest menu)) {
                        throw new GameTestAssertException("Expected a chest menu from the core");
                    }
                    helper.assertValueEqual(menu.getChestSlotCount(), core.getStorage().getSlotCount(), "menu slots");
                    menu.removed(player);

                    // Lowering the depth makes the slot over capacity, which players are told about on opening.
                    core.getStorage().insert(0, STONE, 1000, false);
                    int oldDepth = GeneralConfig.depthSize3;
                    try {
                        GeneralConfig.depthSize3 = 1;
                        // Loading applies the capacity for the current config.
                        core.loadWithComponents(core.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                        helper.assertTrue(core.getStorage().isExtractOnly(0), "Expected an over capacity slot");
                        core.warnIfOverCapacity(player);
                    } finally {
                        GeneralConfig.depthSize3 = oldDepth;
                        core.loadWithComponents(core.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                    }
                })
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
    public void testBrokenDormantCoreKeepsContents(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getStorage().insert(STONE, 100, false);
                    helper.setBlock(MIN_A.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> {
                    ItemStack dropped = breakCoreAndPickUp(helper, corePos);
                    ChestStorage.Contents contents = dropped.get(RegistryEntries.COMPONENT_CHEST_CONTENTS.value());
                    helper.assertTrue(contents != null && contents.entries().size() == 1, "Expected the dropped dormant core to carry its contents");
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
    public void testCreativeBreakDropsCoreWithContentsOnPlayerSide(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getStorage().insert(STONE, 100, false);
                    ItemStack dropped = breakCoreAsPlayer(helper, corePos, false);
                    ChestStorage.Contents contents = dropped.get(RegistryEntries.COMPONENT_CHEST_CONTENTS.value());
                    helper.assertTrue(contents != null && contents.entries().size() == 1, "Expected the dropped core to carry its contents");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCreativeBreakKeepsUpgradesOfEmptyCore(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getUpgrades().setItem(0, upgradeItem(ChestUpgrades.LOCK));
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.destroyBlock(helper.absolutePos(corePos));
                    helper.assertBlockNotPresent(core(ChestMaterial.WOOD), corePos);
                    helper.assertItemEntityPresent(core(ChestMaterial.WOOD).asItem(), corePos, 3);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCreativeBreakEmptyCoreDropsNothing(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.destroyBlock(helper.absolutePos(corePos));
                    helper.assertBlockNotPresent(core(ChestMaterial.WOOD), corePos);
                    helper.assertItemEntityNotPresent(core(ChestMaterial.WOOD).asItem(), corePos, 3);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSurvivalBreakDropsCoreOnPlayerSide(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ItemStack dropped = breakCoreAsPlayer(helper, corePos, true);
                    helper.assertTrue(dropped.is(core(ChestMaterial.WOOD).asItem()), "Expected a wooden core");
                    helper.assertBlockProperty(MIN_A, BlockChestWall.FORMED, false);
                    placeCore(helper, dropped, corePos);
                })
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> helper.assertTrue(getCore(helper, corePos).getStorage().getSlot(0).isEmpty(), "Expected an empty core"))
                .thenSucceed();
    }

    private static ServerPlayer makePlayerNorthOf(GameTestHelper helper, BlockPos corePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos absolute = helper.absolutePos(corePos);
        player.moveTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() - 2.5, 0, 0);
        return player;
    }

    private static ItemStack breakCoreAsPlayer(GameTestHelper helper, BlockPos corePos, boolean survival) {
        ServerPlayer player = makePlayerNorthOf(helper, corePos);
        if (survival) {
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        }
        BlockPos absolute = helper.absolutePos(corePos);
        helper.assertTrue(player.gameMode.destroyBlock(absolute), "Expected the player to break the core");
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(absolute).inflate(2));
        helper.assertValueEqual(items.size(), 1, "dropped item entities");
        // The core is on the north face, so its drop must not land inside the chest.
        helper.assertTrue(items.get(0).getZ() < absolute.getZ(), "Expected the drop on the player's side");
        ItemStack stack = items.get(0).getItem().copy();
        items.get(0).discard();
        return stack;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRemoveComponentsFromTagDropsStorage(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(STONE, 10, false);
                    CompoundTag tag = core.saveWithoutMetadata(helper.getLevel().registryAccess());
                    helper.assertTrue(tag.contains("storage"), "Expected storage in the saved tag");
                    core.removeComponentsFromTag(tag);
                    helper.assertFalse(tag.contains("storage"), "Expected storage to be stripped, as it is a component");
                    BlockEntityChestCore loaded = new BlockEntityChestCore(core.getBlockPos(), core.getBlockState());
                    loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
                    helper.assertTrue(loaded.getStorage().getSlot(0).isEmpty(), "Expected a core without stored contents to load empty");
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
    public void testSearchTagPrefix(GameTestHelper helper) {
        // Tags are only bound with data packs loaded, so not in unit tests.
        DeepSlot bricks = DeepSlot.of(new ItemStack(Items.STONE_BRICKS), 1);
        helper.assertTrue(search(bricks, "$stone_bricks"), "tag path should match");
        helper.assertTrue(search(bricks, "$minecraft:stone_bricks"), "tag id should match");
        helper.assertTrue(search(bricks, "$logs|$stone_bricks"), "tag alternative should match");
        helper.assertFalse(search(bricks, "$logs"), "other tag should not match");
        helper.succeed();
    }

    private static boolean search(DeepSlot slot, String query) {
        return ChestSearch.matches(slot, query, stack -> stack.getHoverName().getString(), stack -> List.of());
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
        helper.assertValueEqual(loaded.getUpgradeSet(), core.getUpgradeSet(), "upgrades");
        for (int slot = 0; slot < core.getStorage().getSlotCount(); slot++) {
            helper.assertValueEqual(loaded.getStorage().getSlot(slot), core.getStorage().getSlot(slot), "slot " + slot);
        }
    }


    // Functional walls

    private static HopperBlockEntity placeHopper(GameTestHelper helper, BlockPos pos, ItemStack... contents) {
        helper.setBlock(pos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(pos);
        for (int i = 0; i < contents.length; i++) {
            hopper.setItem(i, contents[i].copy());
        }
        return hopper;
    }

    private static int countInHopper(HopperBlockEntity hopper, Item item) {
        int count = 0;
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
            if (hopper.getItem(slot).is(item)) {
                count += hopper.getItem(slot).getCount();
            }
        }
        return count;
    }

    private static BlockEntityChestWall getWall(GameTestHelper helper, BlockPos pos) {
        if (!(helper.getBlockEntity(pos) instanceof BlockEntityChestWall wall)) {
            throw new GameTestAssertException("No functional wall at " + pos);
        }
        return wall;
    }

    /**
     * Open an Interface's settings like a sneak-click does. Created directly, like {@link #openChest}.
     */
    private static ContainerInterface openInterface(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        ContainerInterface menu = new ContainerInterface(101, player.getInventory(), getWall(helper, pos));
        player.containerMenu = menu;
        return menu;
    }

    /**
     * Click a settings slot with a stack on the cursor, like a player does.
     */
    private static void clickSetting(GameTestHelper helper, ContainerInterface menu, ServerPlayer player, int slot, ItemStack cursor) {
        menu.setCarried(cursor.copy());
        menu.clicked(slot, 0, ClickType.PICKUP, player);
        helper.assertTrue(ItemStack.matches(menu.getCarried(), cursor), "Expected the cursor to stay untouched");
        menu.setCarried(ItemStack.EMPTY);
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testPlainWallsExposeNothingButInterfacesDo(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos wallPos = MIN_A.offset(1, 2, 1);
        HopperBlockEntity hopper = placeHopper(helper, wallPos.above(), STONE.copyWithCount(2));
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenIdle(40)
                .thenExecute(() -> {
                    helper.assertValueEqual(countInHopper(hopper, Items.STONE), 2, "stone left above a plain wall");
                    helper.assertTrue(getCore(helper, corePos).getStorage().getSlot(0).isEmpty(), "Expected nothing to go through a plain wall");
                    placeWall(helper, wallPos, WallType.INTERFACE);
                })
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenWaitUntil(() -> helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 2L, "stone through the interface"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFunctionalWallsFitAnyMaterial(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.IRON);
        List<BlockPos> walls = List.of(MIN_A, MIN_A.offset(1, 2, 1), MIN_A.offset(2, 1, 2));
        for (int i = 0; i < walls.size(); i++) {
            placeWall(helper, walls.get(i), WallType.VALUES[i % WallType.VALUES.length]);
        }
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    // Functional walls draw their icon on the giant chest, plain walls do not.
                    Set<BlockPos> expected = Sets.newHashSet(helper.absolutePos(corePos));
                    walls.forEach(pos -> expected.add(helper.absolutePos(pos)));
                    helper.assertValueEqual(Set.copyOf(getCore(helper, corePos).getDecoratedPositions()), expected, "decorated positions");
                    for (BlockPos pos : walls) {
                        helper.assertBlockProperty(pos, BlockChestWall.FORMED, true);
                        helper.assertTrue(getWall(helper, pos).getCore().isPresent(), "Expected the wall to find its chest at " + pos);
                    }
                    helper.destroyBlock(walls.get(1));
                })
                .thenWaitUntil(() -> assertDormant(helper, corePos))
                .thenExecute(() -> helper.assertTrue(getWall(helper, walls.get(0)).getItemHandlerLogic().isEmpty(),
                        "Expected no item access through a dormant chest"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testInterfaceInputOnly(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos top = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE);
        BlockPos bottom = placeWall(helper, MIN_A.offset(1, 0, 1), WallType.INTERFACE);
        ContainerInterface[] bottomMenu = new ContainerInterface[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    for (BlockPos pos : List.of(top, bottom)) {
                        ContainerInterface menu = openInterface(helper, player, pos);
                        clickSetting(helper, menu, player, 0, STONE.copyWithCount(5));
                        helper.assertTrue(getWall(helper, pos).getSettings().getItem(0).is(Items.STONE), "Expected stone in the filter");
                        helper.assertValueEqual(getWall(helper, pos).getSettings().getItem(0).getCount(), 1, "filter entry count");
                        menu.clickMenuButton(player, ContainerInterface.BUTTON_MODE);
                        helper.assertValueEqual(getWall(helper, pos).getMode(), WallAccess.Mode.INPUT, "mode after one click");
                        bottomMenu[0] = menu;
                    }
                    placeHopper(helper, top.above(), STONE.copyWithCount(2), new ItemStack(Items.DIRT, 2));
                    placeHopper(helper, bottom.below());
                })
                .thenWaitUntil(() -> helper.assertValueEqual(getCore(helper, corePos).getStorage().getSlot(0).getCount(), 2L, "stone let in"))
                .thenIdle(40)
                .thenExecute(() -> {
                    HopperBlockEntity above = (HopperBlockEntity) helper.getBlockEntity(top.above());
                    HopperBlockEntity below = (HopperBlockEntity) helper.getBlockEntity(bottom.below());
                    helper.assertValueEqual(countInHopper(above, Items.DIRT), 2, "dirt kept out by the filter");
                    helper.assertTrue(below.isEmpty(), "Expected nothing to come out of an input-only interface");
                    // Output only: the hopper below pulls the stone out.
                    bottomMenu[0].clickMenuButton(makeViewer(helper), ContainerInterface.BUTTON_MODE);
                    helper.assertValueEqual(getWall(helper, bottom).getMode(), WallAccess.Mode.OUTPUT, "mode after two clicks");
                })
                .thenWaitUntil(() -> helper.assertValueEqual(countInHopper((HopperBlockEntity) helper.getBlockEntity(bottom.below()), Items.STONE), 2,
                        "stone pulled from the output-only interface"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testInterfaceExtractsInTheFilterForm(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.IRON);
        BlockPos bottom = placeWall(helper, MIN_A.offset(1, 0, 1), WallType.INTERFACE);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.COMPRESSION));
                    core.getStorage().insert(new ItemStack(Items.IRON_BLOCK), 2, false);
                    ServerPlayer player = makeViewer(helper);
                    ContainerInterface menu = openInterface(helper, player, bottom);
                    // Nuggets in the filter: only iron passes, taken out as nuggets.
                    clickSetting(helper, menu, player, 0, new ItemStack(Items.IRON_NUGGET));
                    helper.assertTrue(getWall(helper, bottom).getAccess().getExtractionForm(core.getStorage(), new ItemStack(Items.IRON_BLOCK)) == Items.IRON_NUGGET,
                            "Expected nuggets as the form");
                    player.closeContainer();
                    placeHopper(helper, bottom.below());
                })
                .thenWaitUntil(() -> helper.assertTrue(countInHopper((HopperBlockEntity) helper.getBlockEntity(bottom.below()), Items.IRON_NUGGET) >= 2,
                        "Expected the hopper to pull nuggets"))
                .thenExecute(() -> {
                    HopperBlockEntity hopper = (HopperBlockEntity) helper.getBlockEntity(bottom.below());
                    helper.assertValueEqual(countInHopper(hopper, Items.IRON_BLOCK) + countInHopper(hopper, Items.IRON_INGOT), 0, "other forms pulled");
                    long nuggets = getCore(helper, corePos).getStorage().getAvailable(0, new ItemStack(Items.IRON_NUGGET));
                    helper.assertValueEqual(nuggets + countInHopper(hopper, Items.IRON_NUGGET), 162L, "nuggets in total");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testInterfaceSettings(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos pos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    player.getInventory().setItem(0, new ItemStack(Items.DIRT, 7));
                    ContainerInterface menu = openInterface(helper, player, pos);
                    clickSetting(helper, menu, player, 3, STONE);
                    // Shift-clicking adds to the filter once and leaves the item in the inventory.
                    int hotbarSlot = menu.slots.size() - 9;
                    menu.quickMoveStack(player, hotbarSlot);
                    menu.quickMoveStack(player, hotbarSlot);
                    helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 7, "dirt left in the inventory");
                    BlockEntityChestWall wall = getWall(helper, pos);
                    helper.assertTrue(wall.getSettings().getItem(0).is(Items.DIRT), "Expected dirt in the first free filter slot");
                    helper.assertTrue(wall.getSettings().getItem(1).isEmpty(), "Expected dirt in the filter once");
                    menu.clickMenuButton(player, ContainerInterface.BUTTON_MODE);
                    menu.clickMenuButton(player, ContainerInterface.BUTTON_MODE);
                    // Clicking with an empty cursor clears an entry.
                    clickSetting(helper, menu, player, 0, ItemStack.EMPTY);
                    helper.assertTrue(wall.getSettings().getItem(0).isEmpty(), "Expected the cleared entry");
                    clickSetting(helper, menu, player, 0, new ItemStack(Items.DIRT));
                    WallAccess access = wall.getAccess();
                    helper.assertValueEqual(access.mode(), WallAccess.Mode.OUTPUT, "mode");
                    helper.assertValueEqual(access.filter().size(), 2, "filter entries");

                    // The settings survive saving and loading.
                    CompoundTag tag = wall.saveWithoutMetadata(helper.getLevel().registryAccess());
                    BlockEntityChestWall loaded = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
                    helper.assertValueEqual(loaded.getMode(), WallAccess.Mode.OUTPUT, "loaded mode");
                    for (int slot = 0; slot < loaded.getSettings().getContainerSize(); slot++) {
                        helper.assertTrue(ItemStack.matches(loaded.getSettings().getItem(slot), wall.getSettings().getItem(slot)), "loaded setting " + slot);
                    }
                    helper.assertTrue(menu.stillValid(player), "Expected the menu to be valid nearby");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 400)
    public void testVoidWallDestroysHeldOverflow(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos top = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.VOID);
        long[] capacity = new long[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    capacity[0] = storage.getCapacity(STONE);
                    storage.insert(0, STONE, capacity[0], false);
                    placeHopper(helper, top.above(), STONE.copyWithCount(3), new ItemStack(Items.DIRT, 2));
                })
                .thenWaitUntil(() -> helper.assertTrue(((HopperBlockEntity) helper.getBlockEntity(top.above())).isEmpty(), "Expected the hopper to empty"))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    helper.assertValueEqual(storage.getSlot(0).getCount(), capacity[0], "stone in its full slot");
                    helper.assertValueEqual(storage.getSlot(1).getCount(), 2L, "dirt stored");
                    helper.assertTrue(storage.getSlot(1).matches(new ItemStack(Items.DIRT)), "Expected dirt in the next slot");
                    helper.assertTrue(storage.getSlot(2).isEmpty(), "Expected the voided stone to take no slot");
                })
                .thenSucceed();
    }

    // Display walls

    private static ItemInteractionResult useWithItem(GameTestHelper helper, ServerPlayer player, BlockPos pos, ItemStack stack) {
        return useWithItem(helper, player, pos, stack, Direction.UP);
    }

    private static ItemInteractionResult useWithItem(GameTestHelper helper, ServerPlayer player, BlockPos pos, ItemStack stack, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        return helper.getLevel().getBlockState(absolute).useItemOn(stack, helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), face, absolute, false));
    }

    /**
     * Right-click with the held item like the server does: an item use that passes falls back to an empty hand use.
     */
    private static InteractionResult click(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolute);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        ItemInteractionResult result = state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        if (result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            return state.useWithoutItem(helper.getLevel(), player, hit);
        }
        return result.result();
    }

    private static int countInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDisplayWallInsertsAndTakes(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos display = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.DISPLAY);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    BlockEntityChestWall wall = getWall(helper, display);
                    ServerPlayer player = makeViewer(helper);
                    // The first item clicked in becomes the shown item and is inserted.
                    ItemStack held = STONE.copyWithCount(10);
                    useWithItem(helper, player, display, held);
                    helper.assertTrue(wall.getDisplayed(Direction.UP).is(Items.STONE), "Expected stone to be shown");
                    helper.assertTrue(held.isEmpty(), "Expected the held stone to be inserted");
                    helper.assertValueEqual(DisplayStats.of(storage, STONE).count(), 10L, "stored stone");
                    // Other items are not inserted.
                    ItemStack dirt = new ItemStack(Items.DIRT, 5);
                    useWithItem(helper, player, display, dirt);
                    helper.assertValueEqual(dirt.getCount(), 5, "dirt kept");
                    // A quick second right-click inserts all stone from the inventory.
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    player.getInventory().setItem(3, STONE.copyWithCount(20));
                    player.getInventory().setItem(4, STONE.copyWithCount(30));
                    useWithItem(helper, player, display, STONE.copyWithCount(1));
                    helper.assertValueEqual(click(helper, player, display), InteractionResult.CONSUME, "double click");
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 0, "stone left in the inventory");
                    helper.assertValueEqual(DisplayStats.of(storage, STONE).count(), 61L, "stored stone after inserting all");
                    // Left-clicks take a stack or one, through the packet the client sends.
                    roundTrip(helper, new ServerboundDisplayTakePacket(helper.absolutePos(display), Direction.UP, false), ServerboundDisplayTakePacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 61, "stone taken as one stack");
                    player.getInventory().clearContent();
                    storage.insert(STONE, 100, false);
                    roundTrip(helper, new ServerboundDisplayTakePacket(helper.absolutePos(display), Direction.UP, true), ServerboundDisplayTakePacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 1, "single stone taken");
                    // Taken items go to the held slot first, even when an earlier slot is free.
                    player.getInventory().clearContent();
                    player.getInventory().selected = 4;
                    roundTrip(helper, new ServerboundDisplayTakePacket(helper.absolutePos(display), Direction.UP, true), ServerboundDisplayTakePacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(player.getInventory().getItem(4).getCount(), 1, "stone in the held slot");
                    // A held stack is topped up, the rest goes elsewhere.
                    player.getInventory().setItem(4, STONE.copyWithCount(60));
                    roundTrip(helper, new ServerboundDisplayTakePacket(helper.absolutePos(display), Direction.UP, false), ServerboundDisplayTakePacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(player.getInventory().getItem(4).getCount(), 64, "held stone topped up");
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 124, "stone after taking a stack");
                    // Another held item stays, the stone goes elsewhere.
                    player.getInventory().clearContent();
                    player.getInventory().setItem(4, new ItemStack(Items.DIRT));
                    roundTrip(helper, new ServerboundDisplayTakePacket(helper.absolutePos(display), Direction.UP, true), ServerboundDisplayTakePacket.CODEC)
                            .actionServer(helper.getLevel(), player);
                    helper.assertTrue(player.getInventory().getItem(4).is(Items.DIRT), "Expected the held dirt to stay");
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 1, "stone next to the held dirt");
                    player.getInventory().selected = 0;
                    // Sneaking with an empty hand does nothing.
                    player.getInventory().clearContent();
                    player.setShiftKeyDown(true);
                    helper.assertValueEqual(click(helper, player, display), InteractionResult.PASS, "sneaking click");
                    helper.assertTrue(wall.getDisplayed(Direction.UP).is(Items.STONE), "Expected stone to stay shown");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDisplayWallSyncsAndHasNoItemAccess(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos display = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.DISPLAY);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, display);
                    helper.assertTrue(wall.getItemHandlerLogic().isEmpty(), "Expected no item access through a Display wall");
                    wall.setDisplayed(Direction.UP, STONE);
                    getCore(helper, corePos).getStorage().insert(STONE, 40, false);
                })
                // The stats follow the chest on their own.
                .thenWaitUntil(() -> helper.assertValueEqual(getWall(helper, display).getDisplayStats(Direction.UP).count(), 40L, "shown count"))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, display);
                    // What clients get.
                    CompoundTag tag = wall.getUpdateTag(helper.getLevel().registryAccess());
                    BlockEntityChestWall client = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    client.loadWithComponents(tag, helper.getLevel().registryAccess());
                    helper.assertTrue(client.getDisplayed(Direction.UP).is(Items.STONE), "Expected the shown item on the client");
                    helper.assertValueEqual(client.getDisplayStats(Direction.UP), wall.getDisplayStats(Direction.UP), "stats on the client");
                    // The shown item survives saving and loading.
                    BlockEntityChestWall loaded = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    loaded.loadWithComponents(wall.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                    helper.assertTrue(loaded.getDisplayed(Direction.UP).is(Items.STONE), "Expected the shown item after loading");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDisplayWallSettings(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos display = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.DISPLAY);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, display);
                    ServerPlayer player = makeViewer(helper);
                    getCore(helper, corePos).getStorage().insert(STONE, 5, false);
                    wall.setDisplayed(Direction.UP, STONE);
                    player.getInventory().setItem(3, STONE.copyWithCount(20));
                    // The settings choose the shown item through a ghost slot. Created directly, like openChest.
                    ContainerDisplay menu = new ContainerDisplay(102, player.getInventory(), wall);
                    player.containerMenu = menu;
                    helper.assertTrue(menu.getSlot(0).getItem().is(Items.STONE), "Expected the shown item in the slot");
                    menu.setCarried(new ItemStack(Items.DIRT, 5));
                    menu.clicked(0, 0, ClickType.PICKUP, player);
                    helper.assertValueEqual(menu.getCarried().getCount(), 5, "cursor kept");
                    helper.assertTrue(wall.getDisplayed(Direction.UP).is(Items.DIRT), "Expected dirt to be shown");
                    menu.setCarried(ItemStack.EMPTY);
                    menu.clicked(0, 0, ClickType.PICKUP, player);
                    helper.assertTrue(wall.getDisplayed(Direction.UP).isEmpty(), "Expected the display to be cleared");
                    // Shift-clicking an inventory item shows it, and leaves it in the inventory.
                    int stoneSlot = menu.slots.indexOf(menu.slots.stream()
                            .filter(slot -> slot.container == player.getInventory() && slot.getContainerSlot() == 3).findFirst().orElseThrow());
                    menu.quickMoveStack(player, stoneSlot);
                    helper.assertTrue(wall.getDisplayed(Direction.UP).is(Items.STONE), "Expected stone to be shown");
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 20, "stone kept after shift-click");
                    helper.assertValueEqual(wall.getDisplayStats(Direction.UP).count(), 5L, "shown count");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDisplayWallFaces(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos display = placeWall(helper, MIN_A.offset(0, 2, 0), WallType.DISPLAY);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, display);
                    ServerPlayer player = makeViewer(helper);
                    BlockPos absolute = helper.absolutePos(display);
                    helper.assertValueEqual(Set.copyOf(wall.getDisplayFaces()), Set.of(Direction.UP, Direction.NORTH, Direction.WEST), "outer faces");
                    // Each face shows its own item.
                    useWithItem(helper, player, display, STONE.copyWithCount(10), Direction.NORTH);
                    useWithItem(helper, player, display, new ItemStack(Items.DIRT, 5), Direction.WEST);
                    helper.assertTrue(wall.getDisplayed(Direction.NORTH).is(Items.STONE), "Expected stone on the north face");
                    helper.assertTrue(wall.getDisplayed(Direction.WEST).is(Items.DIRT), "Expected dirt on the west face");
                    helper.assertTrue(wall.getDisplayed(Direction.UP).isEmpty(), "Expected nothing on the top face");
                    helper.assertValueEqual(wall.getDisplayStats(Direction.WEST).count(), 5L, "dirt shown on the west face");
                    player.getInventory().clearContent();
                    new ServerboundDisplayTakePacket(absolute, Direction.WEST, true).actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(countInInventory(player, Items.DIRT), 1, "dirt taken from the west face");
                    // The settings have a column per face, and keep at least one face shown.
                    ContainerDisplay menu = new ContainerDisplay(103, player.getInventory(), wall);
                    helper.assertValueEqual(menu.getFaces().size(), 3, "columns");
                    // Each face has its own options, all on at first.
                    helper.assertTrue(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.WEST, DisplayOption.COUNT)), "Expected the west count to toggle");
                    helper.assertFalse(wall.isEnabled(Direction.WEST, DisplayOption.COUNT), "Expected no count on the west face");
                    helper.assertTrue(wall.isEnabled(Direction.UP, DisplayOption.COUNT), "Expected a count on the top face");
                    helper.assertTrue(wall.isEnabled(Direction.WEST, DisplayOption.FILL_LEVEL), "Expected a fill level on the west face");
                    helper.assertTrue(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.NORTH, DisplayOption.SHOWN)), "Expected north to be hidden");
                    helper.assertTrue(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.WEST, DisplayOption.SHOWN)), "Expected west to be hidden");
                    helper.assertTrue(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.UP, DisplayOption.SHOWN)), "Expected all faces to hide");
                    helper.assertTrue(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.UP, DisplayOption.SHOWN)), "Expected the top to show again");
                    helper.assertFalse(menu.clickMenuButton(player, ContainerDisplay.getButton(Direction.SOUTH, DisplayOption.COUNT)), "Expected an inner face to be refused");
                    helper.assertTrue(wall.isFaceHidden(Direction.NORTH), "Expected north to be hidden");
                    helper.assertFalse(wall.isFaceHidden(Direction.UP), "Expected the top to be shown");
                    // A hidden face acts like a plain wall: no inserts, no takes, mined normally.
                    ItemStack stone = STONE.copyWithCount(3);
                    helper.assertValueEqual(useWithItem(helper, player, display, stone, Direction.NORTH),
                            ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, "click on a hidden face");
                    helper.assertValueEqual(stone.getCount(), 3, "stone kept");
                    player.getInventory().clearContent();
                    new ServerboundDisplayTakePacket(absolute, Direction.NORTH, false).actionServer(helper.getLevel(), player);
                    helper.assertValueEqual(countInInventory(player, Items.STONE), 0, "stone taken from a hidden face");
                    helper.assertFalse(DisplayWallInteractions.onAttack(player, helper.getLevel(), absolute, Direction.NORTH),
                            "Expected a hidden face to mine normally");
                    helper.assertTrue(DisplayWallInteractions.onAttack(player, helper.getLevel(), absolute, Direction.UP),
                            "Expected a shown face to be protected");
                    // Faces, items and visibility reach clients.
                    BlockEntityChestWall client = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    client.loadWithComponents(wall.getUpdateTag(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                    helper.assertTrue(client.getDisplayed(Direction.WEST).is(Items.DIRT), "Expected dirt on the client");
                    helper.assertTrue(client.isFaceHidden(Direction.NORTH), "Expected north hidden on the client");
                    helper.assertFalse(client.isEnabled(Direction.WEST, DisplayOption.COUNT), "Expected no west count on the client");
                    helper.assertValueEqual(client.getDisplayStats(Direction.NORTH), wall.getDisplayStats(Direction.NORTH), "north stats on the client");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testDisplayWallOnlyMinedWithAPickaxe(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos display = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.DISPLAY);
        BlockPos interfacePos = placeWall(helper, MIN_A.offset(1, 0, 1), WallType.INTERFACE);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makeViewer(helper);
                    BlockPos absolute = helper.absolutePos(display);
                    for (ItemStack held : List.of(ItemStack.EMPTY, STONE.copy(), new ItemStack(Items.WOODEN_SWORD))) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, held);
                        helper.assertTrue(DisplayWallInteractions.onAttack(player, helper.getLevel(), absolute, Direction.UP),
                                "Expected mining to be cancelled with " + held);
                    }
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
                    helper.assertFalse(DisplayWallInteractions.onAttack(player, helper.getLevel(), absolute, Direction.UP), "Expected a pickaxe to mine");
                    // Only Display walls are protected.
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    helper.assertFalse(DisplayWallInteractions.onAttack(player, helper.getLevel(), helper.absolutePos(interfacePos), Direction.UP),
                            "Expected an Interface to mine normally");
                })
                .thenSucceed();
    }

    // Magnet walls

    private static ItemEntity dropItem(GameTestHelper helper, Vec3 relative, ItemStack stack) {
        Vec3 absolute = helper.absoluteVec(relative);
        ItemEntity item = new ItemEntity(helper.getLevel(), absolute.x, absolute.y, absolute.z, stack.copy(), 0, 0, 0);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testMagnetWallPullsItems(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos wallPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.MAGNET);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    helper.assertTrue(getWall(helper, wallPos).getItemHandlerLogic().isEmpty(), "Expected no item access through a Magnet wall");
                    // Above the chest, and to the side of it at its height.
                    dropItem(helper, Vec3.atCenterOf(wallPos.above(3)), STONE.copyWithCount(16));
                    dropItem(helper, Vec3.atCenterOf(MIN_A.offset(5, 0, 1)), new ItemStack(Items.DIRT, 3));
                    // Items that may not be picked up, such as ones a player just threw, are left alone.
                    dropItem(helper, Vec3.atCenterOf(wallPos.above(2).east(2)), new ItemStack(Items.DIAMOND)).setNeverPickUp();
                })
                .thenWaitUntil(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    helper.assertValueEqual(DisplayStats.of(storage, STONE).count(), 16L, "stone pulled in");
                    helper.assertValueEqual(DisplayStats.of(storage, new ItemStack(Items.DIRT)).count(), 3L, "dirt pulled in");
                })
                .thenExecute(() -> {
                    AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16);
                    helper.assertValueEqual(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, item -> item.getItem().is(Items.STONE)).size(), 0, "stone entities left");
                    helper.assertValueEqual(helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, item -> item.getItem().is(Items.DIAMOND)).size(), 1, "diamond entities left");
                    helper.assertValueEqual(DisplayStats.of(getCore(helper, corePos).getStorage(), new ItemStack(Items.DIAMOND)).count(), 0L, "diamonds pulled in");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testMagnetWallLeavesWhatDoesNotFit(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos wallPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.MAGNET);
        BlockPos dropPos = wallPos.above(2).east(2);
        ItemEntity[] dirt = new ItemEntity[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    // Every slot full of stone.
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    storage.insert(STONE, Long.MAX_VALUE / 4, false);
                    helper.assertValueEqual(storage.insert(new ItemStack(Items.DIRT), 1, true), 0L, "room for dirt");
                    dirt[0] = dropItem(helper, Vec3.atCenterOf(dropPos), new ItemStack(Items.DIRT));
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    helper.assertTrue(dirt[0].isAlive(), "Expected the dirt to stay");
                    // It fell straight down instead of being pulled towards the wall.
                    helper.assertValueEqual(dirt[0].blockPosition().getX(), helper.absolutePos(dropPos).getX(), "dirt x");
                    helper.assertValueEqual(dirt[0].blockPosition().getZ(), helper.absolutePos(dropPos).getZ(), "dirt z");
                })
                .thenSucceed();
    }

    // Redstone walls

    private static int getComparatorOutput(GameTestHelper helper, BlockPos comparatorPos) {
        return ((ComparatorBlockEntity) helper.getBlockEntity(comparatorPos)).getOutputSignal();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRedstoneWallSignal(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos wallPos = placeWall(helper, MIN_A.offset(1, 0, 0), WallType.REDSTONE);
        BlockPos comparatorPos = wallPos.north();
        helper.setBlock(comparatorPos.below(), Blocks.STONE);
        helper.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.SOUTH));
        // A second wall powers a lamp directly, without a comparator.
        BlockPos lampWallPos = placeWall(helper, MIN_A.offset(1, 0, 2), WallType.REDSTONE);
        BlockPos lampPos = lampWallPos.south();
        helper.setBlock(lampPos, Blocks.REDSTONE_LAMP);
        ItemStack dirt = new ItemStack(Items.DIRT);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 0, "signal of an empty chest");
                    helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, false);
                    helper.assertTrue(getWall(helper, wallPos).getItemHandlerLogic().isEmpty(), "Expected no item access through a Redstone wall");
                    getCore(helper, corePos).getStorage().insert(STONE, 1024, false);
                })
                // Without a target, like the core: one full slot of 27.
                .thenWaitUntil(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 1, "whole chest signal"))
                .thenWaitUntil(() -> {
                    helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, true);
                    helper.assertValueEqual(helper.getLevel().getSignal(helper.absolutePos(lampWallPos), Direction.NORTH), 1, "weak power");
                    // Weak power only, like a trapped chest's sides.
                    helper.assertValueEqual(helper.getLevel().getDirectSignal(helper.absolutePos(lampWallPos), Direction.NORTH), 0, "strong power");
                })
                .thenExecute(() -> getWall(helper, wallPos).getRedstoneTarget().setItem(0, dirt.copy()))
                .thenWaitUntil(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 0, "signal without dirt"))
                .thenExecute(() -> {
                    ChestStorage storage = getCore(helper, corePos).getStorage();
                    storage.insert(dirt, storage.getCapacity(dirt), false);
                })
                // Only the dirt slot counts, and it is full.
                .thenWaitUntil(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 15, "signal of a full dirt slot"))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, wallPos);
                    // The target survives saving and loading.
                    BlockEntityChestWall loaded = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    loaded.loadWithComponents(wall.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                    helper.assertTrue(loaded.getRedstoneTarget().getItem(0).is(Items.DIRT), "Expected the target after loading");
                    // The settings set the target through a ghost slot, and show the signal.
                    ServerPlayer player = makeViewer(helper);
                    ContainerRedstone menu = new ContainerRedstone(104, player.getInventory(), wall);
                    player.containerMenu = menu;
                    helper.assertValueEqual(menu.getSignal(), 15, "signal in the settings");
                    menu.setCarried(ItemStack.EMPTY);
                    menu.clicked(0, 0, ClickType.PICKUP, player);
                    helper.assertTrue(wall.getRedstoneTarget().getItem(0).isEmpty(), "Expected the whole chest again");
                    // Only Redstone walls give a signal.
                    BlockState interfaceState = functionalWall(WallType.INTERFACE).defaultBlockState();
                    helper.assertFalse(interfaceState.hasAnalogOutputSignal(), "Expected no signal from an Interface");
                    helper.assertFalse(interfaceState.isSignalSource(), "Expected no power from an Interface");
                })
                // Two full slots of 27.
                .thenWaitUntil(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 2, "whole chest signal again"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 200)
    public void testMagnetWallRadius(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos wallPos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.MAGNET);
        ItemEntity[] dirt = new ItemEntity[1];
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, wallPos);
                    helper.assertValueEqual(wall.getMagnetRadius(), GeneralConfig.getMagnetDefaultRadius(), "default radius");
                    // The settings change it in steps of 1 or 5, within 1 and the configured maximum.
                    ServerPlayer player = makeViewer(helper);
                    ContainerMagnet menu = new ContainerMagnet(105, player.getInventory(), wall);
                    menu.clickMenuButton(player, ContainerMagnet.BUTTON_INCREASE);
                    helper.assertValueEqual(menu.getRadius(), GeneralConfig.getMagnetDefaultRadius() + 1, "radius after +1");
                    menu.clickMenuButton(player, ContainerMagnet.BUTTON_INCREASE_MORE);
                    helper.assertValueEqual(menu.getRadius(), GeneralConfig.getMagnetDefaultRadius() + 1 + 8, "radius after +8");
                    for (int i = 0; i < GeneralConfig.HARD_MAX_MAGNET_RADIUS; i++) {
                        menu.clickMenuButton(player, ContainerMagnet.BUTTON_DECREASE_MORE);
                    }
                    helper.assertValueEqual(wall.getMagnetRadius(), 1, "smallest radius");
                    for (int i = 0; i < GeneralConfig.HARD_MAX_MAGNET_RADIUS; i++) {
                        menu.clickMenuButton(player, ContainerMagnet.BUTTON_INCREASE_MORE);
                    }
                    helper.assertValueEqual(wall.getMagnetRadius(), GeneralConfig.getMagnetMaxRadius(), "largest radius");
                    helper.assertValueEqual(menu.getMaxRadius(), GeneralConfig.getMagnetMaxRadius(), "maximum in the settings");
                    // Radius 1 reaches the blocks next to the wall: dirt on the chest's top, one block from the wall.
                    wall.setMagnetRadius(1);
                    dropItem(helper, Vec3.atBottomCenterOf(wallPos.above().south()), new ItemStack(Items.DIRT, 2));
                })
                .thenWaitUntil(() -> helper.assertValueEqual(DisplayStats.of(getCore(helper, corePos).getStorage(), new ItemStack(Items.DIRT)).count(),
                        2L, "dirt next to the wall pulled in with radius 1"))
                .thenExecute(() -> {
                    BlockEntityChestWall wall = getWall(helper, wallPos);
                    // It survives saving and loading.
                    wall.setMagnetRadius(2);
                    BlockEntityChestWall loaded = new BlockEntityChestWall(wall.getBlockPos(), wall.getBlockState());
                    loaded.loadWithComponents(wall.saveWithoutMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
                    helper.assertValueEqual(loaded.getMagnetRadius(), 2, "radius after loading");
                    // Dirt on a block next to the chest, about 3.4 blocks from the wall's block.
                    helper.setBlock(MIN_A.offset(5, -1, 1), Blocks.STONE);
                    dirt[0] = dropItem(helper, Vec3.atCenterOf(MIN_A.offset(5, 0, 1)), new ItemStack(Items.DIRT));
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    helper.assertTrue(dirt[0].isAlive(), "Expected dirt outside the radius to stay");
                    getWall(helper, wallPos).setMagnetRadius(5);
                })
                .thenWaitUntil(() -> helper.assertValueEqual(DisplayStats.of(getCore(helper, corePos).getStorage(), new ItemStack(Items.DIRT)).count(),
                        3L, "dirt pulled in with a larger radius"))
                .thenSucceed();
    }

    // Nested chests

    @GameTest(template = TEMPLATE_EMPTY)
    public void testFilledCoresCanNotBeStored(GameTestHelper helper) {
        BlockPos corePosA = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos corePosB = buildChest(helper, MIN_B, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    assertFormed(helper, corePosA, MIN_A, 3);
                    assertFormed(helper, corePosB, MIN_B, 3);
                })
                .thenExecute(() -> {
                    helper.assertValueEqual(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(RegistryEntries.COMPONENT_CHEST_CONTENTS.value()),
                            NestedChests.CONTENTS_COMPONENT, "contents component id");
                    getCore(helper, corePosA).getStorage().insert(STONE, 100, false);
                    ItemStack filledCore = breakCoreAndPickUp(helper, corePosA);
                    helper.assertTrue(filledCore.has(RegistryEntries.COMPONENT_CHEST_CONTENTS.value()), "Expected a core with contents");
                    ChestStorage storage = getCore(helper, corePosB).getStorage();
                    // Not through any insert path, and no slot can be locked to it.
                    helper.assertValueEqual(storage.insert(filledCore, 1, false), 0L, "filled core inserted");
                    helper.assertValueEqual(storage.insertAutomated(filledCore, 1, false), 0L, "filled core inserted by automation");
                    helper.assertTrue(ItemStack.matches(new ItemHandlerLogic(storage).insertItem(0, filledCore.copy(), false), filledCore),
                            "Expected an item handler to refuse a filled core");
                    helper.assertFalse(storage.lockTo(0, filledCore), "Expected no lock to a filled core");
                    // Nor inside a shulker box or bundle.
                    ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
                    shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(filledCore)));
                    helper.assertValueEqual(storage.insert(shulker, 1, false), 0L, "shulker box with a filled core inserted");
                    ItemStack bundle = new ItemStack(Items.BUNDLE);
                    bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(shulker)));
                    helper.assertValueEqual(storage.insert(bundle, 1, false), 0L, "bundle with a filled core inserted");
                    // Empty cores and other containers are fine.
                    ItemStack emptyCore = new ItemStack(filledCore.getItem());
                    helper.assertValueEqual(storage.insert(emptyCore, 1, false), 1L, "empty core inserted");
                    ItemStack stoneShulker = new ItemStack(Items.SHULKER_BOX);
                    stoneShulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(STONE.copyWithCount(64))));
                    helper.assertValueEqual(storage.insert(stoneShulker, 1, false), 1L, "shulker box with stone inserted");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRecipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getRecipeManager();
        // Every item of this mod is craftable, besides test content.
        Set<Item> results = recipes.getRecipes().stream()
                .map(r -> r.value().getResultItem(level.registryAccess()).getItem())
                .collect(Collectors.toSet());
        Set<Item> testItems = Set.of(wall(GameTestAddon.MATERIAL).asItem(), core(GameTestAddon.MATERIAL).asItem());
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Reference.MOD_ID) && !testItems.contains(item)) {
                helper.assertTrue(results.contains(item), "No recipe for " + item);
            }
        }

        ItemStack ironWall = modItem("chest_wall_iron");
        ItemStack i = new ItemStack(Items.IRON_INGOT);
        ItemStack l = new ItemStack(Items.OAK_LOG);
        // Plain walls make two.
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(i, i, i, i, l, i, i, i, i)), ironWall.copyWithCount(2));
        ItemStack p = new ItemStack(Items.BIRCH_PLANKS);
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(p, p, p, p, new ItemStack(Items.SPRUCE_LOG), p, p, p, p)),
                modItem("chest_wall_wood").copyWithCount(2));
        assertCrafts(helper, CraftingInput.of(2, 1, List.of(ironWall, new ItemStack(Items.CHEST))), modItem("chest_core_iron"));
        assertCrafts(helper, CraftingInput.of(2, 1, List.of(modItem("chest_wall_wood"), new ItemStack(Items.ACACIA_PLANKS))),
                modItem("uncolossal_chest"));
        // Functional walls accept any plain wall.
        assertCrafts(helper, CraftingInput.of(2, 1, List.of(modItem("chest_wall_gold"), new ItemStack(Items.HOPPER))),
                modItem("chest_wall_interface"));
        assertCrafts(helper, CraftingInput.of(2, 2, List.of(ironWall, new ItemStack(Items.ENDER_PEARL), i, new ItemStack(Items.REDSTONE))),
                modItem("chest_wall_magnet"));
        assertCrafts(helper, CraftingInput.of(2, 1, List.of(modItem("chest_wall_display"), new ItemStack(Items.HOPPER))), null);
        // Upgrades are built around any plain wall, not a chest, so they do not clash with other mods.
        ItemStack d = new ItemStack(Items.DIAMOND);
        ItemStack w = modItem("chest_wall_wood");
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(i, d, i, d, w, d, i, d, i)), modItem("upgrade_depth"));
        ItemStack c = new ItemStack(Items.CHEST);
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(i, d, i, d, c, d, i, d, i)), null);

        // Obsidian walls build on diamond walls, and netherite walls on obsidian walls.
        ItemStack dw = modItem("chest_wall_diamond");
        ItemStack o = new ItemStack(Items.OBSIDIAN);
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(dw, o, dw, o, ItemStack.EMPTY, o, dw, o, dw)),
                modItem("chest_wall_obsidian").copyWithCount(4));
        assertCrafts(helper, CraftingInput.of(3, 3, List.of(o, o, o, o, new ItemStack(Items.OAK_LOG), o, o, o, o)), null);
        SmithingRecipeInput fromDiamond = new SmithingRecipeInput(new ItemStack(Items.GOLD_INGOT), dw, new ItemStack(Items.NETHERITE_SCRAP));
        helper.assertTrue(recipes.getRecipeFor(RecipeType.SMITHING, fromDiamond, level).isEmpty(), "Expected no netherite walls from diamond walls");
        SmithingRecipeInput smithing = new SmithingRecipeInput(new ItemStack(Items.GOLD_INGOT), modItem("chest_wall_obsidian"),
                new ItemStack(Items.NETHERITE_SCRAP));
        ItemStack netherite = recipes.getRecipeFor(RecipeType.SMITHING, smithing, level)
                .map(r -> r.value().assemble(smithing, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(ItemStack.isSameItem(netherite, modItem("chest_wall_netherite")) && netherite.getCount() == 2,
                "Expected two netherite walls, got " + netherite);
        helper.succeed();
    }

    private static ItemStack modItem(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path)));
    }

    private static void assertCrafts(GameTestHelper helper, CraftingInput input, @Nullable ItemStack expected) {
        ServerLevel level = helper.getLevel();
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(r -> r.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        if (expected == null) {
            helper.assertTrue(result.isEmpty(), "Expected no result, got " + result);
        } else {
            helper.assertTrue(ItemStack.isSameItem(result, expected) && result.getCount() == expected.getCount(),
                    "Expected " + expected + ", got " + result);
        }
    }

    // Material upgrades

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMaterialUpgradeWoodToNetherite(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos interfacePos = placeWall(helper, MIN_A.offset(1, 2, 1), WallType.INTERFACE);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(STONE, 100, false);
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.SLOT_EXPANSION));
                    int slots = core.getStorage().getSlotCount();
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    for (int i = 1; i < ChestMaterial.getAll().size(); i++) {
                        ChestMaterial previous = ChestMaterial.getAll().get(i - 1);
                        ChestMaterial target = ChestMaterial.getAll().get(i);
                        // 24 plain walls and the core, the interface stays.
                        giveWalls(player, target, 25);
                        helper.assertTrue(useMaterialUpgrade(helper, player, corePos, target).consumesAction(),
                                "Expected an upgrade to " + target.getName());
                        helper.assertBlockPresent(core(target), corePos);
                        for (BlockPos pos : new ChestStructure(MIN_A, 3).shell()) {
                            if (!pos.equals(corePos) && !pos.equals(interfacePos)) {
                                helper.assertBlockPresent(wall(target), pos);
                            }
                        }
                        helper.assertBlockPresent(functionalWall(WallType.INTERFACE), interfacePos);
                        assertFormed(helper, corePos, MIN_A, 3);
                        helper.assertBlockProperty(interfacePos, BlockChestWall.FORMED, true);
                        BlockEntityChestCore upgraded = getCore(helper, corePos);
                        helper.assertValueEqual(countStored(upgraded.getStorage(), STONE), 100L, "stone in " + target.getName());
                        helper.assertValueEqual(upgraded.getUpgradeSet().count(ChestUpgrades.SLOT_EXPANSION), 1, "upgrades in " + target.getName());
                        helper.assertValueEqual(upgraded.getStorage().getSlotCount(), slots, "slots in " + target.getName());
                        helper.assertValueEqual(upgraded.getUpgrades().getContainerSize(), target.getProperties().upgradeSlots(), "upgrade slots");
                        helper.assertValueEqual(countInInventory(player, wall(target).asItem()), 0, "new walls left");
                        helper.assertValueEqual(countInInventory(player, wall(previous).asItem()), 25, "old walls returned");
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMaterialUpgradeRefusals(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    getCore(helper, corePos).getStorage().insert(STONE, 100, false);
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    Item diamondWall = wall(ChestMaterial.DIAMOND).asItem();
                    // A tool without a material does nothing.
                    giveWalls(player, ChestMaterial.DIAMOND, 26);
                    helper.assertFalse(useMaterialUpgrade(helper, player, corePos, null).consumesAction(),
                            "Expected a tool without a material to fail");
                    helper.assertFalse(useMaterialUpgrade(helper, player, corePos, ChestMaterial.WOOD).consumesAction(),
                            "Expected a change to the same material to fail");
                    // One wall short of 26 blocks, and named walls are never taken.
                    player.getInventory().clearContent();
                    giveWalls(player, ChestMaterial.DIAMOND, 25);
                    ItemStack named = new ItemStack(diamondWall, 64);
                    named.set(DataComponents.CUSTOM_NAME, Component.literal("Keep"));
                    player.getInventory().placeItemBackInInventory(named);
                    helper.assertFalse(useMaterialUpgrade(helper, player, corePos, ChestMaterial.DIAMOND).consumesAction(),
                            "Expected the upgrade to fail without enough walls");
                    helper.assertValueEqual(countInInventory(player, diamondWall), 25 + 64, "diamond walls after failing");
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.WOOD).asItem()), 0, "wood walls after failing");
                    helper.assertBlockPresent(core(ChestMaterial.WOOD), corePos);
                    helper.assertBlockPresent(wall(ChestMaterial.WOOD), MIN_A);
                    // Exactly enough, skipping the tiers in between.
                    giveWalls(player, ChestMaterial.DIAMOND, 1);
                    helper.assertTrue(useMaterialUpgrade(helper, player, corePos, ChestMaterial.DIAMOND).consumesAction(),
                            "Expected the upgrade to succeed");
                    helper.assertBlockPresent(core(ChestMaterial.DIAMOND), corePos);
                    helper.assertValueEqual(countInInventory(player, diamondWall), 64, "named diamond walls left");
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.WOOD).asItem()), 26, "wood walls returned");
                    helper.assertValueEqual(countStored(getCore(helper, corePos).getStorage(), STONE), 100L, "stone");
                    // Creative players neither pay nor get walls back.
                    player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
                    helper.assertTrue(useMaterialUpgrade(helper, player, corePos, ChestMaterial.IRON).consumesAction(),
                            "Expected a creative change to succeed");
                    helper.assertBlockPresent(core(ChestMaterial.IRON), corePos);
                    helper.assertValueEqual(countInInventory(player, diamondWall), 64, "diamond walls after creative");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMaterialDowngrade(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.COPPER);
        BlockPos largeCorePos = buildChest(helper, MIN_B, 4, ChestMaterial.COPPER);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    assertFormed(helper, corePos, MIN_A, 3);
                    assertFormed(helper, largeCorePos, MIN_B, 4);
                })
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    core.getStorage().insert(STONE, 100, false);
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    giveWalls(player, ChestMaterial.WOOD, 56);
                    // Wood has one upgrade slot.
                    core.getUpgrades().setItem(0, upgradeItem(ChestUpgrades.SLOT_EXPANSION));
                    core.getUpgrades().setItem(1, upgradeItem(ChestUpgrades.DEPTH));
                    helper.assertFalse(useMaterialUpgrade(helper, player, corePos, ChestMaterial.WOOD).consumesAction(),
                            "Expected a downgrade with too many upgrades to fail");
                    // Wood takes no Depth.
                    core.getUpgrades().setItem(0, ItemStack.EMPTY);
                    helper.assertFalse(useMaterialUpgrade(helper, player, corePos, ChestMaterial.WOOD).consumesAction(),
                            "Expected a downgrade with Depth to fail");
                    // Too large for wood.
                    helper.assertFalse(useMaterialUpgrade(helper, player, largeCorePos, ChestMaterial.WOOD).consumesAction(),
                            "Expected a downgrade of a 4x4 chest to fail");
                    helper.assertBlockPresent(core(ChestMaterial.COPPER), corePos);
                    helper.assertBlockPresent(core(ChestMaterial.COPPER), largeCorePos);
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.WOOD).asItem()), 56, "wood walls after failures");
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.COPPER).asItem()), 0, "copper walls after failures");
                    // The upgrade in the second slot moves to wood's only slot.
                    core.getUpgrades().setItem(1, upgradeItem(ChestUpgrades.SLOT_EXPANSION));
                    helper.assertTrue(useMaterialUpgrade(helper, player, corePos, ChestMaterial.WOOD).consumesAction(),
                            "Expected the downgrade to succeed");
                    helper.assertBlockPresent(core(ChestMaterial.WOOD), corePos);
                    helper.assertBlockPresent(wall(ChestMaterial.WOOD), MIN_A);
                    assertFormed(helper, corePos, MIN_A, 3);
                    BlockEntityChestCore downgraded = getCore(helper, corePos);
                    helper.assertValueEqual(downgraded.getUpgrades().getContainerSize(), 1, "wood upgrade slots");
                    helper.assertTrue(ItemChestUpgrade.getUpgrade(downgraded.getUpgrades().getItem(0)) == ChestUpgrades.SLOT_EXPANSION,
                            "Expected the slot expansion in the first slot");
                    helper.assertValueEqual(countStored(downgraded.getStorage(), STONE), 100L, "stone");
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.WOOD).asItem()), 30, "wood walls left");
                    helper.assertValueEqual(countInInventory(player, wall(ChestMaterial.COPPER).asItem()), 26, "copper walls returned");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMaterialUpgradeToolMenu(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack tool = new ItemStack(materialUpgradeTool());
        player.setItemInHand(InteractionHand.OFF_HAND, tool);
        ContainerMaterialUpgradeTool menu = new ContainerMaterialUpgradeTool(0, player.getInventory(), InteractionHand.OFF_HAND);
        helper.assertValueEqual(menu.getTarget(), -1, "target without a material");
        helper.assertTrue(menu.clickMenuButton(player, ChestMaterial.getAll().indexOf(ChestMaterial.DIAMOND)), "Expected the button to work");
        helper.assertValueEqual(ItemMaterialUpgradeTool.getTarget(player.getOffhandItem()).orElse(null), ChestMaterial.DIAMOND, "target");
        helper.assertValueEqual(menu.getTarget(), ChestMaterial.getAll().indexOf(ChestMaterial.DIAMOND), "menu target");
        helper.assertFalse(menu.clickMenuButton(player, ChestMaterial.getAll().size()), "Expected an unknown button to fail");
        helper.assertTrue(menu.stillValid(player), "Expected the menu to be valid while holding the tool");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.assertFalse(menu.stillValid(player), "Expected the menu to close without the tool");
        helper.succeed();
    }

    private static Item materialUpgradeTool() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "material_upgrade_tool"));
    }

    private static void giveWalls(ServerPlayer player, ChestMaterial material, int count) {
        Item item = wall(material).asItem();
        for (int remaining = count; remaining > 0; remaining -= item.getDefaultMaxStackSize()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(item, Math.min(remaining, item.getDefaultMaxStackSize())));
        }
    }

    /**
     * Right-click a block with a Material Upgrade Tool like the server does for a player.
     * @param target The tool's material, or null for none.
     */
    private static InteractionResult useMaterialUpgrade(GameTestHelper helper, ServerPlayer player, BlockPos pos, @Nullable ChestMaterial target) {
        // The off hand keeps the tool out of the inventory slots that pay.
        ItemStack tool = new ItemStack(materialUpgradeTool());
        if (target != null) {
            ItemMaterialUpgradeTool.setTarget(tool, target);
        }
        player.setItemInHand(InteractionHand.OFF_HAND, tool);
        BlockPos absolute = helper.absolutePos(pos);
        InteractionResult result = player.gameMode.useItemOn(player, helper.getLevel(), tool, InteractionHand.OFF_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.NORTH, absolute, false));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        return result;
    }

    // Uncolossal chest

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestMenuAndLid(GameTestHelper helper) {
        BlockPos pos = placeUncolossalChest(helper, Direction.EAST);
        BlockEntityUncolossalChest chest = getUncolossalChest(helper, pos);
        helper.assertValueEqual(chest.getContainerSize(), 5, "slots");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ContainerUncolossalChest menu = new ContainerUncolossalChest(1, player.getInventory(), chest);
        player.containerMenu = menu;
        helper.assertValueEqual(menu.getType(), MenuType.HOPPER, "menu type");
        helper.assertValueEqual(menu.slots.size(), 5 + 36, "menu slots");
        helper.assertValueEqual(chest.getOpenerCount(), 1, "openers while open");
        // Shift-clicking stone from the player inventory fills the chest.
        player.getInventory().setItem(0, new ItemStack(Items.STONE, 64));
        menu.quickMoveStack(player, 5 + 27);
        helper.assertValueEqual(chest.getItem(0).getCount(), 64, "moved into the chest");
        player.closeContainer();
        helper.assertValueEqual(chest.getOpenerCount(), 0, "openers after closing");
        helper.assertBlockProperty(pos, BlockUncolossalChest.FACING, Direction.EAST);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestDropsContentsAndKeepsName(GameTestHelper helper) {
        BlockPos pos = placeUncolossalChest(helper, Direction.NORTH);
        BlockEntityUncolossalChest chest = getUncolossalChest(helper, pos);
        // Like placing a renamed item, which also sets the (here empty) contents.
        chest.applyComponents(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME, Component.literal("Tiny")).build(), DataComponentPatch.EMPTY);
        chest.setItem(0, new ItemStack(Items.STONE, 10));
        chest.setItem(4, new ItemStack(Items.DIAMOND, 3));
        helper.assertValueEqual(chest.getDisplayName().getString(), "Tiny", "name");
        helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
        List<ItemStack> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(pos)).inflate(2))
                .stream().map(ItemEntity::getItem).toList();
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(Items.STONE) && stack.getCount() == 10), "Expected the stone to drop, got " + drops);
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 3), "Expected the diamonds to drop");
        helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().asItem())
                && "Tiny".equals(stack.getHoverName().getString())), "Expected the named chest to drop, got " + drops);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestComparator(GameTestHelper helper) {
        BlockPos pos = placeUncolossalChest(helper, Direction.NORTH);
        BlockPos comparatorPos = pos.south();
        helper.setBlock(comparatorPos.below(), Blocks.STONE);
        helper.setBlock(comparatorPos, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.NORTH));
        BlockEntityUncolossalChest chest = getUncolossalChest(helper, pos);
        helper.startSequence()
                .thenExecute(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 0, "empty signal"))
                .thenExecute(() -> {
                    for (int slot = 0; slot < 5; slot++) {
                        chest.setItem(slot, new ItemStack(Items.STONE, 64));
                    }
                })
                .thenWaitUntil(() -> helper.assertValueEqual(getComparatorOutput(helper, comparatorPos), 15, "full signal"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestHoppers(GameTestHelper helper) {
        BlockPos pos = placeUncolossalChest(helper, Direction.NORTH);
        HopperBlockEntity above = placeHopper(helper, pos.above(), new ItemStack(Items.STONE, 5));
        helper.setBlock(pos.below(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.NORTH));
        BlockPos sinkPos = pos.below().north();
        helper.setBlock(sinkPos, Blocks.CHEST);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(countInHopper(above, Items.STONE), 0, "stone left in the top hopper");
                    helper.assertTrue(helper.getBlockEntity(sinkPos) instanceof Container sink && sink.countItem(Items.STONE) == 5,
                            "Expected all stone to pass through the chest");
                })
                .thenSucceed();
    }

    private static BlockPos placeUncolossalChest(GameTestHelper helper, Direction facing) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState().setValue(BlockUncolossalChest.FACING, facing));
        return pos;
    }

    private static BlockEntityUncolossalChest getUncolossalChest(GameTestHelper helper, BlockPos pos) {
        if (!(helper.getBlockEntity(pos) instanceof BlockEntityUncolossalChest chest)) {
            throw new GameTestAssertException("No uncolossal chest at " + pos);
        }
        return chest;
    }

    // Tooltips

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWallAndCoreTooltipsShowMaterialLimits(GameTestHelper helper) {
        for (ChestMaterial material : ChestMaterial.getAll()) {
            for (Block block : List.of(wall(material), core(material))) {
                List<Component> lines = new ItemStack(block).getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL);
                helper.assertTrue(lines.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents
                                && contents.getKey().equals("material.colossalchests2.limits")
                                && List.of(contents.getArgs()).equals(List.of(material.getProperties().maxSize(), material.getProperties().upgradeSlots()))),
                        "Expected the limits of " + material.getName() + " on " + block + ", got " + lines);
            }
        }
        helper.succeed();
    }

    // Advancements

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementChestFormed(GameTestHelper helper) {
        // The player is nearby before the last block goes in, like a player building the chest.
        ServerPlayer player = makePlayerNorthOf(helper, MIN_A.offset(1, 1, 0));
        BlockPos corePosA = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos corePosB = buildChest(helper, MIN_B, 3, ChestMaterial.COPPER);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    assertFormed(helper, corePosA, MIN_A, 3);
                    assertFormed(helper, corePosB, MIN_B, 3);
                })
                .thenExecute(() -> {
                    assertAdvancement(helper, player, "formed/wood", true);
                    // 3x3 is the largest wooden chest, but not the largest copper one.
                    assertAdvancement(helper, player, "largest/wood", true);
                    assertAdvancement(helper, player, "formed/copper", true);
                    assertAdvancement(helper, player, "largest/copper", false);
                    assertAdvancement(helper, player, "formed/iron", false);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementChestFormedOutOfRange(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos far = helper.absolutePos(MIN_A).offset(0, 0, -(BlockEntityChestCore.FORMED_TRIGGER_RANGE + 5));
        player.moveTo(far.getX(), far.getY(), far.getZ());
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.GOLD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> assertAdvancement(helper, player, "formed/gold", false))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMaterialChanged(GameTestHelper helper) {
        BlockPos corePos = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 3))
                .thenExecute(() -> {
                    ServerPlayer player = makePlayerNorthOf(helper, corePos);
                    player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    assertAdvancement(helper, player, "material_changed", false);
                    giveWalls(player, ChestMaterial.DIAMOND, 26);
                    helper.assertTrue(useMaterialUpgrade(helper, player, corePos, ChestMaterial.DIAMOND).consumesAction(),
                            "Expected the change to diamond");
                    assertAdvancement(helper, player, "material_changed", true);
                    // Changing material also counts as forming a chest of the new material.
                    assertAdvancement(helper, player, "formed/diamond", true);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRootAndUncolossal(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ResourceLocation wallRecipe = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_wall_iron");
        assertAdvancement(helper, player, "root", false);
        helper.assertFalse(player.getRecipeBook().contains(wallRecipe), "Expected the recipe to be locked");
        // Picking up a chest unlocks the tab and the recipes.
        player.getInventory().add(new ItemStack(Items.CHEST));
        player.inventoryMenu.broadcastChanges();
        assertAdvancement(helper, player, "root", true);
        helper.assertTrue(player.getRecipeBook().contains(wallRecipe), "Expected the recipe to be unlocked");
        assertAdvancement(helper, player, "uncolossal", false);
        player.getInventory().add(new ItemStack(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value()));
        player.inventoryMenu.broadcastChanges();
        assertAdvancement(helper, player, "uncolossal", true);
        helper.succeed();
    }

    private static void assertAdvancement(GameTestHelper helper, ServerPlayer player, String path, boolean done) {
        AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path));
        helper.assertTrue(advancement != null, "Expected advancement " + path + " to exist");
        helper.assertValueEqual(player.getAdvancements().getOrStartProgress(advancement).isDone(), done, "advancement " + path + " done");
    }

    // Sounds

    @GameTest(template = TEMPLATE_EMPTY)
    public void testChestSoundsBySize(GameTestHelper helper) {
        BlockPos corePosA = buildChest(helper, MIN_A, 3, ChestMaterial.WOOD);
        BlockPos corePosB = buildChest(helper, MIN_B, 4, ChestMaterial.COPPER);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    assertFormed(helper, corePosA, MIN_A, 3);
                    assertFormed(helper, corePosB, MIN_B, 4);
                })
                .thenExecute(() -> {
                    ListeningPlayer listening = makeListeningPlayer(helper);
                    ServerPlayer player = listening.player();
                    EmbeddedChannel channel = listening.channel();
                    assertSounds(helper, channel, MIN_A, 3, () -> openChest(helper, player, corePosA), SoundEvents.CHEST_OPEN);
                    assertSounds(helper, channel, MIN_A, 3, player::closeContainer, SoundEvents.CHEST_CLOSE);
                    assertSounds(helper, channel, MIN_B, 4, () -> openChest(helper, player, corePosB),
                            SoundEvents.CHEST_OPEN);
                    // A second viewer makes no sound.
                    ServerPlayer other = makeViewer(helper);
                    assertSounds(helper, channel, MIN_B, 4, () -> openChest(helper, other, corePosB));
                    assertSounds(helper, channel, MIN_B, 4, player::closeContainer);
                    assertSounds(helper, channel, MIN_B, 4, other::closeContainer,
                            SoundEvents.CHEST_CLOSE);
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestSounds(GameTestHelper helper) {
        BlockPos pos = placeUncolossalChest(helper, Direction.NORTH);
        BlockEntityUncolossalChest chest = getUncolossalChest(helper, pos);
        ListeningPlayer listening = makeListeningPlayer(helper);
        ServerPlayer player = listening.player();
        assertSounds(helper, listening.channel(), pos, 1, () -> player.containerMenu = new ContainerUncolossalChest(1, player.getInventory(), chest),
                SoundEvents.CHEST_OPEN);
        assertSounds(helper, listening.channel(), pos, 1, player::closeContainer, SoundEvents.CHEST_CLOSE);
        helper.succeed();
    }

    /**
     * Like {@link GameTestHelper#makeMockServerPlayerInLevel()}, but the packets it receives can be read from the channel.
     */
    private static ListeningPlayer makeListeningPlayer(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-sound-player"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        BlockPos near = helper.absolutePos(MIN_A.offset(1, 0, -2));
        player.moveTo(near.getX() + 0.5, near.getY(), near.getZ() + 0.5);
        return new ListeningPlayer(player, channel);
    }

    private record ListeningPlayer(ServerPlayer player, EmbeddedChannel channel) {
    }

    /**
     * Run an action and check the sounds it plays at the center of a chest, all at that size's pitch and volume.
     */
    private static void assertSounds(GameTestHelper helper, EmbeddedChannel channel, BlockPos min, int size, Runnable action, SoundEvent... expected) {
        drainSounds(helper, channel, min, size);
        action.run();
        List<ClientboundSoundPacket> sounds = drainSounds(helper, channel, min, size);
        helper.assertValueEqual(sounds.stream().map(packet -> packet.getSound().value()).toList(), List.of(expected), "sounds");
        for (ClientboundSoundPacket packet : sounds) {
            float pitch = ChestSounds.getPitch(size);
            helper.assertTrue(packet.getPitch() >= pitch * 0.95F - 0.001F && packet.getPitch() <= pitch * 1.05F + 0.001F,
                    "Expected a pitch around " + pitch + ", got " + packet.getPitch());
            helper.assertTrue(packet.getSource() == SoundSource.BLOCKS, "Expected a block sound");
        }
        if (sounds.size() > 0) {
            helper.assertValueEqual(sounds.get(0).getVolume(), ChestSounds.getVolume(size), "volume");
        }
    }

    private static List<ClientboundSoundPacket> drainSounds(GameTestHelper helper, EmbeddedChannel channel, BlockPos min, int size) {
        Vec3 center = Vec3.atLowerCornerOf(helper.absolutePos(min)).add(size / 2D, size / 2D, size / 2D);
        List<ClientboundSoundPacket> sounds = Lists.newArrayList();
        Object message;
        while ((message = channel.readOutbound()) != null) {
            // Packet positions are floats, which lose precision this far from the world origin.
            if (message instanceof ClientboundSoundPacket packet && center.distanceToSqr(packet.getX(), packet.getY(), packet.getZ()) < 4) {
                sounds.add(packet);
            }
        }
        return sounds;
    }

    // Build command

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBuildCommandAtPosition(GameTestHelper helper) {
        BlockPos min = helper.absolutePos(MIN_A);
        List<Component> messages = runCommand(helper, Vec3.atCenterOf(helper.absolutePos(MIN_A.offset(2, 0, -1))), 0, Commands.LEVEL_GAMEMASTERS,
                "colossalchests2 build iron 5 " + min.getX() + " " + min.getY() + " " + min.getZ());
        assertMessage(helper, messages, "command.colossalchests2.build.success");
        // The core is on the side facing the source.
        BlockPos corePos = MIN_A.offset(2, 2, 0);
        helper.assertBlockPresent(core(ChestMaterial.IRON), corePos);
        assertFormed(helper, corePos, MIN_A, 5);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBuildCommandInFront(GameTestHelper helper) {
        // Looking south, so the chest goes from z 2 to 4, centered on x 5.
        List<Component> messages = runCommand(helper, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 1, 0))), 0,
                Commands.LEVEL_GAMEMASTERS, "colossalchests2 build wood 3");
        assertMessage(helper, messages, "command.colossalchests2.build.success");
        assertFormed(helper, new BlockPos(5, 2, 2), new BlockPos(4, 1, 2), 3);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testBuildCommandRefusals(GameTestHelper helper) {
        BlockPos min = helper.absolutePos(MIN_A);
        String at = " " + min.getX() + " " + min.getY() + " " + min.getZ();
        Vec3 source = Vec3.atCenterOf(helper.absolutePos(MIN_A.offset(2, 0, -1)));

        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build wood 4" + at),
                "command.colossalchests2.build.too_large");
        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build plastic 3" + at),
                "command.colossalchests2.build.unknown_material");
        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS,
                "colossalchests2 build wood 3 " + min.getX() + " " + (helper.getLevel().getMaxBuildHeight() - 1) + " " + min.getZ()),
                "command.colossalchests2.build.not_loaded");
        runCommand(helper, source, 0, Commands.LEVEL_ALL, "colossalchests2 build wood 3" + at);
        helper.assertBlockNotPresent(wall(ChestMaterial.WOOD), MIN_A);

        // A chest with items on the shell and stone inside are in the way.
        helper.setBlock(MIN_A.offset(2, 2, 2), Blocks.STONE);
        helper.setBlock(MIN_A, Blocks.CHEST);
        if (helper.getBlockEntity(MIN_A) instanceof Container chest) {
            chest.setItem(0, new ItemStack(Items.DIAMOND));
        }
        List<Component> messages = runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build iron 5" + at);
        assertMessage(helper, messages, "command.colossalchests2.build.obstructed");
        TranslatableContents obstructed = getTranslation(messages.getFirst());
        helper.assertValueEqual(List.of(obstructed.getArgs()), List.of(2, min.getX(), min.getY(), min.getZ()), "obstruction count and first position");
        helper.assertBlockNotPresent(wall(ChestMaterial.IRON), MIN_A.offset(4, 4, 4));

        // Replacing overwrites them without dropping anything.
        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build iron 5" + at + " replace"),
                "command.colossalchests2.build.success");
        assertFormed(helper, MIN_A.offset(2, 2, 0), MIN_A, 5);
        helper.assertBlockPresent(Blocks.AIR, MIN_A.offset(2, 2, 2));
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(min).inflate(8));
        helper.assertTrue(drops.isEmpty(), "Expected no drops, got " + drops);
        helper.succeed();
    }

    /**
     * Run a command as a non-player source.
     * @return The messages sent back to the source.
     */
    private static List<Component> runCommand(GameTestHelper helper, Vec3 position, float yRot, int permission, String command) {
        List<Component> messages = Lists.newArrayList();
        CommandSource output = new CommandSource() {
            @Override
            public void sendSystemMessage(Component message) {
                messages.add(message);
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack source = new CommandSourceStack(output, position, new Vec2(0, yRot), helper.getLevel(), permission,
                "test", Component.literal("test"), server, null);
        server.getCommands().performPrefixedCommand(source, command);
        return messages;
    }

    /**
     * @return The translation of a message, which failures wrap in a styled empty component.
     */
    private static TranslatableContents getTranslation(Component message) {
        Component inner = message.getContents() instanceof TranslatableContents || message.getSiblings().isEmpty() ? message : message.getSiblings().getFirst();
        return inner.getContents() instanceof TranslatableContents contents ? contents : null;
    }

    private static void assertMessage(GameTestHelper helper, List<Component> messages, String key) {
        helper.assertTrue(messages.size() == 1 && getTranslation(messages.getFirst()) != null
                && getTranslation(messages.getFirst()).getKey().equals(key), "Expected one message " + key + ", got " + messages);
    }

    // Addon materials

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAddonMaterialFormsChest(GameTestHelper helper) {
        ChestMaterial material = GameTestAddon.MATERIAL;
        helper.assertValueEqual(ChestMaterial.byId(material.id()).orElse(null), material, "registered material");
        helper.assertTrue(ChestMaterial.getAll().indexOf(material) == ChestMaterial.getAll().indexOf(ChestMaterial.GOLD) + 1,
                "Expected the material between gold and diamond, got " + ChestMaterial.getAll());
        helper.assertTrue(wall(material) instanceof BlockChestWall wall && wall.getMaterial() == material, "Expected a wall block");
        helper.assertTrue(core(material) instanceof BlockChestCore core && core.getMaterial() == material, "Expected a core block");
        BlockPos corePos = buildChest(helper, MIN_A, 5, material);
        helper.startSequence()
                .thenWaitUntil(() -> assertFormed(helper, corePos, MIN_A, 5))
                .thenExecute(() -> {
                    BlockEntityChestCore core = getCore(helper, corePos);
                    helper.assertValueEqual(core.getUpgrades().getContainerSize(), 4, "upgrade slots");
                    // Set by the material, as the depth upgrade has no limit for it.
                    helper.assertValueEqual(core.getMaxUpgradeCount(ChestUpgrades.DEPTH), 3, "depth limit");
                    helper.assertTrue(core.getDisplayName().getContents() instanceof TranslatableContents title
                                    && title.getArgs()[0] instanceof Component name && name.getContents() instanceof TranslatableContents key
                                    && key.getKey().equals("material.colossalchests2test.test_addon"),
                            "Expected the material name in the title, got " + core.getDisplayName());
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAddonMaterialInCommandAndTool(GameTestHelper helper) {
        ChestMaterial material = GameTestAddon.MATERIAL;
        BlockPos min = helper.absolutePos(MIN_A);
        String at = " " + min.getX() + " " + min.getY() + " " + min.getZ();
        Vec3 source = Vec3.atCenterOf(helper.absolutePos(MIN_A.offset(2, 0, -1)));
        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build " + material.id() + " 6" + at),
                "command.colossalchests2.build.too_large");
        assertMessage(helper, runCommand(helper, source, 0, Commands.LEVEL_GAMEMASTERS, "colossalchests2 build test_addon 5" + at),
                "command.colossalchests2.build.success");
        assertFormed(helper, MIN_A.offset(2, 2, 0), MIN_A, 5);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(materialUpgradeTool()));
        ContainerMaterialUpgradeTool menu = new ContainerMaterialUpgradeTool(0, player.getInventory(), InteractionHand.OFF_HAND);
        helper.assertTrue(menu.clickMenuButton(player, ChestMaterial.getAll().indexOf(material)), "Expected the button to work");
        helper.assertValueEqual(ItemMaterialUpgradeTool.getTarget(player.getOffhandItem()).orElse(null), material, "target");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testChestTablesFromDataAndSynced(GameTestHelper helper) {
        ChestTables tables = ChestTablesLoader.get();
        // Loaded from this mod's data files, there are no material values in code.
        helper.assertValueEqual(tables.getMaterial(ChestMaterial.NETHERITE.id()).maxSize(), 10, "netherite size");
        helper.assertValueEqual(tables.getMaterial(ChestMaterial.NETHERITE.id()).after(), Optional.of(ChestMaterial.OBSIDIAN.id()), "netherite after");
        helper.assertValueEqual(ChestMaterial.getAll().subList(0, 3), List.of(ChestMaterial.WOOD, ChestMaterial.COPPER, ChestMaterial.IRON), "order");
        ClientboundChestTablesPacket packet = roundTrip(helper, new ClientboundChestTablesPacket(tables), ClientboundChestTablesPacket.CODEC);
        helper.assertValueEqual(packet.getTables(), tables, "synced tables");
        helper.succeed();
    }

}
