package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockUncolossalChest;
import org.cyclops.colossalchests2.inventory.ContainerUncolossalChest;

/**
 * The contents of an uncolossal chest: five slots, opened like a vanilla chest.
 * @author rubensworks
 */
public class BlockEntityUncolossalChest extends BaseContainerBlockEntity implements LidBlockEntity {

    public static final int SIZE = 5;
    private static final int EVENT_OPENERS = 1;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), EVENT_OPENERS, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ContainerUncolossalChest menu && menu.getContainer() == BlockEntityUncolossalChest.this;
        }
    };
    private final ChestLidController lidController = new ChestLidController();
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public BlockEntityUncolossalChest(BlockPos pos, BlockState state) {
        super(RegistryEntries.BLOCK_ENTITY_UNCOLOSSAL_CHEST.value(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityUncolossalChest chest) {
        chest.lidController.tickLid();
    }

    /**
     * Higher pitched than a vanilla chest, as it is so small.
     */
    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, SoundSource.BLOCKS,
                0.5F, level.random.nextFloat() * 0.2F + 1.15F);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.colossalchests2.uncolossal_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new ContainerUncolossalChest(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_OPENERS) {
            lidController.shouldBeOpen(param > 0);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public void startOpen(Player player) {
        if (!remove && !player.isSpectator()) {
            openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!remove && !player.isSpectator()) {
            openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!remove) {
            openersCounter.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
        }
    }

    public int getOpenerCount() {
        return openersCounter.getOpenerCount();
    }

    @Override
    public float getOpenNess(float partialTick) {
        return lidController.getOpenness(partialTick);
    }

    /**
     * @return The direction the chest faces, south when rendered as an item.
     */
    public Direction getFacing() {
        BlockState state = getBlockState();
        return level != null && state.hasProperty(BlockUncolossalChest.FACING) ? state.getValue(BlockUncolossalChest.FACING) : Direction.SOUTH;
    }

}
