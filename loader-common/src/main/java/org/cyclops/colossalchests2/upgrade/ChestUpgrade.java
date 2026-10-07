package org.cyclops.colossalchests2.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.UpgradeProperties;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A core upgrade type. Its limits and strength come from its data file, see {@link UpgradeProperties}.
 * Addons register their own upgrades with {@link ChestUpgrades#register(ChestUpgrade)} while their mod is constructed,
 * and an {@link ItemChestUpgrade} named [namespace]:upgrade_[name] for it, for example with {@link ItemChestUpgradeConfig}.
 * @author rubensworks
 */
public class ChestUpgrade {

    private final ResourceLocation id;
    private final UpgradeProperties defaultProperties;

    /**
     * @param id The upgrade id, matching its data file.
     * @param defaultProperties The limits and strength when no data file defines them.
     */
    public ChestUpgrade(ResourceLocation id, UpgradeProperties defaultProperties) {
        this.id = id;
        this.defaultProperties = defaultProperties;
    }

    public UpgradeProperties getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * @return The name, from the key of its item, item.[namespace].upgrade_[name].
     */
    public Component getDisplayName() {
        return Component.translatable(id.withPrefix("upgrade_").toLanguageKey("item"));
    }

    public ResourceLocation getId() {
        return id;
    }

    public UpgradeProperties getProperties() {
        return ChestTablesLoader.get().getUpgrade(id);
    }

    /**
     * @param material A material id.
     * @return How many of this upgrade a chest of that material takes.
     */
    public int getMaxCount(ResourceLocation material) {
        return ChestTablesLoader.get().getMaxUpgradeCount(id, material);
    }

    /**
     * Apply the capacity modifiers of this upgrade.
     * @param builder The profile being built.
     * @param count The installed count, at least 1.
     */
    public void applyProfile(CapacityProfile.Builder builder, int count) {
    }

    /**
     * @param count The installed count.
     * @return The slots this upgrade adds.
     */
    public int getExtraSlots(int count) {
        return 0;
    }

    /**
     * @param storage The chest storage.
     * @return Slots that keep this upgrade from being removed, besides slots that would not fit the new capacity.
     */
    public List<Integer> getRemovalProblems(ChestStorage storage) {
        return List.of();
    }

    /**
     * Limit what goes into the chest, from players and automation alike.
     * @param core The chest.
     * @param type The item type, its count is ignored.
     * @param count The installed count, at least 1.
     * @return If the chest takes this item type.
     */
    public boolean canInsert(BlockEntityChestCore core, ItemStack type, int count) {
        return true;
    }

    /**
     * Called each server tick while the chest is formed.
     * @param core The chest.
     * @param count The installed count, at least 1.
     */
    public void tick(BlockEntityChestCore core, int count) {
    }

    /**
     * @return A short line with the strength of the upgrade for its item tooltip, if any.
     */
    @Nullable
    public Component getEffect() {
        return null;
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
