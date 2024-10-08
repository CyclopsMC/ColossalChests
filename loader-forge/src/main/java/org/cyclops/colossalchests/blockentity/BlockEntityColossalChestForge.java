package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.cyclopscore.inventory.SimpleInventoryCommon;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestForge extends BlockEntityColossalChest {

    private LazyOptional<IItemHandler> capabilityItemHandler = LazyOptional.empty();

    public BlockEntityColossalChestForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public void setInventory(SimpleInventoryCommon inventory) {
        this.capabilityItemHandler.invalidate();
        super.setInventory(inventory);
        if (this.inventory.getContainerSize() > 0) {
            IItemHandler itemHandler = new InvWrapper(this.inventory);
            this.capabilityItemHandler = LazyOptional.of(() -> itemHandler);
        } else {
            this.capabilityItemHandler = LazyOptional.empty();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        ensureInventoryInitialized();
        if (this.capabilityItemHandler.isPresent() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return this.capabilityItemHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public AABB getRenderBoundingBox() {
        int size = this.getSizeSingular();
        return new AABB(
                Vec3.atLowerCornerOf(this.getBlockPos().subtract(new Vec3i(size, size, size))),
                Vec3.atLowerCornerOf(this.getBlockPos().offset(size, size * 2, size))
        );
    }
}
