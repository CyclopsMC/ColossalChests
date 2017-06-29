package org.cyclops.colossalchests.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.Interface;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.cyclopscore.inventory.PlayerInventoryIterator;

/**
 * An item to upgrade chests to the next tier.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ItemUpgradeTool extends ConfigurableItem {

    private static ItemUpgradeTool _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemUpgradeTool getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemUpgradeTool(ExtendedConfig eConfig) {
        super(eConfig);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + (itemStack.getMetadata() == 0 ? "" : ".reverse");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, final World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        IBlockState blockState = world.getBlockState(pos);
        if (BlockHelpers.getSafeBlockStateProperty(blockState, ColossalChest.ACTIVE, false)) {
            // Determine the chest core location
            BlockPos tileLocation = ColossalChest.getCoreLocation(world, pos);
            final TileColossalChest tile = TileHelpers.getSafeTile(world, tileLocation, TileColossalChest.class);

            // Determine the new material type
            PropertyMaterial.Type newType = transformType(itemStack, tile.getMaterial());
            if (newType == null) {
                if(world.isRemote) {
                    return EnumActionResult.PASS;
                }
                ColossalChest.addPlayerChatError(player, new L10NHelpers.UnlocalizedString(
                        "multiblock.colossalchests.error.upgradeLimit"));
                return EnumActionResult.FAIL;
            }

            // Loop over the up/downgrade tiers until one works.
            L10NHelpers.UnlocalizedString firstError = null;
            do {
                L10NHelpers.UnlocalizedString error = attemptTransform(world, pos, player, tile, newType, tile.getMaterial());
                if (error != null) {
                    if (firstError == null) {
                        firstError = error;
                    }
                } else {
                    return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
                }
            } while((newType = transformType(itemStack, newType)) != null);

            ColossalChest.addPlayerChatError(player, firstError);
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.FAIL;
        }
        return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    }

    protected L10NHelpers.UnlocalizedString attemptTransform(final World world, BlockPos pos, EntityPlayer player,
                                                final TileColossalChest tile, final PropertyMaterial.Type newType,
                                                final PropertyMaterial.Type currentType) {
        int currentItemMeta = currentType.ordinal();
        int requiredItemMeta = newType.ordinal();
        Vec3i size = tile.getSize();

        // Calculate required item blocks
        final ItemStack requiredCores = new ItemStack(ColossalChest.getInstance(), 0, requiredItemMeta);
        final ItemStack requiredInterfaces = new ItemStack(Interface.getInstance(), 0, requiredItemMeta);
        final ItemStack requiredWalls = new ItemStack(ChestWall.getInstance(), 0, requiredItemMeta);
        TileColossalChest.detector.detect(world, pos, null, new CubeDetector.IValidationAction() {
            @Override
            public L10NHelpers.UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
                if (blockState.getBlock() == ColossalChest.getInstance()) {
                    requiredCores.grow(1);
                } else if (blockState.getBlock() == Interface.getInstance()) {
                    requiredInterfaces.grow(1);
                } else if (blockState.getBlock() == ChestWall.getInstance()) {
                    requiredWalls.grow(1);
                }
                return null;
            }
        }, false);

        // Check required items in inventory
        if (!(consumeItems(player, requiredCores, true)
                && consumeItems(player, requiredInterfaces, true)
                && consumeItems(player, requiredWalls, true))) {
            return new L10NHelpers.UnlocalizedString(
                    "multiblock.colossalchests.error.upgrade", requiredCores.getCount(),
                    requiredInterfaces.getCount(), requiredWalls.getCount(), newType.getLocalizedName());
        }

        // Actually consume the items
        consumeItems(player, requiredCores.copy(), false);
        consumeItems(player, requiredInterfaces.copy(), false);
        consumeItems(player, requiredWalls.copy(), false);

        // Update the chest material
        if(!world.isRemote) {
            tile.setSize(Vec3i.NULL_VECTOR);
            tile.setMaterial(newType);
            TileColossalChest.detector.detect(world, pos, null, new CubeDetector.IValidationAction() {
                @Override
                public L10NHelpers.UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
                    world.setBlockState(location, blockState.withProperty(ColossalChest.MATERIAL, newType),
                            MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
                    if (blockState.getBlock() == ColossalChest.getInstance()
                            || blockState.getBlock() == Interface.getInstance()) {
                        tile.addInterface(location);
                    }
                    return null;
                }
            }, false);
            tile.setSize(size); // To trigger the chest size to be updated
        }

        // Add the lower tier items to the players inventory again.
        ItemStack returnedCores = requiredCores.copy();
        ItemStack returnedInterfaces = requiredInterfaces.copy();
        ItemStack returnedWalls = requiredWalls.copy();
        returnedCores.setItemDamage(currentItemMeta);
        returnedInterfaces.setItemDamage(currentItemMeta);
        returnedWalls.setItemDamage(currentItemMeta);
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedCores);
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedInterfaces);
        InventoryHelpers.tryReAddToStack(player, ItemStack.EMPTY, returnedWalls);

        return null;
    }

    protected boolean consumeItems(EntityPlayer player, ItemStack consumeStack, boolean simulate) {
        if (player.capabilities.isCreativeMode) {
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

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (!ItemStackHelpers.isValidCreativeTab(this, tab)) return;
        super.getSubItems(tab, subItems);
        subItems.add(new ItemStack(this, 1, 1));
    }

    protected PropertyMaterial.Type transformType(ItemStack itemStack, PropertyMaterial.Type type) {
        if (itemStack.getMetadata() == 0 && type.ordinal() < PropertyMaterial.Type.values().length - 1) {
            return PropertyMaterial.Type.values()[type.ordinal() + 1];
        } else if (itemStack.getMetadata() == 1 && type.ordinal() > 0) {
            return PropertyMaterial.Type.values()[type.ordinal() - 1];
        }
        return null;
    }

}
