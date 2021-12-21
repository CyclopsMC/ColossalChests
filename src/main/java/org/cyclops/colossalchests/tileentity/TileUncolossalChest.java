package org.cyclops.colossalchests.tileentity;

import lombok.experimental.Delegate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.UncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import javax.annotation.Nullable;
import java.util.List;

import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.ITickingTile;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity.TickingTileComponent;

/**
 * A machine that can infuse things with blood.
 * @author rubensworks
 *
 */
@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class TileUncolossalChest extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile, INamedContainerProvider, IChestLid {

    private static final int TICK_MODULUS = 200;

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    private ITextComponent customName = null;

    private final SimpleInventory inventory;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;

    public TileUncolossalChest() {
        super(RegistryEntries.TILE_ENTITY_UNCOLOSSAL_CHEST);
        this.inventory = new SimpleInventory(5, 64) {
            @Override
            public void startOpen(PlayerEntity entityPlayer) {
                if (!entityPlayer.isSpectator()) {
                    super.startOpen(entityPlayer);
                    triggerPlayerUsageChange(1);
                }
            }

            @Override
            public void stopOpen(PlayerEntity entityPlayer) {
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
    public void read(CompoundNBT tag) {
        super.read(tag);
        inventory.read(tag.getCompound("inventory"));
        if (tag.contains("CustomName", Constants.NBT.TAG_STRING)) {
            this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        CompoundNBT subTag = new CompoundNBT();
        inventory.write(subTag);
        tag.put("inventory", subTag);
        if (this.customName != null) {
            tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        return super.save(tag);
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        // Resynchronize clients with the server state, the last condition makes sure
        // not all chests are synced at the same time.
        if(level != null
                && !this.level.isClientSide
                && this.playersUsing != 0
                && WorldHelpers.efficientTick(level, TICK_MODULUS, getBlockPos().hashCode())) {
            this.playersUsing = 0;
            float range = 5.0F;
            @SuppressWarnings("unchecked")
            List<PlayerEntity> entities = this.level.getEntitiesOfClass(
                    PlayerEntity.class,
                    new AxisAlignedBB(
                            getBlockPos().offset(new Vector3i(-range, -range, -range)),
                            getBlockPos().offset(new Vector3i(1 + range, 1 + range, 1 + range))
                    )
            );

            for(PlayerEntity player : entities) {
                if (player.containerMenu instanceof ContainerColossalChest) {
                    ++this.playersUsing;
                }
            }

            level.blockEvent(getBlockPos(), RegistryEntries.BLOCK_UNCOLOSSAL_CHEST, 1, playersUsing);
        }

        prevLidAngle = lidAngle;
        float increaseAngle = 0.25F;
        if (playersUsing > 0 && lidAngle == 0.0F) {
            level.playLocalSound(
                    (double) getBlockPos().getX() + 0.5D,
                    (double) getBlockPos().getY() + 0.5D,
                    (double) getBlockPos().getZ() + 0.5D,
                    SoundEvents.CHEST_OPEN,
                    SoundCategory.BLOCKS,
                    0.5F,
                    level.random.nextFloat() * 0.2F + 1.15F,
                    false
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
                level.playLocalSound(
                        (double) getBlockPos().getX() + 0.5D,
                        (double) getBlockPos().getY() + 0.5D,
                        (double) getBlockPos().getZ() + 0.5D,
                        SoundEvents.CHEST_CLOSE,
                        SoundCategory.BLOCKS,
                        0.5F,
                        level.random.nextFloat() * 0.2F + 1.15F,
                        false
                );
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
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

    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? customName : new TranslationTextComponent("general.colossalchests.uncolossalchest");
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
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerUncolossalChest(id, playerInventory, this.getInventory());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getOpenNess(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }
}
