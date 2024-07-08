package org.cyclops.colossalchests.item;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.IBlockChestMaterial;
import org.cyclops.colossalchests.block.Interface;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.PlayerInventoryIterator;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

import java.util.List;

/**
 * An item to upgrade chests to the next tier.
 * @author rubensworks
 */
public class ItemUpgradeTool extends Item {

    private final boolean upgrade;

    public ItemUpgradeTool(Properties properties, boolean upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (blockState.getBlock() instanceof IBlockChestMaterial
                && BlockHelpers.getSafeBlockStateProperty(blockState, ColossalChest.ENABLED, false)) {
            // Determine the chest core location
            BlockPos tileLocation = ColossalChest.getCoreLocation(((IBlockChestMaterial) blockState.getBlock()).getMaterial(), context.getLevel(), context.getClickedPos());
            final BlockEntityColossalChest tile = BlockEntityHelpers.get(context.getLevel(), tileLocation, BlockEntityColossalChest.class).orElse(null);

            // Determine the new material type
            ChestMaterial newType = transformType(itemStack, tile.getMaterial());
            if (newType == null) {
                if(context.getLevel().isClientSide()) {
                    return InteractionResult.PASS;
                }
                ColossalChest.addPlayerChatError(context.getPlayer(), Component.translatable(
                        "multiblock.colossalchests.error.upgradeLimit"));
                return InteractionResult.FAIL;
            }

            // Loop over the up/downgrade tiers until one works.
            Component firstError = null;
            do {
                Component error = attemptTransform(context.getLevel(), context.getClickedPos(), context.getPlayer(), tile, newType, tile.getMaterial(), context.getHand());
                if (error != null) {
                    if (firstError == null) {
                        firstError = error;
                    }
                } else {
                    return context.getLevel().isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
                }
            } while((newType = transformType(itemStack, newType)) != null);

            ColossalChest.addPlayerChatError(context.getPlayer(), firstError);
            return context.getLevel().isClientSide() ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return context.getLevel().isClientSide() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    protected Component attemptTransform(final Level world, BlockPos pos, Player player,
                                         final BlockEntityColossalChest tile, final ChestMaterial newType,
                                         final ChestMaterial currentType, InteractionHand hand) {
        Vec3i size = tile.getSize();

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
            return Component.translatable("multiblock.colossalchests.error.unexpected");
        }

        // Check required items in inventory
        if (!(consumeItems(player, requiredCores, true)
                && consumeItems(player, requiredInterfaces, true)
                && consumeItems(player, requiredWalls, true))) {
            return Component.translatable(
                    "multiblock.colossalchests.error.upgrade", requiredCores.getCount(),
                    requiredInterfaces.getCount(), requiredWalls.getCount(), Component.translatable(newType.getUnlocalizedName()));
        }

        // Actually consume the items
        consumeItems(player, requiredCores.copy(), false);
        consumeItems(player, requiredInterfaces.copy(), false);
        consumeItems(player, requiredWalls.copy(), false);

        // Update the chest material and move the contents to the new tile
        if(!world.isClientSide) {
            tile.setSize(Vec3i.ZERO);
            SimpleInventory oldInventory = tile.getLastValidInventory();
            Direction oldRotation = tile.getRotation();
            Vec3 oldRenderOffset = tile.getRenderOffset();
            List<Vec3i> oldInterfaceLocations = Lists.newArrayList(tile.getInterfaceLocations());
            Wrapper<BlockPos> coreLocation = new Wrapper<>(null);
            List<BlockPos> interfaceLocations = Lists.newArrayList();
            validMaterial.getChestDetector().detect(world, pos, null, (location, blockState) -> {
                BlockState blockStateNew = null;
                if (blockState.getBlock() instanceof ColossalChest) {
                    coreLocation.set(location);
                    blockStateNew = newType.getBlockCore().defaultBlockState();
                } else if (blockState.getBlock() instanceof Interface) {
                    blockStateNew = newType.getBlockInterface().defaultBlockState();
                    interfaceLocations.add(location);
                } else if (blockState.getBlock() instanceof ChestWall) {
                    blockStateNew = newType.getBlockWall().defaultBlockState();
                }

                world.setBlock(location, blockStateNew.setValue(ColossalChest.ENABLED, blockState.getValue(ColossalChest.ENABLED)),
                        MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
                if (blockState.getBlock() instanceof ColossalChest
                        || blockState.getBlock() instanceof Interface) {
                    tile.addInterface(location);
                }
                return null;
            }, false);

            // From this point on, use the new tile entity
            BlockEntityColossalChest tileNew = BlockEntityHelpers.get(world, coreLocation.get(), BlockEntityColossalChest.class)
                    .orElseThrow(() -> new IllegalStateException("Could not find a colossal chest core location during upgrading."));
            tileNew.setLastValidInventory(oldInventory);
            tileNew.setMaterial(newType);
            tileNew.setRotation(oldRotation);
            tileNew.setRenderOffset(oldRenderOffset);
            for (Vec3i oldInterfaceLocation : oldInterfaceLocations) {
                tileNew.addInterface(oldInterfaceLocation);
            }
            tileNew.setSize(size); // To trigger the chest size to be updated

            // Set the core position into the newly transformed interfaces
            for (BlockPos interfaceLocation : interfaceLocations) {
                BlockEntityInterface tileInterface = BlockEntityHelpers.get(world, interfaceLocation, BlockEntityInterface.class)
                        .orElseThrow(() -> new IllegalStateException("Could not find a colossal chest interface location during upgrading."));
                tileInterface.setCorePosition(coreLocation.get());
            }

            RegistryEntries.TRIGGER_CHEST_FORMED.get().test((ServerPlayer) player, newType, size.getX() + 1);
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

    protected boolean consumeItems(Player player, ItemStack consumeStack, boolean simulate) {
        if (player.isCreative()) {
            return true;
        }
        PlayerInventoryIterator it = new PlayerInventoryIterator(player);
        int validItems = 0;
        while (it.hasNext()) {
            ItemStack stack = it.next();
            if (!stack.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(stack, consumeStack)) {
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
