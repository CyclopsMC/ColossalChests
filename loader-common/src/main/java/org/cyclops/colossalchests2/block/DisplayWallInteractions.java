package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.network.packet.ServerboundDisplayTakePacket;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Drawer style clicks on Display walls: left-click takes, right-click inserts, a double right-click inserts all.
 * An empty right-click opens the settings.
 * @author rubensworks
 */
public final class DisplayWallInteractions {

    private static boolean attackReleased = true;

    private DisplayWallInteractions() {
    }

    /**
     * Called on the client at the end of each tick.
     * @param attackDown If the attack button is held.
     */
    public static void onClientTick(boolean attackDown) {
        if (!attackDown) {
            attackReleased = true;
        }
    }

    public static boolean isDisplayWall(BlockState state) {
        return state.getBlock() instanceof BlockChestFunctionalWall wall && wall.getType() == WallType.DISPLAY;
    }

    /**
     * A Display wall is only mined with a tool that can harvest it, so taking items never breaks the chest.
     * @param state A block state.
     * @param player A player.
     * @return If a left-click by the player must not mine the block.
     */
    public static boolean isProtectedFromMining(BlockState state, Player player) {
        return isDisplayWall(state) && !player.getMainHandItem().isCorrectToolForDrops(state);
    }

    /**
     * Handle a left-click on a block, called on both sides by the loaders before mining starts.
     * The client asks the server to take items, once per click.
     * @param player The player.
     * @param level The level.
     * @param pos The clicked position.
     * @return If mining must be cancelled.
     */
    public static boolean onAttack(Player player, Level level, BlockPos pos) {
        if (!isProtectedFromMining(level.getBlockState(pos), player)) {
            return false;
        }
        // Holding the button repeats this, only a new click takes again.
        if (level.isClientSide && attackReleased) {
            attackReleased = false;
            ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundDisplayTakePacket(pos, player.isShiftKeyDown()));
        }
        return true;
    }

    private static Optional<ChestStorage> getStorage(BlockEntityChestWall wall) {
        return wall.getCore().map(BlockEntityChestCore::getStorage);
    }

    /**
     * Take the shown type into the player's inventory, dropping what does not fit.
     * @param player The player.
     * @param wall A Display wall.
     * @param single If one item is taken, otherwise a stack.
     * @return How many were taken.
     */
    public static long take(Player player, BlockEntityChestWall wall, boolean single) {
        ItemStack type = wall.getDisplayed();
        Optional<ChestStorage> storage = getStorage(wall);
        if (type.isEmpty() || storage.isEmpty()) {
            return 0;
        }
        long taken = storage.get().extract(type, single ? 1 : type.getMaxStackSize(), false);
        if (taken > 0) {
            ItemStack stack = type.copyWithCount((int) taken);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            wall.updateDisplayStats(false);
        }
        return taken;
    }

    /**
     * A right-click with an item: an empty display starts showing it, and the shown type is inserted.
     * @return The result, or null to fall back to the default wall behaviour.
     */
    @Nullable
    public static ItemInteractionResult useItemOn(ItemStack stack, Player player, BlockEntityChestWall wall) {
        Optional<ChestStorage> storage = getStorage(wall);
        // An empty hand is handled by useWithoutItem.
        if (stack.isEmpty() || storage.isEmpty()) {
            return null;
        }
        if (!player.level().isClientSide) {
            if (wall.getDisplayed().isEmpty()) {
                wall.setDisplayed(stack);
            }
            if (ItemStack.isSameItemSameComponents(stack, wall.getDisplayed())) {
                if (wall.recordInsertClick(player, player.level().getGameTime())) {
                    insertAll(player, storage.get(), wall.getDisplayed());
                } else {
                    stack.shrink((int) storage.get().insert(stack, stack.getCount(), false));
                }
                wall.updateDisplayStats(false);
            }
        }
        return ItemInteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * A right-click with an empty hand: right after an insert it inserts all of the shown type, otherwise it opens the
     * settings. Sneaking does nothing.
     */
    public static InteractionResult useWithoutItem(Player player, BlockEntityChestWall wall) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        // Only the server knows about the previous click, the client lets it decide.
        if (player instanceof ServerPlayer serverPlayer) {
            Optional<ChestStorage> storage = getStorage(wall);
            if (storage.isPresent() && !wall.getDisplayed().isEmpty() && wall.recordInsertClick(player, player.level().getGameTime())) {
                insertAll(player, storage.get(), wall.getDisplayed());
                wall.updateDisplayStats(false);
            } else {
                IModHelpers.get().getMinecraftHelpers().openMenu(serverPlayer, wall, buf -> buf.writeBlockPos(wall.getBlockPos()));
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * Insert every stack of the type from the player's inventory.
     * @return How many were inserted.
     */
    public static long insertAll(Player player, ChestStorage storage, ItemStack type) {
        long inserted = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(stack, type)) {
                long count = storage.insert(stack, stack.getCount(), false);
                stack.shrink((int) count);
                inserted += count;
            }
        }
        player.getInventory().setChanged();
        return inserted;
    }
}
