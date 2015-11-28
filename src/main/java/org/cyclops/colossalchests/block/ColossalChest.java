package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.client.gui.container.GuiColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainerGui;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * A machine that can infuse stuff with blood.
 *
 * @author rubensworks
 */
public class ColossalChest extends ConfigurableBlockContainerGui implements CubeDetector.IDetectionListener {

    @BlockProperty
    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    @BlockProperty
    public static final PropertyMaterial MATERIAL = PropertyMaterial.create("material", PropertyMaterial.Type.class);

    private static ColossalChest _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static ColossalChest getInstance() {
        return _instance;
    }

    public ColossalChest(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.rock, TileColossalChest.class);
        this.setHardness(5.0F);
        this.setStepSound(soundTypeWood);
        this.setHarvestLevel("axe", 2); // Iron tier
        this.setRotatable(false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFullCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    @Override
    public Item getItemDropped(IBlockState blockState, Random random, int zero) {
        return Item.getItemFromBlock(this);
    }

    public static DetectionResult triggerDetector(World world, BlockPos blockPos, boolean valid) {
        return TileColossalChest.detector.detect(world, blockPos, valid ? null : blockPos, new MaterialValidationAction(), true);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (stack.hasDisplayName()) {
            TileColossalChest tile = TileHelpers.getSafeTile(world, pos, TileColossalChest.class);
            if (tile != null) {
                tile.setCustomName(stack.getDisplayName());
                tile.setSize(Vec3i.NULL_VECTOR);
            }
        }
        triggerDetector(world, pos, true);
    }

    @Override
    public void onBlockAdded(World world, BlockPos blockPos, IBlockState blockState) {
        super.onBlockAdded(world, blockPos, blockState);
        triggerDetector(world, blockPos, true);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if((Boolean)state.getValue(ACTIVE)) triggerDetector(world, pos, false);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onDetect(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            TileColossalChest tile = TileHelpers.getSafeTile(world, location, TileColossalChest.class);
            if(tile != null) {
                tile.setMaterial(BlockHelpers.getSafeBlockStateProperty(
                        world.getBlockState(location), ColossalChest.MATERIAL, PropertyMaterial.Type.WOOD));
                tile.setSize(valid ? size : Vec3i.NULL_VECTOR);
                tile.setCenter(new Vec3(
                        originCorner.getX() + ((double) size.getX()) / 2,
                        originCorner.getY() + ((double) size.getY()) / 2,
                        originCorner.getZ() + ((double) size.getZ()) / 2
                ));
            }
        }
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerColossalChest.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiColossalChest.class;
    }

    /**
     * Get the core block location.
     * @param world The world.
     * @param blockPos The start position to search from.
     * @return The found location.
     */
    public static @Nullable BlockPos getCoreLocation(World world, BlockPos blockPos) {
        final Wrapper<BlockPos> tileLocationWrapper = new Wrapper<BlockPos>();
        TileColossalChest.detector.detect(world, blockPos, null, new CubeDetector.IValidationAction() {

            @Override
            public L10NHelpers.UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
                if (blockState.getBlock() == ColossalChest.getInstance()) {
                    tileLocationWrapper.set(location);
                }
                return null;
            }

        }, false);
        return tileLocationWrapper.get();
    }

    /**
     * Show the structure forming error in the given player chat window.
     * @param world The world.
     * @param blockPos The start position.
     * @param player The player.
     */
    public static void addPlayerChatError(World world, BlockPos blockPos, EntityPlayer player) {
        if(!world.isRemote && player.getHeldItem() == null) {
            DetectionResult result = TileColossalChest.detector.detect(world, blockPos, null,  new MaterialValidationAction(), false);
            if (result != null && result.getError() != null) {
                IChatComponent chat = new ChatComponentText("");
                IChatComponent prefix = new ChatComponentText(
                        String.format("[%s]: ", L10NHelpers.localize("multiblock.colossalchests.error.prefix"))
                ).setChatStyle(new ChatStyle().
                        setColor(EnumChatFormatting.GRAY).
                        setChatHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentTranslation("multiblock.colossalchests.error.prefix.info")
                        ))
                );
                IChatComponent error = new ChatComponentText(result.getError().localize());
                chat.appendSibling(prefix);
                chat.appendSibling(error);
                player.addChatComponentMessage(chat);
            } else {
                player.addChatComponentMessage(new ChatComponentText(L10NHelpers.localize(
                        "multiblock.colossalchests.error.unexpected")));
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
        for(PropertyMaterial.Type material : PropertyMaterial.Type.values()) {
            list.add(new ItemStack(getInstance(), 1, material.ordinal()));
        }
    }

    @Override
    protected BlockState createBlockState() {
        return (propertyManager = new BlockPropertyManagerComponent(this,
                new BlockPropertyManagerComponent.PropertyComparator() {
                    @Override
                    public int compare(IProperty o1, IProperty o2) {
                        return o2.getName().compareTo(o1.getName());
                    }
                },
                new BlockPropertyManagerComponent.UnlistedPropertyComparator())).createDelegatedBlockState();
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // Meta * 2 because we always want the inactive state
        return super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta * 2, placer);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumFacing side, float par7, float par8, float par9) {
        if(!((Boolean) blockState.getValue(ACTIVE))) {
            ColossalChest.addPlayerChatError(world, blockPos, player);
        }
        return super.onBlockActivated(world, blockPos, blockState, player, side, par7, par8, par9);
    }

    private static class MaterialValidationAction implements CubeDetector.IValidationAction {
        private final Wrapper<PropertyMaterial.Type> requiredMaterial;

        public MaterialValidationAction() {
            this.requiredMaterial = new Wrapper<PropertyMaterial.Type>(null);
        }

        @Override
        public L10NHelpers.UnlocalizedString onValidate(BlockPos blockPos, IBlockState blockState) {
            PropertyMaterial.Type material = BlockHelpers.
                    getSafeBlockStateProperty(blockState, ColossalChest.MATERIAL, null);
            if(requiredMaterial.get() == null) {
                requiredMaterial.set(material);
                return null;
            }
            return requiredMaterial.get() == material ? null : new L10NHelpers.UnlocalizedString(
                    "multiblock.colossalchests.error.material", material.getLocalizedName(), LocationHelpers.toCompactString(blockPos),
                    requiredMaterial.get().getLocalizedName());
        }
    }
}
