package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.UncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
public class BlockEntityUncolossalChest extends CyclopsBlockEntity implements MenuProvider, LidBlockEntity {

    private static final int TICK_MODULUS = 200;

    private Component customName = null;

    private final SimpleInventory inventory;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;

    public BlockEntityUncolossalChest(BlockPos blockPos, BlockState blockState) {
        super(RegistryEntries.BLOCK_ENTITY_UNCOLOSSAL_CHEST, blockPos, blockState);
        this.inventory = new SimpleInventory(5, 64) {
            @Override
            public void startOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.startOpen(entityPlayer);
                    triggerPlayerUsageChange(1);
                }
            }

            @Override
            public void stopOpen(Player entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.stopOpen(entityPlayer);
                    triggerPlayerUsageChange(-1);
                }
            }
        };
        this.inventory.addDirtyMarkListener(this);
        addCapabilityInternal(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, LazyOptional.of(() -> new InvWrapper(this.inventory)));
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        inventory.read(tag.getCompound("inventory"));
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag subTag = new CompoundTag();
        inventory.write(subTag);
        tag.put("inventory", subTag);
        if (this.customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            playersUsing = j;
        }
        return true;
    }

    private void triggerPlayerUsageChange(int change) {
        if (level != null) {
            playersUsing += change;
            level.blockEvent(getBlockPos(), RegistryEntries.BLOCK_UNCOLOSSAL_CHEST, 1, playersUsing);
        }
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? customName : new TranslatableComponent("general.colossalchests.uncolossalchest");
    }

    @Override
    public Direction getRotation() {
        // World is null in itemstack renderer
        if (getLevel() == null) {
            return Direction.SOUTH;
        }

        BlockState blockState = getLevel().getBlockState(getBlockPos());
        if(blockState.getBlock() != RegistryEntries.BLOCK_UNCOLOSSAL_CHEST) return Direction.NORTH;
        return BlockHelpers.getSafeBlockStateProperty(blockState, UncolossalChest.FACING, Direction.NORTH);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerUncolossalChest(id, playerInventory, this.getInventory());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getOpenNess(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

    public static class Ticker extends BlockEntityTickerDelayed<BlockEntityUncolossalChest> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, BlockEntityUncolossalChest blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            // Resynchronize clients with the server state, the last condition makes sure
            // not all chests are synced at the same time.
            if(level != null
                    && !level.isClientSide
                    && blockEntity.playersUsing != 0
                    && WorldHelpers.efficientTick(level, TICK_MODULUS, pos.hashCode())) {
                blockEntity.playersUsing = 0;
                float range = 5.0F;
                @SuppressWarnings("unchecked")
                List<Player> entities = level.getEntitiesOfClass(
                        Player.class,
                        new AABB(
                                pos.offset(new Vec3i(-range, -range, -range)),
                                pos.offset(new Vec3i(1 + range, 1 + range, 1 + range))
                        )
                );

                for(Player player : entities) {
                    if (player.containerMenu instanceof ContainerColossalChest) {
                        ++blockEntity.playersUsing;
                    }
                }

                level.blockEvent(pos, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST, 1, blockEntity.playersUsing);
            }

            blockEntity.prevLidAngle = blockEntity.lidAngle;
            float increaseAngle = 0.25F;
            if (blockEntity.playersUsing > 0 && blockEntity.lidAngle == 0.0F) {
                level.playLocalSound(
                        (double) pos.getX() + 0.5D,
                        (double) pos.getY() + 0.5D,
                        (double) pos.getZ() + 0.5D,
                        SoundEvents.CHEST_OPEN,
                        SoundSource.BLOCKS,
                        0.5F,
                        level.random.nextFloat() * 0.2F + 1.15F,
                        false
                );
            }
            if (blockEntity.playersUsing == 0 && blockEntity.lidAngle > 0.0F || blockEntity.playersUsing > 0 && blockEntity.lidAngle < 1.0F) {
                float preIncreaseAngle = blockEntity.lidAngle;
                if (blockEntity.playersUsing > 0) {
                    blockEntity.lidAngle += increaseAngle;
                } else {
                    blockEntity.lidAngle -= increaseAngle;
                }
                if (blockEntity.lidAngle > 1.0F) {
                    blockEntity.lidAngle = 1.0F;
                }
                float closedAngle = 0.5F;
                if (blockEntity.lidAngle < closedAngle && preIncreaseAngle >= closedAngle) {
                    level.playLocalSound(
                            (double) pos.getX() + 0.5D,
                            (double) pos.getY() + 0.5D,
                            (double) pos.getZ() + 0.5D,
                            SoundEvents.CHEST_CLOSE,
                            SoundSource.BLOCKS,
                            0.5F,
                            level.random.nextFloat() * 0.2F + 1.15F,
                            false
                    );
                }
                if (blockEntity.lidAngle < 0.0F) {
                    blockEntity.lidAngle = 0.0F;
                }
            }
        }
    }
}
