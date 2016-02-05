package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.block.property.BlockPropertyManagerComponent;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

import java.util.List;

/**
 * Part of the Colossal Blood Chest multiblock structure.
 * @author rubensworks
 *
 */
public class ChestWall extends ConfigurableBlock implements CubeDetector.IDetectionListener {

    @BlockProperty
    public static final PropertyBool ACTIVE = ColossalChest.ACTIVE;
    @BlockProperty
    public static final PropertyMaterial MATERIAL = ColossalChest.MATERIAL;

    private static ChestWall _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ChestWall getInstance() {
        return _instance;
    }

    public ChestWall(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.rock);
        this.setHardness(5.0F);
        this.setStepSound(soundTypeWood);
        this.setHarvestLevel("axe", 2); // Iron tier
    }

    @Override
    public boolean isToolEffective(String type, IBlockState state) {
        return ColossalChest.isToolEffectiveShared(type, state);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
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

    @Override
    public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if(!world.captureBlockSnapshots) {
            ColossalChest.triggerDetector(world, pos, true);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos blockPos, IBlockState blockState) {
        super.onBlockAdded(world, blockPos, blockState);
        if(!world.captureBlockSnapshots) {
            ColossalChest.triggerDetector(world, blockPos, true);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if((Boolean)state.getValue(ACTIVE)) ColossalChest.triggerDetector(world, pos, false);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onDetect(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            boolean change = !(Boolean) world.getBlockState(location).getValue(ACTIVE);
            world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            if(change) {
                TileColossalChest.detectStructure(world, location, size, valid, originCorner);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumFacing side,
                                    float posX, float posY, float posZ) {
        if((Boolean) blockState.getValue(ACTIVE)) {
            BlockPos tileLocation = ColossalChest.getCoreLocation(world, blockPos);
            if(tileLocation != null) {
                world.getBlockState(tileLocation).getBlock().
                        onBlockActivated(world, tileLocation, world.getBlockState(tileLocation),
                                player, side, posX, posY, posZ);
                return true;
            }
        } else {
            ColossalChest.addPlayerChatError(world, blockPos, player);
        }
        return super.onBlockActivated(world, blockPos, blockState, player, side, posX, posY, posZ);
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
    public int damageDropped(IBlockState state) {
        return state.getValue(ColossalChest.MATERIAL).ordinal();
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && ColossalChest.canPlace(worldIn, pos);
    }
}
