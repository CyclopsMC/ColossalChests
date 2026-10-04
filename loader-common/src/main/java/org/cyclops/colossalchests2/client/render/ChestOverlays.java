package org.cyclops.colossalchests2.client.render;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Overlays that chest members draw on top of the giant chest, per block.
 * Only members that are not plain walls are considered.
 * @author rubensworks
 */
public final class ChestOverlays {

    private static final Map<Block, IChestOverlay> OVERLAYS = Maps.newIdentityHashMap();

    private ChestOverlays() {
    }

    public static void register(Block block, IChestOverlay overlay) {
        OVERLAYS.put(block, overlay);
    }

    @Nullable
    public static IChestOverlay get(Block block) {
        return OVERLAYS.get(block);
    }

}
