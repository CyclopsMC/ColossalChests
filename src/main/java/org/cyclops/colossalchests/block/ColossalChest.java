package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.colossalchests.Advancements;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.block.BlockTileGui;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;

import javax.annotation.Nullable;

/**
 * A machine that can infuse stuff with blood.
 *
 * @author rubensworks
 */
public class ColossalChest extends BlockTileGui implements CubeDetector.IDetectionListener, IBlockChestMaterial {

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private final ChestMaterial material;

    public ColossalChest(Properties properties, ChestMaterial material) {
        super(properties, TileColossalChest::new);
        this.material = material;

        material.setBlockCore(this);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(ENABLED, false));
    }

    @Override
    public String getTranslationKey() {
        String baseKey = super.getTranslationKey();
        return baseKey.substring(0, baseKey.lastIndexOf('_'));
    }

    @Override
    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    public static boolean isToolEffectiveShared(ChestMaterial material, BlockState state, ToolType tool) {
        if(material == ChestMaterial.WOOD) {
            return tool == ToolType.AXE;
        }
        return tool == ToolType.PICKAXE;
    }

    public static boolean canPlace(IWorldReader world, BlockPos pos) {
        for(Direction side : Direction.values()) {
            BlockState blockState = world.getBlockState(pos.offset(side));
            Block block = blockState.getBlock();
            if((block instanceof ColossalChest || block instanceof ChestWall || block instanceof Interface)
                    && blockState.getProperties().contains(ENABLED) && blockState.get(ENABLED)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isToolEffective(BlockState state, ToolType tool) {
        return isToolEffectiveShared(this.material, state, tool);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos,
                                    EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    public static DetectionResult triggerDetector(ChestMaterial material, IWorld world, BlockPos blockPos, boolean valid, @Nullable PlayerEntity player) {
        DetectionResult detectionResult = material.getChestDetector().detect(world, blockPos, valid ? null : blockPos, new MaterialValidationAction(), true);
        if (player instanceof ServerPlayerEntity && detectionResult.getError() == null) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.get(ENABLED)) {
                TileColossalChest tile = TileHelpers.getSafeTile(world, blockPos, TileColossalChest.class).orElse(null);
                if (tile == null) {
                    BlockPos corePos = getCoreLocation(material, world, blockPos);
                    tile = TileHelpers.getSafeTile(world, corePos, TileColossalChest.class).orElse(null);
                }

                Advancements.CHEST_FORMED.trigger((ServerPlayerEntity) player,
                        Pair.of(material, tile.getSizeSingular()));
            }
        }
        return detectionResult;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (stack.hasDisplayName()) {
            TileColossalChest tile = TileHelpers.getSafeTile(world, pos, TileColossalChest.class).orElse(null);
            if (tile != null) {
                tile.setCustomName(stack.getDisplayName());
                tile.setSize(Vec3i.NULL_VECTOR);
            }
        }
        triggerDetector(this.material, world, pos, true, placer instanceof PlayerEntity ? (PlayerEntity) placer : null);
    }

    @Override
    public void onBlockAdded(BlockState blockStateNew, World world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onBlockAdded(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!world.captureBlockSnapshots && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.get(ENABLED)) {
            triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    @Override
    public void onDetect(IWorldReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            ((IWorldWriter) world).setBlockState(location, world.getBlockState(location).with(ENABLED, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            TileColossalChest tile = TileHelpers.getSafeTile(world, location, TileColossalChest.class).orElse(null);
            if(tile != null) {
                tile.setMaterial(this.material);
                tile.setSize(valid ? size : Vec3i.NULL_VECTOR);
                tile.setCenter(new Vec3d(
                        originCorner.getX() + ((double) size.getX()) / 2,
                        originCorner.getY() + ((double) size.getY()) / 2,
                        originCorner.getZ() + ((double) size.getZ()) / 2
                ));
                tile.addInterface(location);
            }
        }
    }

    /**
     * Get the core block location.
     * @param material The chest material.
     * @param world The world.
     * @param blockPos The start position to search from.
     * @return The found location.
     */
    public static @Nullable BlockPos getCoreLocation(ChestMaterial material, IWorldReader world, BlockPos blockPos) {
        final Wrapper<BlockPos> tileLocationWrapper = new Wrapper<BlockPos>();
        material.getChestDetector().detect(world, blockPos, null, (location, blockState) -> {
            if (blockState.getBlock() instanceof ColossalChest) {
                tileLocationWrapper.set(location);
            }
            return null;
        }, false);
        return tileLocationWrapper.get();
    }

    /**
     * Show the structure forming error in the given player chat window.
     * @param material The chest material.
     * @param world The world.
     * @param blockPos The start position.
     * @param player The player.
     * @param hand The used hand.
     */
    public static void addPlayerChatError(ChestMaterial material, World world, BlockPos blockPos, PlayerEntity player, Hand hand) {
        if(!world.isRemote && player.getHeldItem(hand).isEmpty()) {
            DetectionResult result = material.getChestDetector().detect(world, blockPos, null,  new MaterialValidationAction(), false);
            if (result != null && result.getError() != null) {
                addPlayerChatError(player, result.getError());
            } else {
                player.sendMessage(new TranslationTextComponent("multiblock.colossalchests.error.unexpected"));
            }
        }
    }

    public static void addPlayerChatError(PlayerEntity player, L10NHelpers.UnlocalizedString unlocalizedError) {
        ITextComponent chat = new StringTextComponent("");
        ITextComponent prefix = new StringTextComponent("[")
                .appendSibling(new TranslationTextComponent("multiblock.colossalchests.error.prefix"))
                .appendSibling(new StringTextComponent("]: "))
                .setStyle(new Style().
                        setColor(TextFormatting.GRAY).
                        setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new TranslationTextComponent("multiblock.colossalchests.error.prefix.info")
                        ))
                );
        ITextComponent error = new StringTextComponent(unlocalizedError.localize());
        chat.appendSibling(prefix);
        chat.appendSibling(error);
        player.sendMessage(chat);
    }

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, World world, PlayerEntity player, BlockPos blockPos, Hand hand, BlockRayTraceResult rayTraceResult) {
        TileHelpers.getSafeTile(world, blockPos, TileColossalChest.class).ifPresent(tile -> packetBuffer.writeInt(tile.getInventory().getSizeInventory()));
    }

    @Override
    public boolean onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(!(blockState.get(ENABLED))) {
            ColossalChest.addPlayerChatError(material, world, blockPos, player, hand);
            return false;
        }
        return super.onBlockActivated(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public void onPlayerDestroy(IWorld world, BlockPos blockPos, BlockState blockState) {
        if(blockState.get(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.onPlayerDestroy(world, blockPos, blockState);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos blockPos, Explosion explosion) {
        if(world.getBlockState(blockPos).get(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.onExplosionDestroy(world, blockPos, explosion);
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

    @Override
    public boolean isValidPosition(BlockState blockState, IWorldReader world, BlockPos blockPos) {
        return super.isValidPosition(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

    private static class MaterialValidationAction implements CubeDetector.IValidationAction {
        private final Wrapper<ChestMaterial> requiredMaterial;

        public MaterialValidationAction() {
            this.requiredMaterial = new Wrapper<ChestMaterial>(null);
        }

        @Override
        public L10NHelpers.UnlocalizedString onValidate(BlockPos blockPos, BlockState blockState) {
            ChestMaterial material = null;
            if (blockState.getBlock() instanceof IBlockChestMaterial) {
                material = ((IBlockChestMaterial) blockState.getBlock()).getMaterial();
            }
            if(requiredMaterial.get() == null) {
                requiredMaterial.set(material);
                return null;
            }
            return requiredMaterial.get() == material ? null : new L10NHelpers.UnlocalizedString(
                    "multiblock.colossalchests.error.material", new L10NHelpers.UnlocalizedString(material.getUnlocalizedName()),
                    LocationHelpers.toCompactString(blockPos),
                    new L10NHelpers.UnlocalizedString(requiredMaterial.get().getUnlocalizedName()));
        }
    }
}
