package org.cyclops.colossalchests.block;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.Advancements;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.cyclopscore.block.BlockWithEntityGui;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.DetectionResult;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A machine that can infuse stuff with blood.
 *
 * @author rubensworks
 */
public class ColossalChest extends BlockWithEntityGui implements CubeDetector.IDetectionListener, IBlockChestMaterial {

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private final ChestMaterial material;

    public ColossalChest(Properties properties, ChestMaterial material) {
        super(properties, BlockEntityColossalChest::new);
        this.material = material;

        material.setBlockCore(this);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ENABLED, false));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState p_49234_, Level p_49235_, BlockPos p_49236_) {
        return super.getMenuProvider(p_49234_, p_49235_, p_49236_);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_COLOSSAL_CHEST, BlockEntityColossalChest::lidAnimateTick) : null;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos pos, Random random) {
        if (level.getBlockEntity(pos) instanceof BlockEntityColossalChest uncolossalChest) {
            uncolossalChest.recheckOpen();
        }
    }

    @Override
    public String getDescriptionId() {
        String baseKey = super.getDescriptionId();
        return baseKey.substring(0, baseKey.lastIndexOf('_'));
    }

    @Override
    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    public static boolean canPlace(LevelReader world, BlockPos pos) {
        for(Direction side : Direction.values()) {
            BlockState blockState = world.getBlockState(pos.relative(side));
            Block block = blockState.getBlock();
            if((block instanceof ColossalChest || block instanceof ChestWall || block instanceof Interface)
                    && blockState.getProperties().contains(ENABLED) && blockState.getValue(ENABLED)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return blockState.getValue(ENABLED) ? RenderShape.ENTITYBLOCK_ANIMATED : super.getRenderShape(blockState);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockReader, BlockPos blockPos) {
        return blockState.getValue(ENABLED);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    public static DetectionResult triggerDetector(ChestMaterial material, LevelAccessor world, BlockPos blockPos, boolean valid, @Nullable Player player) {
        DetectionResult detectionResult = material.getChestDetector().detect(world, blockPos, valid ? null : blockPos, new MaterialValidationAction(), true);
        if (player instanceof ServerPlayer && detectionResult.getError() == null) {
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getValue(ENABLED)) {
                BlockEntityColossalChest tile = BlockEntityHelpers.get(world, blockPos, BlockEntityColossalChest.class).orElse(null);
                if (tile == null) {
                    BlockPos corePos = getCoreLocation(material, world, blockPos);
                    tile = BlockEntityHelpers.get(world, corePos, BlockEntityColossalChest.class).orElse(null);
                }

                Advancements.CHEST_FORMED.test((ServerPlayer) player, material, tile.getSizeSingular());
            }
        }
        return detectionResult;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (stack.hasCustomHoverName()) {
            BlockEntityColossalChest tile = BlockEntityHelpers.get(world, pos, BlockEntityColossalChest.class).orElse(null);
            if (tile != null) {
                tile.setCustomName(stack.getHoverName());
                tile.setSize(Vec3i.ZERO);
            }
        }
        triggerDetector(this.material, world, pos, true, placer instanceof Player ? (Player) placer : null);
    }

    @Override
    public void onPlace(BlockState blockStateNew, Level world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onPlace(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!world.captureBlockSnapshots && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.getValue(ENABLED)) {
            triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    @Override
    public void onDetect(LevelReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            ((LevelWriter) world).setBlock(location, world.getBlockState(location).setValue(ENABLED, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            BlockEntityColossalChest tile = BlockEntityHelpers.get(world, location, BlockEntityColossalChest.class).orElse(null);
            if(tile != null) {
                tile.setMaterial(this.material);
                tile.setSize(valid ? size : Vec3i.ZERO);
                tile.setCenter(new Vec3(
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
    public static @Nullable BlockPos getCoreLocation(ChestMaterial material, LevelReader world, BlockPos blockPos) {
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
    public static void addPlayerChatError(ChestMaterial material, Level world, BlockPos blockPos, Player player, InteractionHand hand) {
        if(!world.isClientSide && player.getItemInHand(hand).isEmpty()) {
            DetectionResult result = material.getChestDetector().detect(world, blockPos, null,  new MaterialValidationAction(), false);
            if (result != null && result.getError() != null) {
                addPlayerChatError(player, result.getError());
            } else {
                player.sendMessage(new TranslatableComponent("multiblock.colossalchests.error.unexpected"), Util.NIL_UUID);
            }
        }
    }

    public static void addPlayerChatError(Player player, Component error) {
        MutableComponent chat = new TextComponent("");
        Component prefix = new TextComponent("[")
                .append(new TranslatableComponent("multiblock.colossalchests.error.prefix"))
                .append(new TextComponent("]: "))
                .setStyle(Style.EMPTY.
                        withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)).
                        withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new TranslatableComponent("multiblock.colossalchests.error.prefix.info")
                        ))
                );
        chat.append(prefix);
        chat.append(error);
        player.sendMessage(chat, Util.NIL_UUID);
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, Level world, Player player, BlockPos blockPos, InteractionHand hand, BlockHitResult rayTraceResult) {
        BlockEntityHelpers.get(world, blockPos, BlockEntityColossalChest.class).ifPresent(tile -> packetBuffer.writeInt(tile.getInventory().getContainerSize()));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if(!(blockState.getValue(ENABLED))) {
            ColossalChest.addPlayerChatError(material, world, blockPos, player, hand);
            return InteractionResult.FAIL;
        }
        return super.use(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.destroy(world, blockPos, blockState);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if(world.getBlockState(pos).getValue(ENABLED)) ColossalChest.triggerDetector(material, world, pos, false, null);
        // IForgeBlock.super.onBlockExploded(state, world, pos, explosion);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        wasExploded(world, pos, explosion);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader world, BlockPos blockPos) {
        return super.canSurvive(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock().getClass() != newState.getBlock().getClass()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityColossalChest.class)
                    .ifPresent(tile -> {
                        // Last inventory overrides inventory when the chest is in a disabled state.
                        SimpleInventory lastInventory = tile.getLastValidInventory();
                        InventoryHelpers.dropItems(world, lastInventory != null ? lastInventory : tile.getInventory(), blockPos);
                    });
            super.onRemove(oldState, world, blockPos, newState, isMoving);
        }
    }

    private static class MaterialValidationAction implements CubeDetector.IValidationAction {
        private final Wrapper<ChestMaterial> requiredMaterial;

        public MaterialValidationAction() {
            this.requiredMaterial = new Wrapper<ChestMaterial>(null);
        }

        @Override
        public Component onValidate(BlockPos blockPos, BlockState blockState) {
            ChestMaterial material = null;
            if (blockState.getBlock() instanceof IBlockChestMaterial) {
                material = ((IBlockChestMaterial) blockState.getBlock()).getMaterial();
            }
            if(requiredMaterial.get() == null) {
                requiredMaterial.set(material);
                return null;
            }
            return requiredMaterial.get() == material ? null : new TranslatableComponent(
                    "multiblock.colossalchests.error.material", new TranslatableComponent(material.getUnlocalizedName()),
                    LocationHelpers.toCompactString(blockPos),
                    new TranslatableComponent(requiredMaterial.get().getUnlocalizedName()));
        }
    }
}
