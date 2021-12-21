package org.cyclops.colossalchests.tileentity;

import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.ITickingTile;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.TickingTileComponent;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class TileInterface extends CyclopsTileEntity {

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    @Getter
    private Vector3i corePosition = null;
    private WeakReference<TileColossalChest> coreReference = new WeakReference<TileColossalChest>(null);

    public TileInterface() {
        super(RegistryEntries.TILE_ENTITY_INTERFACE);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        TileColossalChest core = getCore();
        if (core != null) {
            LazyOptional<T> t = core.getCapability(capability, facing);
            if (t.isPresent()) {
                return t;
            }
        }
        return super.getCapability(capability, facing);
    }

    public void setCorePosition(Vector3i corePosition) {
        this.corePosition = corePosition;
        coreReference = new WeakReference<TileColossalChest>(null);
    }

    protected TileColossalChest getCore() {
        if(corePosition == null) {
            return null;
        }
        if (coreReference.get() == null) {
            coreReference = new WeakReference<TileColossalChest>(
                    TileHelpers.getSafeTile(getLevel(), new BlockPos(corePosition), TileColossalChest.class).orElse(null));
        }
        return coreReference.get();
    }

}
