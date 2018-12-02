package org.cyclops.colossalchests.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class TileInterface extends CyclopsTileEntity implements ISidedInventory {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    @Getter
    private Vec3i corePosition = null;
    private WeakReference<TileColossalChest> coreReference = new WeakReference<TileColossalChest>(null);

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        TileColossalChest core = getCore();
        if (core != null && core.hasCapability(capability, facing)) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        TileColossalChest core = getCore();
        if (core != null) {
            T t = core.getCapability(capability, facing);
            if (t != null) {
                return t;
            }
        }
        return super.getCapability(capability, facing);
    }

    public void setCorePosition(Vec3i corePosition) {
        this.corePosition = corePosition;
        coreReference = new WeakReference<TileColossalChest>(null);
    }

    protected TileColossalChest getCore() {
        if(corePosition == null) {
            return null;
        }
        if (coreReference.get() == null) {
            coreReference = new WeakReference<TileColossalChest>(
                    TileHelpers.getSafeTile(getWorld(), new BlockPos(corePosition), TileColossalChest.class));
        }
        return coreReference.get();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return new int[0];
        }
        return core.getSlotsForFace(side);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return false;
        }
        return core.canInsertItem(index, itemStackIn, direction);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return false;
        }
        return core.canExtractItem(index, stack, direction);
    }

    @Override
    public int getSizeInventory() {
        ISidedInventory core = getCore();
        if(core == null) {
            return 0;
        }
        return core.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        ISidedInventory core = getCore();
        if(core == null) {
            return true;
        }
        return core.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return ItemStack.EMPTY;
        }
        return core.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return ItemStack.EMPTY;
        }
        return core.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return ItemStack.EMPTY;
        }
        return core.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ISidedInventory core =  getCore();
        if(core != null) {
            core.setInventorySlotContents(index, stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        ISidedInventory core =  getCore();
        if(core == null) {
            return 0;
        }
        return core.getInventoryStackLimit();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        ISidedInventory core = getCore();
        if(core == null) {
            return false;
        }
        return core.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        ISidedInventory core =  getCore();
        if(core != null) {
            core.openInventory(player);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        ISidedInventory core =  getCore();
        if(core != null) {
            core.closeInventory(player);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return false;
        }
        return core.isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id) {
        ISidedInventory core =  getCore();
        if(core == null) {
            return -1;
        }
        return core.getField(id);
    }

    @Override
    public void setField(int id, int value) {
        ISidedInventory core =  getCore();
        if(core != null) {
            core.setField(id, value);
        }
    }

    @Override
    public int getFieldCount() {
        ISidedInventory core =  getCore();
        if(core == null) {
            return 0;
        }
        return core.getFieldCount();
    }

    @Override
    public void clear() {
        ISidedInventory core =  getCore();
        if(core != null) {
            core.clear();
        }
    }

    @Override
    public String getName() {
        ISidedInventory core = getCore();
        if(core == null) {
            return null;
        }
        return core.getName();
    }

    @Override
    public boolean hasCustomName() {
        ISidedInventory core =  getCore();
        if(core == null) {
            return false;
        }
        return core.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        ISidedInventory core =  getCore();
        if(core == null) {
            return null;
        }
        return core.getDisplayName();
    }

}
