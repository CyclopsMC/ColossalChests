package org.cyclops.colossalchests.blockentity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.inventory.INBTInventory;
import org.cyclops.cyclopscore.inventory.IndexedInventory;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
@OnlyIn(value = Dist.CLIENT, _interface = LidBlockEntity.class)
public class BlockEntityColossalChest extends CyclopsBlockEntity implements MenuProvider, LidBlockEntity {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState blockState) {
            BlockEntityColossalChest.playSound(level, pos, blockState, SoundEvents.CHEST_OPEN);
        }

        protected void onClose(Level level, BlockPos pos, BlockState blockState) {
            BlockEntityColossalChest.playSound(level, pos, blockState, SoundEvents.CHEST_CLOSE);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState blockState, int p_155364_, int p_155365_) {
            BlockEntityColossalChest.this.signalOpenCount(level, pos, blockState, p_155364_, p_155365_);
        }

        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof ContainerColossalChest)) {
                return false;
            } else {
                Container container = ((ContainerColossalChest)player.containerMenu).getContainerInventory();
                return container == BlockEntityColossalChest.this.getInventory();
            }
        }
    };
    private final ChestLidController chestLidController = new ChestLidController();

    private SimpleInventory lastValidInventory = null;
    private SimpleInventory inventory = null;
    private LazyOptional<IItemHandler> capabilityItemHandler = LazyOptional.empty();

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.ZERO);
    @NBTPersist
    private Vec3 renderOffset = new Vec3(0, 0, 0);
    private Component customName = null;
    @NBTPersist
    private int materialId = 0;
    @NBTPersist
    private int rotation = 0;
    @NBTPersist(useDefaultValue = false)
    private List<Vec3i> interfaceLocations = Lists.newArrayList();

    private boolean recreateNullInventory = true;

    private EnumFacingMap<int[]> facingSlots = EnumFacingMap.newMap();

    public BlockEntityColossalChest(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_COLOSSAL_CHEST, blockPos, blockState);
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
                while(slot < Math.min(this.lastValidInventory.getContainerSize(), this.inventory.getContainerSize())) {
                    ItemStack contents = this.lastValidInventory.getItem(slot);
                    if (!contents.isEmpty()) {
                        this.inventory.setItem(slot, contents);
                        this.lastValidInventory.setItem(slot, ItemStack.EMPTY);
                    }
                    slot++;
                }
                if(slot < this.lastValidInventory.getContainerSize()) {
                    InventoryHelpers.dropItems(getLevel(), this.lastValidInventory, getBlockPos());
                }
                this.lastValidInventory = null;
            }
        } else {
            interfaceLocations.clear();
            if(this.inventory != null) {
                if(GeneralConfig.ejectItemsOnDestroy) {
                    InventoryHelpers.dropItems(getLevel(), this.inventory, getBlockPos());
                    this.lastValidInventory = null;
                } else {
                    this.lastValidInventory = this.inventory;
                }
            }
            setInventory(new LargeInventory(0, 0));
        }

        // Send an immediate update
        BlockHelpers.markForUpdate(getLevel(), getBlockPos());
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
        return getLevel() != null && getLevel().isClientSide;
    }

    protected LargeInventory constructInventory() {
        if (!isClientSide() && GeneralConfig.creativeChests) {
            return constructInventoryDebug();
        }
        return !isClientSide() ? new IndexedInventory(calculateInventorySize(), 64) {
            @Override
            public void startOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.startOpen(entityPlayer);
                    BlockEntityColossalChest.this.startOpen(entityPlayer);
                }
            }

            @Override
            public void stopOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.stopOpen(entityPlayer);
                    BlockEntityColossalChest.this.stopOpen(entityPlayer);
                }
            }
        } : new LargeInventory(calculateInventorySize(), 64);
    }

    protected LargeInventory constructInventoryDebug() {
        LargeInventory inv = !isClientSide() ? new IndexedInventory(calculateInventorySize(), 64)
                : new LargeInventory(calculateInventorySize(), 64);
        Random random = new Random();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            inv.setItem(i, new ItemStack(Iterables.get(ForgeRegistries.ITEMS.getValues(),
                    random.nextInt(ForgeRegistries.ITEMS.getValues().size()))));
        }
        return inv;
    }

    @Override
    public CompoundTag getUpdateTag() {
        // Don't send the inventory to the client.
        // The client will receive the data once the gui is opened.
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;
        this.inventory = null;
        this.lastValidInventory = null;
        this.recreateNullInventory = false;
        CompoundTag tag = super.getUpdateTag();
        this.inventory = oldInventory;
        this.lastValidInventory = oldLastInventory;
        this.recreateNullInventory = true;
        return tag;
    }

    @Override
    public void read(CompoundTag tag) {
        SimpleInventory oldInventory = this.inventory;
        SimpleInventory oldLastInventory = this.lastValidInventory;

        if (getLevel() != null && getLevel().isClientSide) {
            // Don't read the inventory on the client.
            // The client will receive the data once the gui is opened.
            this.inventory = null;
            this.lastValidInventory = null;
            this.recreateNullInventory = false;
        }
        super.read(tag);
        if (getLevel() != null && getLevel().isClientSide) {
            this.inventory = oldInventory;
            this.lastValidInventory = oldLastInventory;
            this.recreateNullInventory = true;
        } else {
            getInventory().read(tag.getCompound("inventory"));
            if (tag.contains("lastValidInventory", Tag.TAG_COMPOUND)) {
                this.lastValidInventory = new LargeInventory(tag.getInt("lastValidInventorySize"), this.inventory.getMaxStackSize());
                this.lastValidInventory.read(tag.getCompound("lastValidInventory"));
            }
        }

        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
        if (this.inventory != null) {
            CompoundTag subTag = new CompoundTag();
            this.inventory.write(subTag);
            tag.put("inventory", subTag);
        }
        if (this.lastValidInventory != null) {
            CompoundTag subTag = new CompoundTag();
            this.lastValidInventory.write(subTag);
            tag.put("lastValidInventory", subTag);
            tag.putInt("lastValidInventorySize", this.lastValidInventory.getContainerSize());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, (blockEntity) -> getUpdateTag());
    }

    protected int calculateInventorySize() {
        int size = getSizeSingular();
        if (size == 1) {
            return 0;
        }
        return (int) Math.ceil((Math.pow(size, 3) * 27) * getMaterial().getInventoryMultiplier() / 9) * 9;
    }

    public void setInventory(SimpleInventory inventory) {
        this.capabilityItemHandler.invalidate();
        this.inventory = inventory;
        if (this.inventory.getContainerSize() > 0) {
            IItemHandler itemHandler = new InvWrapper(this.inventory);
            this.capabilityItemHandler = LazyOptional.of(() -> itemHandler);
        } else {
            this.capabilityItemHandler = LazyOptional.empty();
        }
    }

    protected void ensureInventoryInitialized() {
        if (getLevel() != null && getLevel().isClientSide && (inventory == null || inventory.getContainerSize() != calculateInventorySize())) {
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
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean canInteractWith(Player entityPlayer) {
        return getSizeSingular() > 1 && super.canInteractWith(entityPlayer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        int size = getSizeSingular();
        return new AABB(getBlockPos().subtract(new Vec3i(size, size, size)), getBlockPos().offset(size, size * 2, size));
    }

    public void setCenter(Vec3 center) {
        Direction rotation;
        double dx = Math.abs(center.x - getBlockPos().getX());
        double dz = Math.abs(center.z - getBlockPos().getZ());
        boolean equal = (center.x - getBlockPos().getX()) == (center.z - getBlockPos().getZ());
        if(dx > dz || (!equal && getSizeSingular() == 2)) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.x - getBlockPos().getX()));
        } else {
            rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(center.z - getBlockPos().getZ()));
        }
        this.setRotation(rotation);
        this.renderOffset = new Vec3(getBlockPos().getX() - center.x, getBlockPos().getY() - center.y, getBlockPos().getZ() - center.z);
    }

    public void setRotation(Direction rotation) {
        this.rotation = rotation.ordinal();
    }

    @Override
    public Direction getRotation() {
        return Direction.from3DDataValue(this.rotation);
    }

    public Vec3 getRenderOffset() {
        return this.renderOffset;
    }

    public void setRenderOffset(Vec3 renderOffset) {
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
    public static void detectStructure(LevelReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {

    }

    /**
     * @return If the structure is valid.
     */
    public boolean isStructureComplete() {
        return !getSize().equals(Vec3i.ZERO);
    }

    public static Vec3i getMaxSize() {
        int size = ColossalChestConfig.maxSize - 1;
        return new Vec3i(size, size, size);
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    public void addInterface(Vec3i blockPos) {
        interfaceLocations.add(blockPos);
    }

    public List<Vec3i> getInterfaceLocations() {
        return Collections.unmodifiableList(interfaceLocations);
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? customName : new TranslatableComponent("general.colossalchests.colossalchest",
                new TranslatableComponent(getMaterial().getUnlocalizedName()), getSizeSingular());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerColossalChest(id, playerInventory, this.getInventory());
    }

    static void playSound(Level level, BlockPos pos, BlockState blockState, SoundEvent soundEvent) {
        level.playSound((Player)null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, soundEvent, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean triggerEvent(int eventType, int value) {
        if (eventType == 1) {
            this.chestLidController.shouldBeOpen(value > 0);
            return true;
        } else {
            return super.triggerEvent(eventType, value);
        }
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected void signalOpenCount(Level level, BlockPos pos, BlockState blockState, int p_155336_, int value) {
        Block block = blockState.getBlock();
        level.blockEvent(pos, block, 1, value);
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState blockState, BlockEntityColossalChest blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float value) {
        return this.chestLidController.getOpenness(value);
    }
}
