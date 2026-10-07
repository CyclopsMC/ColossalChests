package org.cyclops.colossalchests2.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.MaterialProperties;

import java.util.List;
import java.util.Optional;

/**
 * A chest material, with a wall and core block each.
 * Tunable values come from the material data files, see {@link MaterialProperties}.
 * @param id The material id, matching its data file.
 * @param soundType The block sound.
 * @param hardness The block hardness.
 * @param needsPickaxe If a pickaxe is the correct tool, otherwise an axe.
 * @param defaultBlastResistance The explosion resistance the blocks are registered with.
 * @author rubensworks
 */
public record ChestMaterial(ResourceLocation id, SoundType soundType, float hardness, boolean needsPickaxe, float defaultBlastResistance) {

    public static final ChestMaterial WOOD = new ChestMaterial(id("wood"), SoundType.WOOD, 2.5F, false, 2.5F);
    public static final ChestMaterial COPPER = new ChestMaterial(id("copper"), SoundType.COPPER, 3.0F, true, 6.0F);
    public static final ChestMaterial IRON = new ChestMaterial(id("iron"), SoundType.METAL, 5.0F, true, 6.0F);
    public static final ChestMaterial GOLD = new ChestMaterial(id("gold"), SoundType.METAL, 3.0F, true, 6.0F);
    public static final ChestMaterial DIAMOND = new ChestMaterial(id("diamond"), SoundType.METAL, 5.0F, true, 6.0F);
    public static final ChestMaterial OBSIDIAN = new ChestMaterial(id("obsidian"), SoundType.STONE, 10.0F, true, 1200.0F);
    public static final ChestMaterial NETHERITE = new ChestMaterial(id("netherite"), SoundType.NETHERITE_BLOCK, 10.0F, true, 1200.0F);

    public static final List<ChestMaterial> VALUES = ImmutableList.of(WOOD, COPPER, IRON, GOLD, DIAMOND, OBSIDIAN, NETHERITE);

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
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

    public Component getDisplayName() {
        return Component.translatable("material.colossalchests2." + getName());
    }

    public static Optional<ChestMaterial> byId(ResourceLocation id) {
        return VALUES.stream().filter(material -> material.id().equals(id)).findFirst();
    }

}
