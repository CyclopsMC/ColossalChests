package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

import java.lang.ref.WeakReference;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class BlockEntityInterface extends CyclopsBlockEntityCommon {

    @NBTPersist
    private Vec3i corePosition = null;
    private WeakReference<BlockEntityColossalChest> coreReference = new WeakReference<BlockEntityColossalChest>(null);

    public BlockEntityInterface(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_INTERFACE.value(), blockPos, blockState);
    }

    public Vec3i getCorePosition() {
        return corePosition;
    }

    public void setCorePosition(Vec3i corePosition) {
        this.corePosition = corePosition;
        coreReference = new WeakReference<BlockEntityColossalChest>(null);
    }

    public BlockEntityColossalChest getCore() {
        if(corePosition == null) {
            return null;
        }
        if (coreReference.get() == null) {
            coreReference = new WeakReference<>(
                    IModHelpers.get().getBlockEntityHelpers().get(getLevel(), new BlockPos(corePosition), BlockEntityColossalChest.class).orElse(null));
        }
        return coreReference.get();
    }

}
