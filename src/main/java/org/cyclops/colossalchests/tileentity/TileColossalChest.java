package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.multi.*;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntityBase;

import java.util.List;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class TileColossalChest extends InventoryTileEntityBase implements CyclopsTileEntity.ITickingTile {

    private static final int TICK_MODULUS = 200;
    /**
     * The multiblock structure detector for this furnace.
     */
    @SuppressWarnings("unchecked")
    public static CubeDetector detector = new HollowCubeDetector(
            new AllowedBlock[]{
                    new AllowedBlock(ChestWall.getInstance()),
                    new AllowedBlock(ColossalChest.getInstance()).addCountValidator(new ExactBlockCountValidator(1))
            },
            Lists.newArrayList(ColossalChest.getInstance(), ChestWall.getInstance())
    ).addSizeValidator(new MinimumSizeValidator(new Vec3i(1, 1, 1))).addSizeValidator(new CubeSizeValidator());

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private INBTInventory inventory = null;

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
    @NBTPersist
    private Vec3 renderOffset = new Vec3(0, 0, 0);
    /**
     * The previous angle of the lid.
     */
    public float prevLidAngle;
    /**
     * The current angle of the lid.
     */
    public float lidAngle;
    private int playersUsing;

    private Block block = ColossalChest.getInstance();

    /**
     * @return the size
     */
    public Vec3i getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Vec3i size) {
        this.size = size;
        if(isStructureComplete()) {
            this.inventory = constructInventory();
        } else {
            if(this.inventory != null) {
                MinecraftHelpers.dropItems(getWorld(), this.inventory, getPos());
            }
            this.inventory = new LargeInventory(0, "invalid", 0);
        }
        sendUpdate();
    }

    public int getSizeSingular() {
        return getSize().getX() + 1;
    }

    protected INBTInventory constructInventory() {
        return new LargeInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
    }

    protected int calculateInventorySize() {
        return (int) (Math.pow(getSizeSingular(), 3) * 27);
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();
        // Resynchronize clients with the server state, the last condition makes sure
        // not all chests are synced at the same time.
        if(worldObj != null
                && !this.worldObj.isRemote
                && this.playersUsing != 0
                && WorldHelpers.efficientTick(worldObj, TICK_MODULUS, getPos().hashCode())) {
            this.playersUsing = 0;
            float range = 5.0F;
            @SuppressWarnings("unchecked")
            List<EntityPlayer> entities = this.worldObj.getEntitiesWithinAABB(
                    EntityPlayer.class,
                    new AxisAlignedBB(
                            getPos().subtract(new Vec3i(range, range, range)),
                            getPos().add(new Vec3i(1 + range, 1 + range, 1 + range))
                    )
            );

            for(EntityPlayer player : entities) {
                if (player.openContainer instanceof ContainerColossalChest) {
                    ++this.playersUsing;
                }
            }

            worldObj.addBlockEvent(getPos(), block, 1, playersUsing);
        }

        prevLidAngle = lidAngle;
        float increaseAngle = 0.15F / Math.min(5, getSizeSingular());
        if (playersUsing > 0 && lidAngle == 0.0F) {
            worldObj.playSoundEffect(
                    (double) getPos().getX() + 0.5D,
                    (double) getPos().getY() + 0.5D,
                    (double) getPos().getZ() + 0.5D,
                    "random.chestopen",
                    (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                    worldObj.rand.nextFloat() * 0.1F + 0.45F + increaseAngle
            );
        }
        if (playersUsing == 0 && lidAngle > 0.0F || playersUsing > 0 && lidAngle < 1.0F) {
            float preIncreaseAngle = lidAngle;
            if (playersUsing > 0) {
                lidAngle += increaseAngle;
            } else {
                lidAngle -= increaseAngle;
            }
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            float closedAngle = 0.5F;
            if (lidAngle < closedAngle && preIncreaseAngle >= closedAngle) {
                worldObj.playSoundEffect(
                        (double) getPos().getX() + 0.5D,
                        (double) getPos().getY() + 0.5D,
                        (double) getPos().getZ() + 0.5D,
                        "random.chestclosed",
                        (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                        worldObj.rand.nextFloat() * 0.05F + 0.45F + increaseAngle
                );
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int i, int j) {
        if (i == 1) {
            playersUsing = j;
        }
        return true;
    }

    @Override
    public void openInventory(EntityPlayer entityPlayer) {
        super.openInventory(entityPlayer);
        triggerPlayerUsageChange(1);
    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer) {
        super.closeInventory(entityPlayer);
        triggerPlayerUsageChange(-1);
    }

    private void triggerPlayerUsageChange(int change) {
        if (worldObj != null) {
            playersUsing += change;
            worldObj.addBlockEvent(getPos(), block, 1, playersUsing);
        }
    }

    @Override
    public INBTInventory getInventory() {
        if(inventory == null) {
            inventory = constructInventory();
        }
        return inventory;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return super.isUseableByPlayer(entityPlayer)
                && (worldObj == null || worldObj.getTileEntity(getPos()) != this);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return getSizeSingular() > 1 && super.canInteractWith(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        int size = getSizeSingular();
        return new AxisAlignedBB(getPos().subtract(new Vec3i(size, size, size)), getPos().add(size, size * 2, size));
    }

    public void setCenter(Vec3 center) {
        EnumFacing rotation = EnumFacing.NORTH;
        if(center.xCoord + 0.5 - getPos().getX() >= getSizeSingular() / 2) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.xCoord - getPos().getX()));
        } else if(center.zCoord + 0.5 - getPos().getZ() >= getSizeSingular() / 2) {
            rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(center.zCoord - getPos().getZ()));
        }
        this.setRotation(rotation);
        this.renderOffset = new Vec3(getPos().getX() - center.xCoord, getPos().getY() - center.yCoord, getPos().getZ() - center.zCoord);
    }

    public Vec3 getRenderOffset() {
        return this.renderOffset;
    }

    /**
     * Callback for when a structure has been detected for a spirit furnace block.
     * @param world The world.
     * @param location The location of one block of the structure.
     * @param size The size of the structure.
     * @param valid If the structure is being validated(/created), otherwise invalidated.
     * @param originCorner The origin corner
     */
    public static void detectStructure(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {

    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return ArrayUtils.toPrimitive((Integer[]) ContiguousSet.create(Range.closed(0, getSizeInventory()),
                DiscreteDomain.integers()).toArray());
    }

    /**
     * @return If the structure is valid.
     */
    public boolean isStructureComplete() {
        return !getSize().equals(Vec3i.NULL_VECTOR);
    }
}
