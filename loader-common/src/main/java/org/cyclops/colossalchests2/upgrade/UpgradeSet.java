package org.cyclops.colossalchests2.upgrade;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.storage.CapacityProfile;

import java.util.Map;

/**
 * The upgrades installed in a core, by count.
 * @param counts Installed count by upgrade, without zero counts.
 * @author rubensworks
 */
public record UpgradeSet(Map<ChestUpgrade, Integer> counts) {

    public static final UpgradeSet EMPTY = new UpgradeSet(Map.of());

    public UpgradeSet {
        counts = ImmutableMap.copyOf(Maps.filterValues(counts, count -> count > 0));
    }

    /**
     * @param stacks The stacks in the upgrade slots.
     * @return The upgrades in them, ignoring other items.
     */
    public static UpgradeSet of(Iterable<ItemStack> stacks) {
        Map<ChestUpgrade, Integer> counts = Maps.newLinkedHashMap();
        for (ItemStack stack : stacks) {
            ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
            if (upgrade != null) {
                counts.merge(upgrade, stack.getCount(), Integer::sum);
            }
        }
        return new UpgradeSet(counts);
    }

    public int count(ChestUpgrade upgrade) {
        return counts.getOrDefault(upgrade, 0);
    }

    public boolean has(ChestUpgrade upgrade) {
        return count(upgrade) > 0;
    }

    /**
     * @param upgrade An upgrade.
     * @param delta The change in count.
     * @return A copy with the count changed.
     */
    public UpgradeSet with(ChestUpgrade upgrade, int delta) {
        Map<ChestUpgrade, Integer> newCounts = Maps.newLinkedHashMap(counts);
        newCounts.merge(upgrade, delta, Integer::sum);
        return new UpgradeSet(newCounts);
    }

    /**
     * Apply the capacity modifiers of all upgrades.
     * @param builder The profile being built from the structure.
     * @return The same builder.
     */
    public CapacityProfile.Builder applyProfile(CapacityProfile.Builder builder) {
        counts.forEach((upgrade, count) -> upgrade.applyProfile(builder, count));
        return builder;
    }

    /**
     * @param baseSlots Slots without upgrades.
     * @param maxSlots The maximum slot count.
     * @return The slot count with these upgrades.
     */
    public int getSlotCount(int baseSlots, int maxSlots) {
        long slots = baseSlots;
        for (Map.Entry<ChestUpgrade, Integer> entry : counts.entrySet()) {
            slots += entry.getKey().getExtraSlots(entry.getValue());
        }
        return (int) Math.min(maxSlots, slots);
    }

    /**
     * @param upgrade An upgrade.
     * @param material The chest material id.
     * @param baseSlots Slots without upgrades.
     * @param maxSlots The maximum slot count.
     * @return How many of the upgrade the chest takes: the material limit, and for slot upgrades only as many
     * as add slots below the maximum.
     */
    public static int getMaxCount(ChestUpgrade upgrade, ResourceLocation material, int baseSlots, int maxSlots) {
        int max = upgrade.getMaxCount(material);
        if (upgrade.getExtraSlots(1) > 0) {
            int count = 0;
            while (count < max && baseSlots + upgrade.getExtraSlots(count + 1) <= maxSlots) {
                count++;
            }
            max = count;
        }
        return max;
    }

}
