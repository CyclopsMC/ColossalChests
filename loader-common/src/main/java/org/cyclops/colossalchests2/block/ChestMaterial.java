package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.Reference;
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
import java.util.stream.Collectors;

/**
 * A chest material, with a wall and core block each.
 * Tunable values come from the material data files, see {@link MaterialProperties}.
 * Addons register their own materials with {@link #register(ChestMaterial)} while their mod is constructed,
 * and register blocks named [namespace]:chest_wall_[name] and [namespace]:chest_core_[name] for it,
 * for example with {@link BlockChestWallConfig} and {@link BlockChestCoreConfig}.
 * @param id The material id, matching its data file.
 * @param soundType The block sound.
 * @param hardness The block hardness.
 * @param needsPickaxe If a pickaxe is the correct tool, otherwise an axe.
 * @param defaultBlastResistance The explosion resistance the blocks are registered with.
 * @param after The material this one comes right after, such as copper, or null to come last.
 *              Materials after the same one are ordered by id, so by mod id first.
 * @param defaultProperties The tunable values when no data file defines them.
 * @author rubensworks
 */
public record ChestMaterial(ResourceLocation id, SoundType soundType, float hardness, boolean needsPickaxe, float defaultBlastResistance,
                            @Nullable ResourceLocation after, MaterialProperties defaultProperties) {

    public static final ChestMaterial WOOD = new ChestMaterial(id("wood"), SoundType.WOOD, 2.5F, false, 2.5F,
            null, new MaterialProperties(1, 3, false));
    public static final ChestMaterial COPPER = new ChestMaterial(id("copper"), SoundType.COPPER, 3.0F, true, 6.0F,
            id("wood"), new MaterialProperties(2, 4, false));
    public static final ChestMaterial IRON = new ChestMaterial(id("iron"), SoundType.METAL, 5.0F, true, 6.0F,
            id("copper"), new MaterialProperties(3, 5, false));
    public static final ChestMaterial GOLD = new ChestMaterial(id("gold"), SoundType.METAL, 3.0F, true, 6.0F,
            id("iron"), new MaterialProperties(4, 6, false));
    public static final ChestMaterial DIAMOND = new ChestMaterial(id("diamond"), SoundType.METAL, 5.0F, true, 6.0F,
            id("gold"), new MaterialProperties(5, 7, false));
    public static final ChestMaterial OBSIDIAN = new ChestMaterial(id("obsidian"), SoundType.STONE, 10.0F, true, 1200.0F,
            id("diamond"), new MaterialProperties(6, 8, true));
    public static final ChestMaterial NETHERITE = new ChestMaterial(id("netherite"), SoundType.NETHERITE_BLOCK, 10.0F, true, 1200.0F,
            id("obsidian"), new MaterialProperties(7, 10, true));

    public static final Codec<ChestMaterial> CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown chest material: " + id)),
            ChestMaterial::id);

    /**
     * The materials of this mod, which it registers blocks for.
     */
    public static final List<ChestMaterial> BUILT_IN = ImmutableList.of(WOOD, COPPER, IRON, GOLD, DIAMOND, OBSIDIAN, NETHERITE);

    private static volatile List<ChestMaterial> all = BUILT_IN;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

    /**
     * Register a material, which must happen while mods are constructed, before any world is loaded.
     * @param material The material.
     */
    public static synchronized void register(ChestMaterial material) {
        if (byId(material.id()).isPresent()) {
            throw new IllegalArgumentException("Chest material " + material.id() + " is already registered");
        }
        all = order(ImmutableList.<ChestMaterial>builder().addAll(all).add(material).build());
    }

    /**
     * @param materials Materials, this mod's first.
     * @return The materials, each right after the one it comes after. Materials after one that is not registered come last.
     */
    static List<ChestMaterial> order(List<ChestMaterial> materials) {
        Set<ResourceLocation> ids = materials.stream().map(ChestMaterial::id).collect(Collectors.toSet());
        Map<ResourceLocation, List<ChestMaterial>> children = Maps.newHashMap();
        List<ChestMaterial> roots = Lists.newArrayList();
        for (ChestMaterial material : materials) {
            if (material.after() != null && ids.contains(material.after())) {
                children.computeIfAbsent(material.after(), id -> Lists.newArrayList()).add(material);
            } else {
                roots.add(material);
            }
        }
        // Added materials go before this mod's next material, so "after copper" means before iron.
        Comparator<ChestMaterial> order = Comparator.<ChestMaterial, Boolean>comparing(BUILT_IN::contains)
                .thenComparing(material -> material.id().toString());
        children.values().forEach(list -> list.sort(order));
        // This mod's first material comes first, other materials without a registered one to follow come last.
        roots.sort(Comparator.<ChestMaterial, Boolean>comparing(material -> !BUILT_IN.contains(material))
                .thenComparing(material -> material.id().toString()));
        List<ChestMaterial> ordered = Lists.newArrayList();
        Deque<ChestMaterial> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            ChestMaterial material = stack.pop();
            ordered.add(material);
            children.getOrDefault(material.id(), List.of()).reversed().forEach(stack::push);
        }
        // Materials in a loop of afters are not reached from any root.
        materials.stream().filter(material -> !ordered.contains(material))
                .sorted(Comparator.comparing(material -> material.id().toString())).forEach(ordered::add);
        return ImmutableList.copyOf(ordered);
    }

    /**
     * @return All registered materials, in order.
     */
    public static List<ChestMaterial> getAll() {
        return all;
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

    public Block getWallBlock() {
        return BuiltInRegistries.BLOCK.get(id.withPrefix("chest_wall_"));
    }

    public Block getCoreBlock() {
        return BuiltInRegistries.BLOCK.get(id.withPrefix("chest_core_"));
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

    public static Optional<ChestMaterial> byId(ResourceLocation id) {
        return all.stream().filter(material -> material.id().equals(id)).findFirst();
    }

}
