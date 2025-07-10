package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.UncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

import javax.annotation.Nullable;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class BlockEntityUncolossalChest extends CyclopsBlockEntity implements MenuProvider, LidBlockEntity {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState blockState) {
            BlockEntityUncolossalChest.playSound(level, pos, blockState, SoundEvents.CHEST_OPEN);
        }

        protected void onClose(Level level, BlockPos pos, BlockState blockState) {
            BlockEntityUncolossalChest.playSound(level, pos, blockState, SoundEvents.CHEST_CLOSE);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState blockState, int p_155364_, int p_155365_) {
            BlockEntityUncolossalChest.this.signalOpenCount(level, pos, blockState, p_155364_, p_155365_);
        }

        protected boolean isOwnContainer(Player player) {
            if (!(player.containerMenu instanceof ContainerUncolossalChest)) {
                return false;
            } else {
                Container container = ((ContainerUncolossalChest)player.containerMenu).getContainerInventory();
                return container == BlockEntityUncolossalChest.this.getInventory();
            }
        }
    };
    private final ChestLidController chestLidController = new ChestLidController();

    private Component customName = null;

    private final SimpleInventory inventory;

    public BlockEntityUncolossalChest(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_UNCOLOSSAL_CHEST.value(), blockPos, blockState);
        this.inventory = new SimpleInventory(5, 64) {
            @Override
            public void startOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.startOpen(entityPlayer);
                    BlockEntityUncolossalChest.this.startOpen(entityPlayer);
                }
            }

            @Override
            public void stopOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.stopOpen(entityPlayer);
                    BlockEntityUncolossalChest.this.stopOpen(entityPlayer);
                }
            }

            @Override
            public boolean stillValid(Player entityplayer) {
                return super.stillValid(entityplayer) && level.getBlockEntity(worldPosition) == BlockEntityUncolossalChest.this;
            }
        };
        this.inventory.addDirtyMarkListener(this);
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        inventory.read(input.child("inventory").orElseThrow());
        this.customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.write(output.child("inventory"));
        if (this.customName != null) {
            output.store("CustomName", ComponentSerialization.CODEC, this.customName);
        }
    }

    static void playSound(Level level, BlockPos pos, BlockState blockState, SoundEvent soundEvent) {
        level.playSound((Player)null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, soundEvent, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.2F + 1.15F);
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

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState blockState, BlockEntityUncolossalChest blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public float getOpenNess(float value) {
        return this.chestLidController.getOpenness(value);
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? customName : Component.translatable("general.colossalchests.uncolossalchest");
    }

    @Override
    public Direction getRotation() {
        // World is null in itemstack renderer
        if (getLevel() == null) {
            return Direction.SOUTH;
        }

        BlockState blockState = getLevel().getBlockState(getBlockPos());
        if(blockState.getBlock() != RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value()) return Direction.NORTH;
        return IModHelpers.get().getBlockHelpers().getSafeBlockStateProperty(blockState, UncolossalChest.FACING, Direction.NORTH);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerUncolossalChest(id, playerInventory, this.getInventory());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        Containers.dropContents(level, pos, this.getInventory());
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }
}
