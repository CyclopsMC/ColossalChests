package org.cyclops.colossalchests2.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.UpgradeProperties;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A core upgrade type. Its limits and strength come from its data file, see {@link UpgradeProperties}.
 * @author rubensworks
 */
public class ChestUpgrade {

    private final ResourceLocation id;

    public ChestUpgrade(ResourceLocation id) {
        this.id = id;
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
