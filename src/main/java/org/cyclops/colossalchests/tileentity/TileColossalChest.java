package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.experimental.Delegate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.colossalchests.Capabilities;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.IndexedInventory;
import org.cyclops.cyclopscore.inventory.IndexedSlotlessItemHandlerWrapper;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class TileColossalChest extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile, INamedContainerProvider {

    private static final int TICK_MODULUS = 200;

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private SimpleInventory lastValidInventory = null;
    private SimpleInventory inventory = null;
    private LazyOptional<IItemHandler> capabilityItemHandler = LazyOptional.empty();
    private LazyOptional<ISlotlessItemHandler> capabilityItemHandlerSlotless = LazyOptional.empty();

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
    @NBTPersist
    private Vec3d renderOffset = new Vec3d(0, 0, 0);
    private ITextComponent customName = null;
    @NBTPersist
    private int materialId = 0;
    @NBTPersist
    private int rotation = 0;
    @NBTPersist(useDefaultValue = false)
    private List<Vec3i> interfaceLocations = Lists.newArrayList();

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

    private EnumFacingMap<int[]> facingSlots = EnumFacingMap.newMap();

    public TileColossalChest() {
        super(RegistryEntries.TILE_ENTITY_COLOSSAL_CHEST);
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
            setInventory(constructInventory());
            this.inventory.addDirtyMarkListener(this);

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
                    InventoryHelpers.dropItems(getWorld(), this.lastValidInventory, getPos());
                }
                this.lastValidInventory = null;
            }
        } else {
            interfaceLocations.clear();
            if(this.inventory != null) {
                if(GeneralConfig.ejectItemsOnDestroy) {
                    InventoryHelpers.dropItems(getWorld(), this.inventory, getPos());
                    this.lastValidInventory = null;
                } else {
                    this.lastValidInventory = this.inventory;
                }
            }
            setInventory(new LargeInventory(0, 0));
        }
        sendUpdate();
    }

    public void setMaterial(ChestMaterial material) {
        this.materialId = material.ordinal();
    }

    public ChestMaterial getMaterial() {
        return ChestMaterial.VALUES.get(this.materialId);
    }

    public SimpleInventory getLastValidInventory() {
        return lastValidInventory;
    }

    public void setLastValidInventory(SimpleInventory lastValidInventory) {
        this.lastValidInventory = lastValidInventory;
    }

    public int getSizeSingular() {
        return getSize().getX() + 1;
    }

    protected boolean isClientSide() {
        return getWorld() != null && getWorld().isRemote;
    }

    protected LargeInventory constructInventory() {
        if (!isClientSide() && GeneralConfig.creativeChests) {
            return constructInventoryDebug();
        }
        return !isClientSide() ? new IndexedInventory(calculateInventorySize(), 64) {
            @Override
            public void openInventory(PlayerEntity entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.openInventory(entityPlayer);
                    triggerPlayerUsageChange(1);
                }
            }

            @Override
            public void closeInventory(PlayerEntity entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.closeInventory(entityPlayer);
                    triggerPlayerUsageChange(-1);
                }
            }
        } : new LargeInventory(calculateInventorySize(), 64);
    }

    protected LargeInventory constructInventoryDebug() {
        LargeInventory inv = !isClientSide() ? new IndexedInventory(calculateInventorySize(), 64)
                : new LargeInventory(calculateInventorySize(), 64);
        Random random = new Random();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, new ItemStack(Iterables.get(ForgeRegistries.ITEMS.getValues(),
                    random.nextInt(ForgeRegistries.ITEMS.getValues().size()))));
        }
        return inv;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        // Don't send the inventory to the client.
        // The client will receive the data once the gui is opened.
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;
        this.inventory = null;
        this.lastValidInventory = null;
        this.recreateNullInventory = false;
        CompoundNBT tag = super.getUpdateTag();
        this.inventory = oldInventory;
        this.lastValidInventory = oldLastInventory;
        this.recreateNullInventory = true;
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;

        if (getWorld() != null && getWorld().isRemote) {
            // Don't read the inventory on the client.
            // The client will receive the data once the gui is opened.
            this.inventory = null;
            this.lastValidInventory = null;
            this.recreateNullInventory = false;
        }
        super.read(tag);
        if (getWorld() != null && getWorld().isRemote) {
            this.inventory = oldInventory;
            this.lastValidInventory = oldLastInventory;
            this.recreateNullInventory = true;
        } else {
            getInventory().read(tag.getCompound("inventory"));
            if (tag.contains("lastValidInventory", Constants.NBT.TAG_COMPOUND)) {
                this.lastValidInventory = new LargeInventory(tag.getInt("lastValidInventorySize"), this.inventory.getInventoryStackLimit());
                this.lastValidInventory.read(tag.getCompound("lastValidInventory"));
            }
        }

        if (tag.contains("CustomName", Constants.NBT.TAG_STRING)) {
            this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        if (this.customName != null) {
            tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        if (this.inventory != null) {
            CompoundNBT subTag = new CompoundNBT();
            this.inventory.write(subTag);
            tag.put("inventory", subTag);
        }
        if (this.lastValidInventory != null) {
            CompoundNBT subTag = new CompoundNBT();
            this.lastValidInventory.write(subTag);
            tag.put("lastValidInventory", subTag);
            tag.putInt("lastValidInventorySize", this.lastValidInventory.getSizeInventory());
        }
        return super.write(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, getUpdateTag());
    }

    protected int calculateInventorySize() {
        int size = getSizeSingular();
        if (size == 1) {
            return 0;
        }
        return (int) Math.ceil((Math.pow(size, 3) * 27) * getMaterial().getInventoryMultiplier() / 9) * 9;
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        // Resynchronize clients with the server state, the last condition makes sure
        // not all chests are synced at the same time.
        if(world != null
                && !this.world.isRemote
                && this.playersUsing != 0
                && WorldHelpers.efficientTick(world, TICK_MODULUS, getPos().hashCode())) {
            this.playersUsing = 0;
            float range = 5.0F;
            @SuppressWarnings("unchecked")
            List<PlayerEntity> entities = this.world.getEntitiesWithinAABB(
                    PlayerEntity.class,
                    new AxisAlignedBB(
                            getPos().add(new Vec3i(-range, -range, -range)),
                            getPos().add(new Vec3i(1 + range, 1 + range, 1 + range))
                    )
            );

            for(PlayerEntity player : entities) {
                if (player.openContainer instanceof ContainerColossalChest) {
                    ++this.playersUsing;
                }
            }

            world.addBlockEvent(getPos(), getBlockState().getBlock(), 1, playersUsing);
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

    private void triggerPlayerUsageChange(int change) {
        if (world != null) {
            playersUsing += change;
            world.addBlockEvent(getPos(), getBlockState().getBlock(), 1, playersUsing);
        }
    }

    public void setInventory(SimpleInventory inventory) {
        this.capabilityItemHandler.invalidate();
        this.capabilityItemHandlerSlotless.invalidate();
        this.inventory = inventory;
        if (this.inventory.getSizeInventory() > 0) {
            IItemHandler itemHandler = new InvWrapper(this.inventory);
            this.capabilityItemHandler = LazyOptional.of(() -> itemHandler);
            if (Capabilities.SLOTLESS_ITEMHANDLER != null) {
                this.capabilityItemHandlerSlotless = LazyOptional.of(() ->
                        new IndexedSlotlessItemHandlerWrapper(itemHandler, (IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) this.inventory));
            }
        } else {
            this.capabilityItemHandler = LazyOptional.empty();
            this.capabilityItemHandlerSlotless = LazyOptional.empty();
        }
    }

    protected void ensureInventoryInitialized() {
        if (getWorld() != null && getWorld().isRemote && (inventory == null || inventory.getSizeInventory() != calculateInventorySize())) {
            setInventory(constructInventory());
        }
    }

    public INBTInventory getInventory() {
        if(lastValidInventory != null) {
            return new IndexedInventory();
        }
        ensureInventoryInitialized();
        if(inventory == null && this.recreateNullInventory) {
            setInventory(constructInventory());
        }
        return inventory;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        ensureInventoryInitialized();
        if (this.capabilityItemHandler.isPresent() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.capabilityItemHandler.cast();
        }
        if (this.capabilityItemHandlerSlotless.isPresent() && capability == Capabilities.SLOTLESS_ITEMHANDLER) {
            return this.capabilityItemHandlerSlotless.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean canInteractWith(PlayerEntity entityPlayer) {
        return getSizeSingular() > 1 && super.canInteractWith(entityPlayer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        int size = getSizeSingular();
        return new AxisAlignedBB(getPos().subtract(new Vec3i(size, size, size)), getPos().add(size, size * 2, size));
    }

    public void setCenter(Vec3d center) {
        Direction rotation;
        double dx = Math.abs(center.x - getPos().getX());
        double dz = Math.abs(center.z - getPos().getZ());
        boolean equal = (center.x - getPos().getX()) == (center.z - getPos().getZ());
        if(dx > dz || (!equal && getSizeSingular() == 2)) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.x - getPos().getX()));
        } else {
            rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(center.z - getPos().getZ()));
        }
        this.setRotation(rotation);
        this.renderOffset = new Vec3d(getPos().getX() - center.x, getPos().getY() - center.y, getPos().getZ() - center.z);
    }

    public void setRotation(Direction rotation) {
        this.rotation = rotation.ordinal();
    }

    @Override
    public Direction getRotation() {
        return Direction.byIndex(this.rotation);
    }

    public Vec3d getRenderOffset() {
        return this.renderOffset;
    }

    public void setRenderOffset(Vec3d renderOffset) {
        this.renderOffset = renderOffset;
    }

    /**
     * Callback for when a structure has been detected for a spirit furnace block.
     * @param world The world.
     * @param location The location of one block of the structure.
     * @param size The size of the structure.
     * @param valid If the structure is being validated(/created), otherwise invalidated.
     * @param originCorner The origin corner
     */
    public static void detectStructure(IWorldReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {

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

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    public void addInterface(Vec3i blockPos) {
        interfaceLocations.add(blockPos);
    }

    public List<Vec3i> getInterfaceLocations() {
        return Collections.unmodifiableList(interfaceLocations);
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? customName : new TranslationTextComponent("general.colossalchests.colossalchest",
                new TranslationTextComponent(getMaterial().getUnlocalizedName()), getSizeSingular());
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerColossalChest(id, playerInventory, this.getInventory());
    }
}
