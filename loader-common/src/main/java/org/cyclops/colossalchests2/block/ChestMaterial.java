package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A chest material, identified by its id. Everything tunable about it, including its order, comes from its data file
 * data/[namespace]/colossalchests2/material/[name].json, see {@link MaterialProperties}.
 * A material exists when a {@link BlockChestCore} is registered for it. Addons register a wall and a core block
 * for their material, for example with {@link BlockChestWallConfig} and {@link BlockChestCoreConfig}.
 * @param id The material id, matching its data file.
 * @author rubensworks
 */
public record ChestMaterial(ResourceLocation id) {

    public static final ChestMaterial WOOD = new ChestMaterial(id("wood"));
    public static final ChestMaterial COPPER = new ChestMaterial(id("copper"));
    public static final ChestMaterial IRON = new ChestMaterial(id("iron"));
    public static final ChestMaterial GOLD = new ChestMaterial(id("gold"));
    public static final ChestMaterial DIAMOND = new ChestMaterial(id("diamond"));
    public static final ChestMaterial OBSIDIAN = new ChestMaterial(id("obsidian"));
    public static final ChestMaterial NETHERITE = new ChestMaterial(id("netherite"));

    /**
     * Accepts any id, so data can refer to materials of mods that are not installed.
     */
    public static final Codec<ChestMaterial> CODEC = ResourceLocation.CODEC.xmap(ChestMaterial::new, ChestMaterial::id);

    /**
     * The materials of this mod, which it registers blocks for.
     */
    public static final List<ChestMaterial> BUILT_IN = ImmutableList.of(WOOD, COPPER, IRON, GOLD, DIAMOND, OBSIDIAN, NETHERITE);

    private static List<ChestMaterial> ordered = List.of();
    @Nullable
    private static ChestTables orderedFor = null;
    private static int orderedCores = -1;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

    /**
     * @return All materials that have a core block, in the order of their data files.
     */
    public static synchronized List<ChestMaterial> getAll() {
        ChestTables tables = ChestTablesLoader.get();
        List<BlockChestCore> cores = BlockChestCore.getInstances();
        if (tables != orderedFor || cores.size() != orderedCores) {
            List<ChestMaterial> materials = cores.stream().map(BlockChestCore::getMaterial).distinct().toList();
            ordered = order(materials, material -> tables.getMaterial(material.id()).after().orElse(null));
            orderedFor = tables;
            orderedCores = cores.size();
        }
        return ordered;
    }

    /**
     * @param materials Materials.
     * @param after Gives the material that each material comes right after, or null.
     * @return The materials, each right after the one it comes after, and before the next material of this mod.
     *         Materials after the same one are ordered by id. Materials after none, after one that does not exist,
     *         or in a loop come last, by id.
     */
    static List<ChestMaterial> order(List<ChestMaterial> materials, Function<ChestMaterial, ResourceLocation> after) {
        Set<ResourceLocation> ids = materials.stream().map(ChestMaterial::id).collect(Collectors.toSet());
        Map<ResourceLocation, List<ChestMaterial>> children = Maps.newHashMap();
        List<ChestMaterial> roots = Lists.newArrayList();
        for (ChestMaterial material : materials) {
            ResourceLocation anchor = after.apply(material);
            if (anchor != null && ids.contains(anchor)) {
                children.computeIfAbsent(anchor, id -> Lists.newArrayList()).add(material);
            } else {
                roots.add(material);
            }
        }
        // Other mods' materials go before this mod's next material, so "after copper" means before iron.
        children.values().forEach(list -> list.sort(Comparator.<ChestMaterial, Boolean>comparing(ChestMaterial::isOwn)
                .thenComparing(material -> material.id().toString())));
        // This mod's first material comes first.
        roots.sort(Comparator.<ChestMaterial, Boolean>comparing(material -> !material.isOwn())
                .thenComparing(material -> material.id().toString()));
        List<ChestMaterial> result = Lists.newArrayList();
        Deque<ChestMaterial> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            ChestMaterial material = stack.pop();
            result.add(material);
            children.getOrDefault(material.id(), List.of()).reversed().forEach(stack::push);
        }
        // Materials in a loop of afters are not reached from any root.
        materials.stream().filter(material -> !result.contains(material))
                .sorted(Comparator.comparing(material -> material.id().toString())).forEach(result::add);
        return ImmutableList.copyOf(result);
    }

    private boolean isOwn() {
        return id.getNamespace().equals(Reference.MOD_ID);
    }

    /**
     * @return The name used in block ids, such as "wood".
     */
    public String getName() {
        return id.getPath();
    }

    /**
     * @return The currently loaded tunable values of this material.
     */
    public MaterialProperties getProperties() {
        return ChestTablesLoader.get().getMaterial(id);
    }

    /**
     * @return The plain wall of this material, or air if there is none.
     */
    public Block getWallBlock() {
        return BlockChestWall.getInstances().stream().filter(wall -> equals(wall.getMaterial())).findFirst().map(Block.class::cast).orElse(Blocks.AIR);
    }

    /**
     * @return The core of this material, or air if there is none.
     */
    public Block getCoreBlock() {
        return BlockChestCore.getInstances().stream().filter(core -> equals(core.getMaterial())).findFirst().map(Block.class::cast).orElse(Blocks.AIR);
    }

    /**
     * @return A tooltip line with this material's limits.
     */
    public Component getLimitsTooltip() {
        MaterialProperties properties = getProperties();
        return Component.translatable("material.colossalchests2.limits", properties.maxSize(), properties.upgradeSlots())
                .withStyle(ChatFormatting.GRAY);
    }

    /**
     * @return The name, from the key material.[namespace].[name].
     */
    public Component getDisplayName() {
        return Component.translatable(id.toLanguageKey("material"));
    }

    /**
     * @return The material with this id, if it has a core block.
     */
    public static Optional<ChestMaterial> byId(ResourceLocation id) {
        return getAll().stream().filter(material -> material.id().equals(id)).findFirst();
    }

}
