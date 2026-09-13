package org.cyclops.colossalchests.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.inventory.LargeInventoryCommon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;

/**
 * Performance benchmarks for colossal chests.
 *
 * These are skipped unless the {@code PERFORMANCE_BENCHMARK_ENABLED} environment variable is set to {@code true},
 * as they are significantly slower than regular game tests.
 * See PERFORMANCE_BENCHMARKING.md for details.
 *
 * @author rubensworks
 */
public class GameTestsPerformance {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 0, 1);

    /**
     * The default file that benchmark results are appended to, relative to the run directory.
     */
    public static final String DEFAULT_OUTPUT_FILE = "logs/benchmark_results.txt";

    private static final int WARMUP_ITERATIONS = 1;
    private static final int MEASURE_ITERATIONS = 3;

    protected static boolean isEnabled() {
        return "true".equalsIgnoreCase(System.getenv("PERFORMANCE_BENCHMARK_ENABLED"));
    }

    protected static void report(String preset, int size, double averageMillis) {
        report(String.format(Locale.ROOT, "preset=%s size=%d avgOperationTime=%.6f", preset, size, averageMillis));
    }

    protected static void reportBytes(String preset, int size, long bytesPerSlot) {
        report(String.format(Locale.ROOT, "preset=%s size=%d bytesPerSlot=%d", preset, size, bytesPerSlot));
    }

    protected static void report(String line) {
        System.out.println("[BENCHMARK] " + line);
        String output = System.getenv("PERFORMANCE_BENCHMARK_OUTPUT");
        Path path = Paths.get(output == null ? DEFAULT_OUTPUT_FILE : output);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, (line + "\n").getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Could not write benchmark results to " + path, e);
        }
    }

    // --------------------------------------------------------------------
    // Structure detection
    // --------------------------------------------------------------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkFormationWood5(GameTestHelper helper) {
        benchmarkFormation(helper, ChestMaterial.WOOD, 5, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkFormationWood9(GameTestHelper helper) {
        benchmarkFormation(helper, ChestMaterial.WOOD, 9, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkFormationWood9Interfaces(GameTestHelper helper) {
        benchmarkFormation(helper, ChestMaterial.WOOD, 9, true);
    }

    /**
     * Measure how long it takes to build up a chest of the given dimension, block by block.
     * This is dominated by the multiblock detection that runs on every block placement.
     */
    protected void benchmarkFormation(GameTestHelper helper, ChestMaterial material, int dimension, boolean interfaces) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        Set<BlockPos> interfacePositions = interfaces ? surfacePositions(POS, dimension) : Sets.newHashSet();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            createChest(helper, dimension, material, interfacePositions);
            clearChest(helper, dimension);
        }

        long total = 0;
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            createChest(helper, dimension, material, interfacePositions);
            total += System.nanoTime() - start;
            clearChest(helper, dimension);
        }

        report("formation_" + material.getName() + (interfaces ? "_interfaces" : ""),
                dimension, millis(total) / MEASURE_ITERATIONS);
        helper.succeed();
    }

    /**
     * Measure how long it takes to break and replace a single wall block of a formed chest.
     * This invalidates and revalidates the whole structure, including its inventory.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkRevalidationWood9(GameTestHelper helper) {
        benchmarkRevalidation(helper, ChestMaterial.WOOD, 9);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkRevalidationNetherite9(GameTestHelper helper) {
        benchmarkRevalidation(helper, ChestMaterial.NETHERITE, 9);
    }

    protected void benchmarkRevalidation(GameTestHelper helper, ChestMaterial material, int dimension) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        createChest(helper, dimension, material, Sets.newHashSet());
        BlockPos wall = POS.offset(1, 0, 0);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            destroyBlock(helper, wall);
            helper.setBlock(wall, material.getBlockWall());
        }

        long total = 0;
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            destroyBlock(helper, wall);
            helper.setBlock(wall, material.getBlockWall());
            total += System.nanoTime() - start;
        }

        clearChest(helper, dimension);
        report("revalidation_" + material.getName(), dimension, millis(total) / MEASURE_ITERATIONS);
        helper.succeed();
    }

    // --------------------------------------------------------------------
    // Inventory
    // --------------------------------------------------------------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkInventoryConstructionWood9(GameTestHelper helper) {
        benchmarkInventoryConstruction(helper, ChestMaterial.WOOD, 9);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkInventoryConstructionNetherite9(GameTestHelper helper) {
        benchmarkInventoryConstruction(helper, ChestMaterial.NETHERITE, 9);
    }

    /**
     * Measure how long it takes to (re)construct the backing inventory of a chest,
     * as happens on structure (in)validation and on chunk load.
     */
    protected void benchmarkInventoryConstruction(GameTestHelper helper, ChestMaterial material, int dimension) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        BlockEntityColossalChest chest = createChest(helper, dimension, material, Sets.newHashSet());
        int size = chest.getInventory().getContainerSize();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            chest.setSize(chest.getSize());
        }

        long total = 0;
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            chest.setSize(chest.getSize());
            total += System.nanoTime() - start;
        }
        report("inventory_construction_" + material.getName(), size, millis(total) / MEASURE_ITERATIONS);

        reportBytes("inventory_memory_" + material.getName(), size, measureInventoryBytesPerSlot(chest, size));

        clearChest(helper, dimension);
        helper.succeed();
    }

    protected long measureInventoryBytesPerSlot(BlockEntityColossalChest chest, int size) {
        Runtime runtime = Runtime.getRuntime();
        chest.setInventory(null);
        long before = usedMemory(runtime);
        chest.setSize(chest.getSize());
        long after = usedMemory(runtime);
        return Math.max(0, (after - before) / size);
    }

    protected long usedMemory(Runtime runtime) {
        System.gc();
        System.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // --------------------------------------------------------------------
    // Container
    // --------------------------------------------------------------------

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkContainerWood9(GameTestHelper helper) {
        benchmarkContainer(helper, ChestMaterial.WOOD, 9);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkContainerNetherite9(GameTestHelper helper) {
        benchmarkContainer(helper, ChestMaterial.NETHERITE, 9);
    }

    /**
     * Measure the container operations that happen while a player has a chest opened:
     * opening it, synchronizing changes on every tick, and shift-clicking an item into it.
     */
    protected void benchmarkContainer(GameTestHelper helper, ChestMaterial material, int dimension) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        BlockEntityColossalChest chest = createChest(helper, dimension, material, Sets.newHashSet());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        int size = chest.getInventory().getContainerSize();

        // Container opening
        long total = 0;
        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            ContainerColossalChest container = new ContainerColossalChest(i, player.getInventory(), chest.getInventory());
            long elapsed = System.nanoTime() - start;
            container.removed(player);
            if (i >= WARMUP_ITERATIONS) {
                total += elapsed;
            }
        }
        report("container_open_" + material.getName(), size, millis(total) / MEASURE_ITERATIONS);

        // Change synchronization, as performed on every tick in which the inventory changed
        ContainerColossalChest container = new ContainerColossalChest(0, player.getInventory(), chest.getInventory());
        container.broadcastChanges();
        total = 0;
        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            chest.getInventory().setItem(0, new ItemStack(Items.APPLE, i + 1));
            long start = System.nanoTime();
            container.broadcastChanges();
            long elapsed = System.nanoTime() - start;
            if (i >= WARMUP_ITERATIONS) {
                total += elapsed;
            }
        }
        report("container_broadcast_" + material.getName(), size, millis(total) / MEASURE_ITERATIONS);
        chest.getInventory().setItem(0, ItemStack.EMPTY);

        // Shift-clicking an item into a full chest, which has to scan for a target slot
        for (int slot = 0; slot < size; slot++) {
            chest.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        int playerSlotIndex = playerSlotIndex(container, size);
        total = 0;
        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            container.getSlot(playerSlotIndex).set(new ItemStack(Items.APPLE));
            long start = System.nanoTime();
            container.quickMoveStack(player, playerSlotIndex);
            long elapsed = System.nanoTime() - start;
            if (i >= WARMUP_ITERATIONS) {
                total += elapsed;
            }
        }
        report("container_quickmove_" + material.getName(), size, millis(total) / MEASURE_ITERATIONS);

        container.removed(player);
        chest.getInventory().clearContent();
        clearChest(helper, dimension);
        helper.succeed();
    }

    protected int playerSlotIndex(ContainerColossalChest container, int chestSize) {
        for (int i = chestSize; i < container.slots.size(); i++) {
            Slot slot = container.getSlot(i);
            if (slot.getItem().isEmpty()) {
                return i;
            }
        }
        throw new IllegalStateException("No empty player inventory slot available");
    }

    /**
     * Measure the payload that is sent to a player when a chest is opened, for both an empty and a full chest.
     *
     * This mirrors the encoding that {@link ContainerColossalChest#updateCraftingInventory} performs,
     * without actually sending anything, as a game test has no real client connection.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkContainerSyncWood9(GameTestHelper helper) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        BlockEntityColossalChest chest = createChest(helper, 9, ChestMaterial.WOOD, Sets.newHashSet());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        HolderLookup.Provider provider = helper.getLevel().registryAccess();
        int size = chest.getInventory().getContainerSize();
        ContainerColossalChest container = new ContainerColossalChest(0, player.getInventory(), chest.getInventory());

        measureSync(container, provider, "container_sync_empty_wood", size);

        for (int slot = 0; slot < size; slot++) {
            chest.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        measureSync(container, provider, "container_sync_full_wood", size);

        container.removed(player);
        chest.getInventory().clearContent();
        clearChest(helper, 9);
        helper.succeed();
    }

    protected void measureSync(ContainerColossalChest container, HolderLookup.Provider provider, String preset, int size) {
        long total = 0;
        long bytes = 0;
        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            bytes = encodeContents(container, provider);
            long elapsed = System.nanoTime() - start;
            if (i >= WARMUP_ITERATIONS) {
                total += elapsed;
            }
        }
        report(preset, size, millis(total) / MEASURE_ITERATIONS);
        reportBytes(preset, size, bytes / size);
    }

    protected long encodeContents(ContainerColossalChest container, HolderLookup.Provider provider) {
        CompoundTag buffer = new CompoundTag();
        ListTag list = new ListTag();
        buffer.put("stacks", list);
        int i = 0;
        for (ItemStack itemStack : container.getItems()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("slot", i++);
            tag.put("stack", ItemStack.OPTIONAL_CODEC
                    .encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), itemStack)
                    .getOrThrow());
            list.add(tag);
        }
        CountingOutputStream counter = new CountingOutputStream();
        try {
            NbtIo.write(buffer, new DataOutputStream(counter));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return counter.getCount();
    }

    /**
     * Measure the container operations for the largest chest that can be built with the default configuration:
     * a 20x20x20 netherite chest. Its structure does not fit inside a game test template,
     * so the inventory is created directly.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkContainerMaxSize(GameTestHelper helper) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        int size = chestSize(ColossalChestConfig.maxSize, ChestMaterial.NETHERITE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        long start = System.nanoTime();
        LargeInventoryCommon inventory = new LargeInventoryCommon(size, 64);
        ContainerColossalChest container = new ContainerColossalChest(0, player.getInventory(), inventory);
        report("container_open_maxsize", size, millis(System.nanoTime() - start));

        inventory.setItem(0, new ItemStack(Items.APPLE));
        container.broadcastChanges();
        inventory.setItem(0, new ItemStack(Items.APPLE, 2));
        start = System.nanoTime();
        container.broadcastChanges();
        report("container_broadcast_maxsize", size, millis(System.nanoTime() - start));

        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        int playerSlotIndex = playerSlotIndex(container, size);
        container.getSlot(playerSlotIndex).set(new ItemStack(Items.APPLE));
        start = System.nanoTime();
        container.quickMoveStack(player, playerSlotIndex);
        report("container_quickmove_maxsize", size, millis(System.nanoTime() - start));

        container.removed(player);
        helper.succeed();
    }

    /**
     * An output stream that only counts the bytes written to it.
     */
    protected static class CountingOutputStream extends OutputStream {
        private long count = 0;

        @Override
        public void write(int b) {
            this.count++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            this.count += len;
        }

        public long getCount() {
            return count;
        }
    }

    /**
     * Measure the client-side cost of receiving all chest contents when a chest is opened.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void benchmarkClientInventoryWood9(GameTestHelper helper) {
        if (!isEnabled()) {
            helper.succeed();
            return;
        }

        int size = chestSize(9, ChestMaterial.WOOD);
        long total = 0;
        for (int i = 0; i < WARMUP_ITERATIONS + MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            LargeInventoryCommon inventory = new LargeInventoryCommon(size, 64);
            for (int slot = 0; slot < size; slot++) {
                inventory.setItem(slot, new ItemStack(Items.STONE, 64));
            }
            long elapsed = System.nanoTime() - start;
            if (i >= WARMUP_ITERATIONS) {
                total += elapsed;
            }
        }
        report("client_inventory_fill_wood", size, millis(total) / MEASURE_ITERATIONS);
        helper.succeed();
    }

    // --------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------

    protected static double millis(long nanos) {
        return nanos / 1_000_000D;
    }

    protected int chestSize(int dimension, ChestMaterial material) {
        return (int) Math.ceil((Math.pow(dimension, 3) * 27) * material.getInventoryMultiplier() / 9) * 9;
    }

    protected Set<BlockPos> surfacePositions(BlockPos pos, int dimension) {
        Set<BlockPos> positions = Sets.newHashSet();
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    if ((x == 0 && y == 0 && z == 0)) {
                        continue; // Core
                    }
                    if (x == 0 || y == 0 || z == 0 || x == dimension - 1 || y == dimension - 1 || z == dimension - 1) {
                        positions.add(pos.offset(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    protected BlockEntityColossalChest createChest(GameTestHelper helper, int dimension, ChestMaterial material, Set<BlockPos> interfaces) {
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    BlockPos poso = POS.offset(x, y, z);
                    if (x == 0 && y == 0 && z == 0) {
                        helper.setBlock(poso, material.getBlockCore());
                    } else if (x == 0 || y == 0 || z == 0 || x == dimension - 1 || y == dimension - 1 || z == dimension - 1) {
                        helper.setBlock(poso, interfaces.contains(poso) ? material.getBlockInterface() : material.getBlockWall());
                    }
                }
            }
        }
        return helper.getBlockEntity(POS);
    }

    protected void destroyBlock(GameTestHelper helper, BlockPos pos) {
        BlockState blockState = helper.getBlockState(pos);
        if (helper.getLevel().removeBlock(helper.absolutePos(pos), false)) {
            blockState.getBlock().destroy(helper.getLevel(), helper.absolutePos(pos), blockState);
        }
    }

    protected void clearChest(GameTestHelper helper, int dimension) {
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    helper.setBlock(POS.offset(x, y, z), Blocks.AIR);
                }
            }
        }
    }

}
