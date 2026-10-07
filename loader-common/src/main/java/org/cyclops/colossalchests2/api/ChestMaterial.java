package org.cyclops.colossalchests2.api;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

/**
 * A chest material, identified by its id. Everything tunable about it, including its order, comes from its data file
 * data/[namespace]/colossalchests2/material/[name].json, see {@link MaterialProperties}.
 * A material exists when a core block is registered for it, see {@link IColossalChestsApi#createCore}.
 * @param id The material id, matching its data file.
 * @author rubensworks
 */
public record ChestMaterial(ResourceLocation id) {

    // Ids of this mod's materials, for code that refers to them. Materials are not registered here.
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ColossalChestsApi.MOD_ID, path);
    }

    /**
     * @return All materials that have a core block, in the order of their data files.
     */
    public static List<ChestMaterial> getAll() {
        return ColossalChestsApi.get().getMaterials();
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
        return ColossalChestsApi.get().getMaterialProperties(id);
    }

    /**
     * @return The plain wall of this material, or air if there is none.
     */
    public Block getWallBlock() {
        return ColossalChestsApi.get().getWallBlock(this);
    }

    /**
     * @return The core of this material, or air if there is none.
     */
    public Block getCoreBlock() {
        return ColossalChestsApi.get().getCoreBlock(this);
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
