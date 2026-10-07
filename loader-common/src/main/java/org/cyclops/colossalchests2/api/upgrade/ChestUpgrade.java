package org.cyclops.colossalchests2.api.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.api.ColossalChestsApi;
import org.cyclops.colossalchests2.api.IChest;
import org.cyclops.colossalchests2.api.IChestContents;
import org.cyclops.colossalchests2.api.UpgradeProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A core upgrade type. Its limits and strength come from its data file, see {@link UpgradeProperties}.
 * Addons register their own upgrades with {@link org.cyclops.colossalchests2.api.IColossalChestsApi#registerUpgrade}
 * while their mod is constructed, and an item named [namespace]:upgrade_[name] for it, from
 * {@link org.cyclops.colossalchests2.api.IColossalChestsApi#createUpgradeItem}.
 * @author rubensworks
 */
public class ChestUpgrade {

    private final ResourceLocation id;

    /**
     * @param id The upgrade id, matching its data file data/[namespace]/colossalchests2/upgrade/[name].json.
     *           Without a data file, the upgrade is disabled.
     */
    public ChestUpgrade(ResourceLocation id) {
        this.id = id;
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
        return ColossalChestsApi.get().getUpgradeProperties(id);
    }

    /**
     * @param material A material id.
     * @return How many of this upgrade a chest of that material takes.
     */
    public int getMaxCount(ResourceLocation material) {
        return ColossalChestsApi.get().getMaxUpgradeCount(id, material);
    }

    /**
     * Apply the capacity modifiers of this upgrade.
     * @param modifiers The capacity being built.
     * @param count The installed count, at least 1.
     */
    public void applyProfile(ICapacityModifiers modifiers, int count) {
    }

    /**
     * @param count The installed count.
     * @return The slots this upgrade adds.
     */
    public int getExtraSlots(int count) {
        return 0;
    }

    /**
     * @param contents The chest contents.
     * @return Slots that keep this upgrade from being removed, besides slots that would not fit the new capacity.
     */
    public List<Integer> getRemovalProblems(IChestContents contents) {
        return List.of();
    }

    /**
     * Limit what goes into the chest, from players and automation alike.
     * @param chest The chest.
     * @param type The item type, its count is ignored.
     * @param count The installed count, at least 1.
     * @return If the chest takes this item type.
     */
    public boolean canInsert(IChest chest, ItemStack type, int count) {
        return true;
    }

    /**
     * Called each server tick while the chest is formed.
     * @param chest The chest.
     * @param count The installed count, at least 1.
     */
    public void tick(IChest chest, int count) {
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
