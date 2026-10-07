package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
 * @param tier The position among materials, lowest first. Built-in materials use multiples of 10.
 * @param defaultProperties The tunable values when no data file defines them.
 * @author rubensworks
 */
public record ChestMaterial(ResourceLocation id, SoundType soundType, float hardness, boolean needsPickaxe, float defaultBlastResistance,
                            int tier, MaterialProperties defaultProperties) {

    public static final ChestMaterial WOOD = new ChestMaterial(id("wood"), SoundType.WOOD, 2.5F, false, 2.5F,
            10, new MaterialProperties(1, 3, false));
    public static final ChestMaterial COPPER = new ChestMaterial(id("copper"), SoundType.COPPER, 3.0F, true, 6.0F,
            20, new MaterialProperties(2, 4, false));
    public static final ChestMaterial IRON = new ChestMaterial(id("iron"), SoundType.METAL, 5.0F, true, 6.0F,
            30, new MaterialProperties(3, 5, false));
    public static final ChestMaterial GOLD = new ChestMaterial(id("gold"), SoundType.METAL, 3.0F, true, 6.0F,
            40, new MaterialProperties(4, 6, false));
    public static final ChestMaterial DIAMOND = new ChestMaterial(id("diamond"), SoundType.METAL, 5.0F, true, 6.0F,
            50, new MaterialProperties(5, 7, false));
    public static final ChestMaterial OBSIDIAN = new ChestMaterial(id("obsidian"), SoundType.STONE, 10.0F, true, 1200.0F,
            60, new MaterialProperties(6, 8, true));
    public static final ChestMaterial NETHERITE = new ChestMaterial(id("netherite"), SoundType.NETHERITE_BLOCK, 10.0F, true, 1200.0F,
            70, new MaterialProperties(7, 10, true));

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
        List<ChestMaterial> materials = Lists.newArrayList(all);
        materials.add(material);
        materials.sort(Comparator.comparingInt(ChestMaterial::tier).thenComparing(m -> m.id().toString()));
        all = ImmutableList.copyOf(materials);
    }

    /**
     * @return All registered materials, ordered by tier.
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
