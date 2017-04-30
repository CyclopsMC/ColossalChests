package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import gnu.trove.map.TIntObjectMap;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.Capabilities;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.*;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.multi.*;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.cyclopscore.inventory.*;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntityBase;

import java.util.Collections;
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
    private Vec3d renderOffset = new Vec3d(0, 0, 0);
    @NBTPersist
    private String customName = null;
    @NBTPersist
    private int materialId = 0;
    @NBTPersist
    private int _modVersion = 0; // For backwards compatibility
    @NBTPersist(useDefaultValue = false)
    private List<Vec3i> interfaceLocations = Lists.newArrayList();
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
    private boolean recreateNullInventory = true;

    private Block block = ColossalChest.getInstance();
    private EnumFacingMap<int[]> facingSlots = EnumFacingMap.newMap();

    public TileColossalChest() {
        if (Capabilities.SLOTLESS_ITEMHANDLER != null) {
            addSlotlessItemHandlerCapability();
        }
    }

    protected void addSlotlessItemHandlerCapability() {
        IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        addCapabilityInternal(Capabilities.SLOTLESS_ITEMHANDLER,
                new IndexedSlotlessItemHandlerWrapper(itemHandler, new IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference() {
                    @Override
                    public int getInventoryStackLimit() {
                        return getInventory().getInventoryStackLimit();
                    }

                    @Override
                    public Map<Item, TIntObjectMap<ItemStack>> getIndex() {
                        return ((IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) getInventory()).getIndex();
                    }

                    @Override
                    public int getFirstEmptySlot() {
                        return ((IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) getInventory()).getFirstEmptySlot();
                    }

                    @Override
                    public int getLastEmptySlot() {
                        return ((IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) getInventory()).getLastEmptySlot();
                    }

                    @Override
                    public int getFirstNonEmptySlot() {
                        return ((IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) getInventory()).getFirstNonEmptySlot();
                    }

                    @Override
                    public int getLastNonEmptySlot() {
                        return ((IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) getInventory()).getLastNonEmptySlot();
                    }
                }));
    }

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
                    ItemStack contents = this.lastValidInventory.getStackInSlot(slot);
                    if (!contents.isEmpty()) {
                        this.inventory.setInventorySlotContents(slot, contents);
                        this.lastValidInventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                    slot++;
                }
                if(slot < this.lastValidInventory.getSizeInventory()) {
                    MinecraftHelpers.dropItems(getWorld(), this.lastValidInventory, getPos());
                }
                this.lastValidInventory = null;
            }
        } else {
            interfaceLocations.clear();
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

    protected IndexedInventory constructInventory() {
        if (GeneralConfig.creativeChests) {
            return constructInventoryDebug();
        }
        return new IndexedInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
    }

    protected IndexedInventory constructInventoryDebug() {
        IndexedInventory inv = new IndexedInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, new ItemStack(Item.REGISTRY.getRandomObject(world.rand)));
        }
        return inv;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // Don't send the inventory to the client.
        // The client will receive the data once the gui is opened.
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;
        this.inventory = null;
        this.lastValidInventory = null;
        this.recreateNullInventory = false;
        NBTTagCompound tag = super.getUpdateTag();
        this.inventory = oldInventory;
        this.lastValidInventory = oldLastInventory;
        this.recreateNullInventory = true;
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;
        if (getWorld() != null && getWorld().isRemote) {
            // Don't read the inventory on the client.
            // The client will receive the data once the gui is opened.
            this.inventory = null;
            this.lastValidInventory = null;
            this.recreateNullInventory = false;
        }
        super.readFromNBT(tag);
        if (getWorld() != null && getWorld().isRemote) {
            this.inventory = oldInventory;
            this.lastValidInventory = oldLastInventory;
            this.recreateNullInventory = true;
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, getUpdateTag());
    }

    protected int calculateInventorySize() {
        return (int) Math.ceil((Math.pow(getSizeSingular(), 3) * 27) * getMaterial().getInventoryMultiplier() / 9) * 9;
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        // Backwards-compatibility check
        if(world != null) {
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
        if(world != null
                && !this.world.isRemote
                && this.playersUsing != 0
                && WorldHelpers.efficientTick(world, TICK_MODULUS, getPos().hashCode())) {
            this.playersUsing = 0;
            float range = 5.0F;
            @SuppressWarnings("unchecked")
            List<EntityPlayer> entities = this.world.getEntitiesWithinAABB(
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

            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }

        prevLidAngle = lidAngle;
        float increaseAngle = 0.15F / Math.min(5, getSizeSingular());
        if (playersUsing > 0 && lidAngle == 0.0F) {
            world.playSound(
                    (double) getPos().getX() + 0.5D,
                    (double) getPos().getY() + 0.5D,
                    (double) getPos().getZ() + 0.5D,
                    SoundEvents.BLOCK_CHEST_OPEN,
                    SoundCategory.BLOCKS,
                    (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                    world.rand.nextFloat() * 0.1F + 0.45F + increaseAngle,
                    true
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
                world.playSound(
                        (double) getPos().getX() + 0.5D,
                        (double) getPos().getY() + 0.5D,
                        (double) getPos().getZ() + 0.5D,
                        SoundEvents.BLOCK_CHEST_CLOSE,
                        SoundCategory.BLOCKS,
                        (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                        world.rand.nextFloat() * 0.05F + 0.45F + increaseAngle,
                        true
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
        if (world != null) {
            playersUsing += change;
            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }
    }

    @Override
    public INBTInventory getInventory() {
        if(lastValidInventory != null) {
            return lastValidInventory;
        }
        if(inventory == null && this.recreateNullInventory) {
            inventory = constructInventory();
        }
        return inventory;
    }

    @Override
    protected boolean canAccess(int slot, EnumFacing side) {
        return getSizeSingular() > 1 && super.canAccess(slot, side);
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

    public void setCenter(Vec3d center) {
        EnumFacing rotation;
        double dx = Math.abs(center.xCoord - getPos().getX());
        double dz = Math.abs(center.zCoord - getPos().getZ());
        boolean equal = (center.xCoord - getPos().getX()) == (center.zCoord - getPos().getZ());
        if(dx > dz || (!equal && getSizeSingular() == 2)) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.xCoord - getPos().getX()));
        } else {
            rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(center.zCoord - getPos().getZ()));
        }
        this.setRotation(rotation);
        this.renderOffset = new Vec3d(getPos().getX() - center.xCoord, getPos().getY() - center.yCoord, getPos().getZ() - center.zCoord);
    }

    public Vec3d getRenderOffset() {
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
        if (side == null) {
            side = EnumFacing.UP;
        }
        int[] slots = facingSlots.get(side);
        if(slots == null) {
            ContiguousSet<Integer> integers = ContiguousSet.create(
                    Range.closed(0, getSizeInventory()), DiscreteDomain.integers()
            );
            slots = ArrayUtils.toPrimitive(integers.toArray(new Integer[integers.size()]));
            facingSlots.put(side, slots);
        }
        return slots;
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

    public void addInterface(Vec3i blockPos) {
        interfaceLocations.add(blockPos);
    }

    public List<Vec3i> getInterfaceLocations() {
        return Collections.unmodifiableList(interfaceLocations);
    }
}
