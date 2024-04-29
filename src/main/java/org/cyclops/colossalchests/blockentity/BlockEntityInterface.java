package org.cyclops.colossalchests.blockentity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

import java.lang.ref.WeakReference;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class BlockEntityInterface extends CyclopsBlockEntity {

    @NBTPersist
    @Getter
    private Vec3i corePosition = null;
    private WeakReference<BlockEntityColossalChest> coreReference = new WeakReference<BlockEntityColossalChest>(null);

    public BlockEntityInterface(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_INTERFACE.get(), blockPos, blockState);
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
                    BlockEntityHelpers.get(getLevel(), new BlockPos(corePosition), BlockEntityColossalChest.class).orElse(null));
        }
        return coreReference.get();
    }

}
