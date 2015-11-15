package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import org.cyclops.colossalchests.tileentity.TileInterface;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;

/**
 * Part of the Colossal Blood Chest multiblock structure.
 * @author rubensworks
 *
 */
public class Interface extends ConfigurableBlockContainer implements CubeDetector.IDetectionListener {

    @BlockProperty
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    private static Interface _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static Interface getInstance() {
        return _instance;
    }

    public Interface(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, Material.rock, TileInterface.class);
        this.setHardness(5.0F);
        this.setStepSound(soundTypeWood);
        this.setHarvestLevel("axe", 2); // Iron tier
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

    private void triggerDetector(World world, BlockPos blockPos, boolean valid) {
        TileColossalChest.detector.detect(world, blockPos, valid ? null : blockPos, true);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
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
            boolean change = !(Boolean) world.getBlockState(location).getValue(ACTIVE);
            world.setBlockState(location, world.getBlockState(location).withProperty(ACTIVE, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            if(change) {
                BlockPos tileLocation = ColossalChest.getCoreLocation(world, location);
                TileInterface tile = TileHelpers.getSafeTile(world, location, TileInterface.class);
                if(tile != null && tileLocation != null) {
                    tile.setCorePosition(tileLocation);
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumFacing side,
                                    float posX, float posY, float posZ) {
        if((Boolean) blockState.getValue(ACTIVE)) {
            TileInterface tile = TileHelpers.getSafeTile(world, blockPos, TileInterface.class);
            if(tile != null && tile.getCorePosition() != null) {
                BlockPos tileLocation = new BlockPos(tile.getCorePosition());
                world.getBlockState(tileLocation).getBlock().
                        onBlockActivated(world, tileLocation, world.getBlockState(tileLocation),
                                player, side, posX, posY, posZ);
                return true;
            }
        }
        return super.onBlockActivated(world, blockPos, blockState, player, side, posX, posY, posZ);
    }

}
