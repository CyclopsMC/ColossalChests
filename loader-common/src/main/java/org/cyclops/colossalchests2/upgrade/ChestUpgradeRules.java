package org.cyclops.colossalchests2.upgrade;

import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.ResizeResult;

import java.util.List;
import java.util.Set;

/**
 * What a chest becomes with a set of upgrades, and which upgrade changes are allowed.
 * @author rubensworks
 */
public final class ChestUpgradeRules {

    private ChestUpgradeRules() {
    }

    /**
     * @param size The structure size, below the minimum for a chest that never formed.
     * @param upgrades The installed upgrades.
     * @return The capacity of a chest of that size with those upgrades.
     */
    public static CapacityProfile createProfile(int size, UpgradeSet upgrades) {
        long depth = size < GeneralConfig.MIN_SIZE ? 0 : GeneralConfig.getDepthForSize(Math.min(size, GeneralConfig.HARD_MAX_SIZE));
        return upgrades.applyProfile(CapacityProfile.builder(depth))
                .maxItemsPerSlot(GeneralConfig.getMaxItemsPerSlot())
                .acceptNonStackables(GeneralConfig.acceptNonStackables)
                .build();
    }

    /**
     * @param upgrades The installed upgrades.
     * @return The slot count with those upgrades.
     */
    public static int getSlotCount(UpgradeSet upgrades) {
        return upgrades.getSlotCount(GeneralConfig.getBaseSlots(), GeneralConfig.getMaxSlots());
    }

    /**
     * @param upgrade An upgrade.
     * @param material The chest material id.
     * @return How many of the upgrade a chest of that material takes.
     */
    public static int getMaxCount(ChestUpgrade upgrade, ResourceLocation material) {
        return UpgradeSet.getMaxCount(upgrade, material, GeneralConfig.getBaseSlots(), GeneralConfig.getMaxSlots());
    }

    /**
     * @param upgrades The installed upgrades.
     * @param upgrade An upgrade to add.
     * @param material The chest material id.
     * @return If the chest takes one more of the upgrade.
     */
    public static boolean canAdd(UpgradeSet upgrades, ChestUpgrade upgrade, ResourceLocation material) {
        return upgrades.count(upgrade) < getMaxCount(upgrade, material);
    }

    /**
     * Removing an upgrade is refused if contents would not fit: a filled slot over its new capacity, or a filled
     * slot beyond the new slot count. Slots that are already over capacity only count if the capacity changes.
     * @param storage The chest storage.
     * @param size The structure size.
     * @param upgrades The installed upgrades.
     * @param upgrade The upgrade to remove.
     * @return The slots that keep the upgrade from being removed, empty if it can be removed.
     */
    public static ResizeResult getRemovalProblems(ChestStorage storage, int size, UpgradeSet upgrades, ChestUpgrade upgrade) {
        if (!upgrades.has(upgrade)) {
            return ResizeResult.OK;
        }
        UpgradeSet remaining = upgrades.with(upgrade, -1);
        Set<Integer> offending = Sets.newTreeSet();
        CapacityProfile newProfile = createProfile(size, remaining);
        if (!newProfile.equals(storage.getProfile())) {
            offending.addAll(storage.validateProfile(newProfile).offendingSlots());
        }
        offending.addAll(storage.validateSlotCount(getSlotCount(remaining)).offendingSlots());
        return ResizeResult.of(List.copyOf(offending));
    }

}
