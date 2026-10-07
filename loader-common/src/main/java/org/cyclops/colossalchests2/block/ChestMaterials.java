package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The registered materials, from the core blocks, in the order of their data files.
 * @author rubensworks
 */
public final class ChestMaterials {

    private static List<ChestMaterial> ordered = List.of();
    @Nullable
    private static ChestTables orderedFor = null;
    private static int orderedCores = -1;

    private ChestMaterials() {
    }

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
    public static List<ChestMaterial> order(List<ChestMaterial> materials, Function<ChestMaterial, ResourceLocation> after) {
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
        children.values().forEach(list -> list.sort(Comparator.<ChestMaterial, Boolean>comparing(ChestMaterials::isOwn)
                .thenComparing(material -> material.id().toString())));
        // This mod's first material comes first.
        roots.sort(Comparator.<ChestMaterial, Boolean>comparing(material -> !isOwn(material))
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

    private static boolean isOwn(ChestMaterial material) {
        return material.id().getNamespace().equals(Reference.MOD_ID);
    }

}
