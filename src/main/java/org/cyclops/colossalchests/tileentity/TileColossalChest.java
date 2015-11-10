package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.AllowedBlock;
import org.cyclops.cyclopscore.block.CubeDetector;
import org.cyclops.cyclopscore.block.HollowCubeDetector;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
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
                    new AllowedBlock(ColossalChest.getInstance()).setExactOccurences(1)
            },
            Lists.newArrayList(ColossalChest.getInstance(), ChestWall.getInstance())
    ).setMinimumSize(new Vec3i(1, 1, 1));

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private INBTInventory inventory = null;

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
    @NBTPersist
    private Vec3i renderOffset = new Vec3i(0, 0, 0);
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
            this.inventory = new SimpleInventory(0, "invalid", 0); // TODO ?
        }
        sendUpdate();
    }

    protected INBTInventory constructInventory() {
        return new SimpleInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
    }

    protected int calculateInventorySize() {
        // TODO: better size algorithm
        return getSize().getX() * 10;
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
        float increaseAngle = 0.05F;
        if (playersUsing > 0 && lidAngle == 0.0F) {
            worldObj.playSoundEffect(
                    (double) getPos().getX() + 0.5D,
                    (double) getPos().getY() + 0.5D,
                    (double) getPos().getZ() + 0.5D,
                    "random.chestopen",
                    0.5F,
                    worldObj.rand.nextFloat() * 0.1F + 0.5F
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
                        0.5F,
                        worldObj.rand.nextFloat() * 0.1F + 0.5F
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().subtract(new Vec3i(3, 3, 3)), getPos().add(3, 6, 3));
    }

    public void setCenter(BlockPos center) {
        EnumFacing rotation = EnumFacing.NORTH;
        if(center.getX() != getPos().getX()) {
            rotation = DirectionHelpers.getEnumFacingFromXSign(center.getX() - getPos().getX());
        } else if(center.getZ() != getPos().getZ()) {
            rotation = DirectionHelpers.getEnumFacingFromZSing(center.getZ() - getPos().getZ());
        }
        this.setRotation(rotation);
        this.renderOffset = getPos().subtract(center);
    }

    public Vec3i getRenderOffset() {
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
