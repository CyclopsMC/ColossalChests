package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.storage.ChestStorage;

import java.util.Optional;

/**
 * Magnet walls pull dropped items around them into their chest, like a hopper would insert them.
 * @author rubensworks
 */
public final class MagnetWall {

    /**
     * Items this close to the chest are inserted, wherever they touch it.
     */
    private static final double REACH = 0.25;
    private static final double PULL_SPEED = 0.25;
    // Cancels the gravity items apply each tick, so they float towards walls above them.
    private static final double LIFT = 0.04;

    private MagnetWall() {
    }

    public static void tick(Level level, BlockPos pos, BlockEntityChestWall wall) {
        if (!GeneralConfig.magnetEnabled) {
            return;
        }
        Optional<BlockEntityChestCore> core = wall.getCore();
        if (core.isEmpty() || core.get().getStructure() == null) {
            return;
        }
        ChestStorage storage = core.get().getStorage();
        ChestStructure structure = core.get().getStructure();
        int radius = wall.getMagnetRadius();
        Vec3 center = Vec3.atCenterOf(pos);
        // Pulled items can land on any face of the chest, not only on the wall.
        AABB reach = AABB.encapsulatingFullBlocks(structure.min(), structure.max()).inflate(REACH);
        // Items within the radius of the wall's block, in blocks along each axis, like a beacon's range.
        // Items a player just dropped have a pickup delay, so a player can still throw items near the chest.
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(radius),
                item -> item.isAlive() && !item.hasPickUpDelay())) {
            ItemStack stack = item.getItem();
            if (item.getBoundingBox().intersects(reach)) {
                long inserted = storage.insertAutomated(stack, stack.getCount(), false);
                if (inserted > 0) {
                    ItemStack rest = stack.copyWithCount(stack.getCount() - (int) inserted);
                    if (rest.isEmpty()) {
                        item.discard();
                    } else {
                        item.setItem(rest);
                    }
                }
            } else if (storage.insertAutomated(stack, 1, true) > 0) {
                // Only items the chest takes are pulled, so the rest does not pile up against the wall.
                Vec3 pull = center.subtract(item.position()).normalize().scale(PULL_SPEED);
                item.setDeltaMovement(pull.add(0, LIFT, 0));
                item.hasImpulse = true;
            }
        }
    }
}
