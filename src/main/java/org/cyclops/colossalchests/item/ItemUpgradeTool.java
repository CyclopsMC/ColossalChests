package org.cyclops.colossalchests.item;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.colossalchests.Advancements;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.IBlockChestMaterial;
import org.cyclops.colossalchests.block.Interface;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.colossalchests.tileentity.TileInterface;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.inventory.PlayerInventoryIterator;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

import java.util.List;

/**
 * An item to upgrade chests to the next tier.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemUpgradeTool extends Item {

    private final boolean upgrade;

    public ItemUpgradeTool(Properties properties, boolean upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack itemStack, ItemUseContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getPos());
        if (blockState.getBlock() instanceof IBlockChestMaterial
                && BlockHelpers.getSafeBlockStateProperty(blockState, ColossalChest.ENABLED, false)) {
            // Determine the chest core location
            BlockPos tileLocation = ColossalChest.getCoreLocation(((IBlockChestMaterial) blockState.getBlock()).getMaterial(), context.getWorld(), context.getPos());
            final TileColossalChest tile = TileHelpers.getSafeTile(context.getWorld(), tileLocation, TileColossalChest.class).orElse(null);

            // Determine the new material type
            ChestMaterial newType = transformType(itemStack, tile.getMaterial());
            if (newType == null) {
                if(context.getWorld().isRemote()) {
                    return ActionResultType.PASS;
                }
                ColossalChest.addPlayerChatError(context.getPlayer(), new TranslationTextComponent(
                        "multiblock.colossalchests.error.upgradeLimit"));
                return ActionResultType.FAIL;
            }

            // Loop over the up/downgrade tiers until one works.
            ITextComponent firstError = null;
            do {
                ITextComponent error = attemptTransform(context.getWorld(), context.getPos(), context.getPlayer(), tile, newType, tile.getMaterial(), context.getHand());
                if (error != null) {
                    if (firstError == null) {
                        firstError = error;
                    }
                } else {
                    return context.getWorld().isRemote() ? ActionResultType.PASS : ActionResultType.SUCCESS;
                }
            } while((newType = transformType(itemStack, newType)) != null);

            ColossalChest.addPlayerChatError(context.getPlayer(), firstError);
            return context.getWorld().isRemote() ? ActionResultType.PASS : ActionResultType.FAIL;
        }
        return context.getWorld().isRemote() ? ActionResultType.PASS : ActionResultType.SUCCESS;
    }

    protected ITextComponent attemptTransform(final World world, BlockPos pos, PlayerEntity player,
                                                             final TileColossalChest tile, final ChestMaterial newType,
                                                             final ChestMaterial currentType, Hand hand) {
        Vector3i size = tile.getSize();

        // Calculate required item blocks
        ChestMaterial validMaterial = null;
        Wrapper<Integer> requiredCoresCount = new Wrapper<>(0);
        Wrapper<Integer> requiredInterfacesCount = new Wrapper<>(0);
        Wrapper<Integer> requiredWallsCount = new Wrapper<>(0);
        for (ChestMaterial material : ChestMaterial.VALUES) {
            DetectionResult result = material.getChestDetector().detect(world, pos, null, (location, blockState) -> {
                if (blockState.getBlock() instanceof ColossalChest) {
                    requiredCoresCount.set(requiredCoresCount.get() + 1);
                } else if (blockState.getBlock() instanceof Interface) {
                    requiredInterfacesCount.set(requiredInterfacesCount.get() + 1);
                } else if (blockState.getBlock() instanceof ChestWall) {
                    requiredWallsCount.set(requiredWallsCount.get() + 1);
                }
                return null;
            }, false);
            if (result.getError() == null) {
                validMaterial = material;
                break;
            }
        }

        ItemStack requiredCores = new ItemStack(newType.getBlockCore(), requiredCoresCount.get());
        ItemStack requiredInterfaces = new ItemStack(newType.getBlockInterface(), requiredInterfacesCount.get());
        ItemStack requiredWalls = new ItemStack(newType.getBlockWall(), requiredWallsCount.get());

        if (validMaterial == null) {
            return new TranslationTextComponent("multiblock.colossalchests.error.unexpected");
        }

        // Check required items in inventory
        if (!(consumeItems(player, requiredCores, true)
                && consumeItems(player, requiredInterfaces, true)
                && consumeItems(player, requiredWalls, true))) {
            return new TranslationTextComponent(
                    "multiblock.colossalchests.error.upgrade", requiredCores.getCount(),
                    requiredInterfaces.getCount(), requiredWalls.getCount(), new TranslationTextComponent(newType.getUnlocalizedName()));
        }

        // Actually consume the items
        consumeItems(player, requiredCores.copy(), false);
        consumeItems(player, requiredInterfaces.copy(), false);
        consumeItems(player, requiredWalls.copy(), false);

        // Update the chest material and move the contents to the new tile
        if(!world.isRemote) {
            tile.setSize(Vector3i.NULL_VECTOR);
            SimpleInventory oldInventory = tile.getLastValidInventory();
            Direction oldRotation = tile.getRotation();
            Vector3d oldRenderOffset = tile.getRenderOffset();
            List<Vector3i> oldInterfaceLocations = Lists.newArrayList(tile.getInterfaceLocations());
            Wrapper<BlockPos> coreLocation = new Wrapper<>(null);
            List<BlockPos> interfaceLocations = Lists.newArrayList();
            validMaterial.getChestDetector().detect(world, pos, null, (location, blockState) -> {
                BlockState blockStateNew = null;
                if (blockState.getBlock() instanceof ColossalChest) {
                    coreLocation.set(location);
                    blockStateNew = newType.getBlockCore().getDefaultState();
                } else if (blockState.getBlock() instanceof Interface) {
                    blockStateNew = newType.getBlockInterface().getDefaultState();
                    interfaceLocations.add(location);
                } else if (blockState.getBlock() instanceof ChestWall) {
                    blockStateNew = newType.getBlockWall().getDefaultState();
                }

                world.setBlockState(location, blockStateNew.with(ColossalChest.ENABLED, blockState.get(ColossalChest.ENABLED)),
                        MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
                if (blockState.getBlock() instanceof ColossalChest
                        || blockState.getBlock() instanceof Interface) {
                    tile.addInterface(location);
                }
                return null;
            }, false);

            // From this point on, use the new tile entity
            TileColossalChest tileNew = TileHelpers.getSafeTile(world, coreLocation.get(), TileColossalChest.class)
                    .orElseThrow(() -> new IllegalStateException("Could not find a colossal chest core location during upgrading."));
            tileNew.setLastValidInventory(oldInventory);
            tileNew.setMaterial(newType);
            tileNew.setRotation(oldRotation);
            tileNew.setRenderOffset(oldRenderOffset);
            for (Vector3i oldInterfaceLocation : oldInterfaceLocations) {
                tileNew.addInterface(oldInterfaceLocation);
            }
            tileNew.setSize(size); // To trigger the chest size to be updated

            // Set the core position into the newly transformed interfaces
            for (BlockPos interfaceLocation : interfaceLocations) {
                TileInterface tileInterface = TileHelpers.getSafeTile(world, interfaceLocation, TileInterface.class)
                        .orElseThrow(() -> new IllegalStateException("Could not find a colossal chest interface location during upgrading."));
                tileInterface.setCorePosition(coreLocation.get());
            }

            Advancements.CHEST_FORMED.test((ServerPlayerEntity) player, newType, size.getX() + 1);
        }

        // Add the lower tier items to the players inventory again.
        ItemStack returnedCores = new ItemStack(currentType.getBlockCore(), requiredCores.getCount());
        ItemStack returnedInterfaces = new ItemStack(currentType.getBlockInterface(), requiredInterfaces.getCount());
        ItemStack returnedWalls = new ItemStack(currentType.getBlockWall(), requiredWalls.getCount());
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedCores, hand);
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedInterfaces, hand);
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedWalls, hand);

        return null;
    }

    protected boolean consumeItems(PlayerEntity player, ItemStack consumeStack, boolean simulate) {
        if (player.isCreative()) {
            return true;
        }
        PlayerInventoryIterator it = new PlayerInventoryIterator(player);
        int validItems = 0;
        while (it.hasNext()) {
            ItemStack stack = it.next();
            if (!stack.isEmpty()) {
                if (ItemStack.areItemsEqual(stack, consumeStack)) {
                    int previousValidItems = validItems;
                    validItems += stack.getCount();
                    validItems = Math.min(consumeStack.getCount(), validItems);
                    if (!simulate) {
                        stack.shrink(validItems - previousValidItems);
                        it.replace(stack.getCount() == 0 ? ItemStack.EMPTY : stack);
                    }
                }
            }
        }
        return validItems == consumeStack.getCount();
    }

    protected ChestMaterial transformType(ItemStack itemStack, ChestMaterial type) {
        if (upgrade && type.ordinal() < ChestMaterial.VALUES.size() - 1) {
            return ChestMaterial.VALUES.get(type.ordinal() + 1);
        } else if (!upgrade && type.ordinal() > 0) {
            return ChestMaterial.VALUES.get(type.ordinal() - 1);
        }
        return null;
    }

}
