package org.cyclops.colossalchests2.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.jetbrains.annotations.Nullable;

/**
 * A chest, from {@link IColossalChestsApi#getChest}.
 * @author rubensworks
 */
public interface IChest extends IChestContents {

    @Nullable
    Level getLevel();

    BlockPos getCorePos();

    /**
     * @return The material, or null if the core block is missing.
     */
    @Nullable
    ChestMaterial getChestMaterial();

    /**
     * @return The outer edge length, or 0 if not formed.
     */
    int getChestSize();

    boolean isFormed();

    /**
     * Insert like automation does, so locks and voids apply.
     * @param type The item type, its count is ignored.
     * @return The amount that was (or would be) inserted.
     */
    long insert(ItemStack type, long amount, boolean simulate);

    /**
     * @param type The item type, its count is ignored.
     * @return The amount that was (or would be) extracted.
     */
    long extract(ItemStack type, long amount, boolean simulate);

    /**
     * @return How many of the upgrade are installed.
     */
    int getUpgradeCount(ChestUpgrade upgrade);

}
