package org.cyclops.colossalchests2.material;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.config.MaterialCost;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeRules;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;

import java.util.List;
import java.util.Map;

/**
 * What changing the material of a formed chest costs, and when it is refused.
 * @author rubensworks
 */
public final class MaterialChangeRules {

    private MaterialChangeRules() {
    }

    /**
     * Why a chest can not change to a material.
     */
    public enum Problem {
        NONE,
        /**
         * The structure is larger than the material allows.
         */
        TOO_LARGE,
        /**
         * More upgrades are installed than the material has slots.
         */
        UPGRADE_SLOTS,
        /**
         * More of an upgrade is installed than the material allows.
         */
        UPGRADE_LIMIT
    }

    /**
     * Contents always fit after a change, as capacity only depends on size and upgrades, which this keeps unchanged.
     * @param size The structure size.
     * @param upgrades The installed upgrades.
     * @param target The material to change to.
     * @return Why the change is refused, or {@link Problem#NONE}.
     */
    public static Problem check(int size, UpgradeSet upgrades, ChestMaterial target) {
        if (size > target.getProperties().maxSize()) {
            return Problem.TOO_LARGE;
        }
        int installed = upgrades.counts().values().stream().mapToInt(Integer::intValue).sum();
        if (installed > target.getProperties().upgradeSlots()) {
            return Problem.UPGRADE_SLOTS;
        }
        for (Map.Entry<ChestUpgrade, Integer> entry : upgrades.counts().entrySet()) {
            if (entry.getValue() > ChestUpgradeRules.getMaxCount(entry.getKey(), target.id())) {
                return Problem.UPGRADE_LIMIT;
            }
        }
        return Problem.NONE;
    }

    /**
     * @param size The structure size.
     * @return The number of blocks on the shell of a chest of that size.
     */
    public static int getShellBlocks(int size) {
        int inner = Math.max(0, size - 2);
        return size * size * size - inner * inner * inner;
    }

    /**
     * @param perBlock The cost for one block.
     * @param blocks The number of blocks.
     * @return The total cost, with entries of the same item merged.
     */
    public static List<MaterialCost> getTotalCost(List<MaterialCost> perBlock, int blocks) {
        Map<ResourceLocation, Integer> totals = Maps.newLinkedHashMap();
        for (MaterialCost cost : perBlock) {
            totals.merge(cost.item(), cost.count() * blocks, Integer::sum);
        }
        return totals.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new MaterialCost(entry.getKey(), entry.getValue()))
                .toList();
    }

}
