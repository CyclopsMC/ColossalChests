package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.*;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.*;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.multi.*;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntityBase;

import java.util.List;
import java.util.Map;

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
                    new AllowedBlock(ColossalChest.getInstance()).addCountValidator(new ExactBlockCountValidator(1)),
                    new AllowedBlock(Interface.getInstance())
            },
            Lists.newArrayList(ColossalChest.getInstance(), ChestWall.getInstance(), Interface.getInstance())
    )
            .addSizeValidator(new MinimumSizeValidator(new Vec3i(1, 1, 1)))
            .addSizeValidator(new CubeSizeValidator())
            .addSizeValidator(new MaximumSizeValidator(getMaxSize()) {
                @Override
                public Vec3i getMaximumSize() {
                    return getMaxSize();
                }
            });

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private SimpleInventory lastValidInventory = null;
    private SimpleInventory inventory = null; // No need to @NBTPersists, this is done because of its getter

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
    @NBTPersist
    private Vec3 renderOffset = new Vec3(0, 0, 0);
    @NBTPersist
    private String customName = null;
    @NBTPersist
    private int materialId = 0;
    @NBTPersist
    private int _modVersion = 0; // For backwards compatibility
    private static final int _MOD_VERSION = 1;

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
    private Map<Integer, int[]> facingSlots = Maps.newHashMap();

    /**
     * @return the size
     */
    public Vec3i getSize() {
        return size;
    }

    /**
     * Set the size.
     * This will also handle the change in inventory size.
     * @param size the size to set
     */
    public void setSize(Vec3i size) {
        this.size = size;
        facingSlots.clear();
        if(isStructureComplete()) {
            this._modVersion = _MOD_VERSION;
            this.inventory = constructInventory();

            // Move all items from the last valid inventory into the new one
            // If the new inventory would be smaller than the old one, the remaining
            // items will be ejected into the world for slot index larger than the new size.
            if(this.lastValidInventory != null) {
                int slot = 0;
                while(slot < Math.min(this.lastValidInventory.getSizeInventory(), this.inventory.getSizeInventory())) {
                    this.inventory.setInventorySlotContents(slot, this.lastValidInventory.getStackInSlot(slot));
                    this.lastValidInventory.setInventorySlotContents(slot, null);
                    slot++;
                }
                if(slot < this.lastValidInventory.getSizeInventory()) {
                    MinecraftHelpers.dropItems(getWorld(), this.lastValidInventory, getPos());
                }
                this.lastValidInventory = null;
            }
        } else {
            if(this.inventory != null) {
                if(GeneralConfig.ejectItemsOnDestroy) {
                    MinecraftHelpers.dropItems(getWorld(), this.inventory, getPos());
                    this.lastValidInventory = null;
                } else {
                    this.lastValidInventory = this.inventory;
                }
            }
            this.inventory = new LargeInventory(0, "invalid", 0);
        }
        sendUpdate();
    }

    public void setMaterial(PropertyMaterial.Type material) {
        this.materialId = material.ordinal();
    }

    public PropertyMaterial.Type getMaterial() {
        return PropertyMaterial.Type.values()[this.materialId];
    }

    public int getSizeSingular() {
        return getSize().getX() + 1;
    }

    protected LargeInventory constructInventory() {
        return new LargeInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
    }

    protected int calculateInventorySize() {
        return (int) Math.ceil((Math.pow(getSizeSingular(), 3) * 27) * getMaterial().getInventoryMultiplier());
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        // Backwards-compatibility check
        if(worldObj != null) {
            if(this._modVersion != _MOD_VERSION && this.isStructureComplete()) {
                ColossalChests.clog("Upgrading colossal chest from old mod version at " + getPos());
                // In the old version, we only had wooden versions, so correctly set their properties.
                TileColossalChest.detector.detect(getWorld(), getPos(), null, new CubeDetector.IValidationAction() {
                    @Override
                    public L10NHelpers.UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
                        getWorld().setBlockState(location, blockState.
                                withProperty(ColossalChest.ACTIVE, true).
                                withProperty(ColossalChest.MATERIAL, PropertyMaterial.Type.WOOD));
                        return null;
                    }
                }, false);
                this._modVersion = _MOD_VERSION;
            }
        }

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
                            getPos().add(new Vec3i(-range, -range, -range)),
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
        EnumFacing rotation;
        double dx = Math.abs(center.xCoord - getPos().getX());
        double dz = Math.abs(center.zCoord - getPos().getZ());
        if(dx >= dz) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.xCoord - getPos().getX()));
        } else {
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
        if(!facingSlots.containsKey(side.ordinal())) {
            ContiguousSet<Integer> integers = ContiguousSet.create(
                    Range.closed(0, getSizeInventory()), DiscreteDomain.integers()
            );
            facingSlots.put(side.ordinal(), ArrayUtils.toPrimitive(integers.toArray(new Integer[integers.size()])));
        }
        return facingSlots.get(side.ordinal());
    }

    /**
     * @return If the structure is valid.
     */
    public boolean isStructureComplete() {
        return !getSize().equals(Vec3i.NULL_VECTOR);
    }

    public static Vec3i getMaxSize() {
        int size = ColossalChestConfig.maxSize;
        return new Vec3i(size, size, size);
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && customName.length() > 0;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : L10NHelpers.localize("general.colossalchests.colossalchest.name",
                getMaterial().getLocalizedName(), getSizeSingular());
    }
}
