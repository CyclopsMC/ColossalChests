package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Drawer style clicks on Display walls: left-click takes, right-click inserts, a double right-click inserts all.
 * @author rubensworks
 */
public final class DisplayWallInteractions {

    private static final int HELD_CLICK_TICKS = 6;

    private static long lastAttackTick = -HELD_CLICK_TICKS - 1;

    private DisplayWallInteractions() {
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
     * The client asks the server to take items, once per click and not while the button is held.
     * @param player The player.
     * @param level The level.
     * @param pos The clicked position.
     * @return If mining must be cancelled.
     */
    public static boolean onAttack(Player player, Level level, BlockPos pos) {
        if (!isProtectedFromMining(level.getBlockState(pos), player)) {
            return false;
        }
        if (level.isClientSide) {
            long tick = level.getGameTime();
            // A held click repeats every tick, or every 6 ticks in creative. A new click comes after a longer gap.
            // Game time can also restart in another world.
            if (tick - lastAttackTick > HELD_CLICK_TICKS || tick < lastAttackTick) {
                ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundDisplayTakePacket(pos, player.isShiftKeyDown()));
            }
            lastAttackTick = tick;
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
        if (storage.isEmpty()) {
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
     * A right-click with an empty hand: sneaking clears the display, a double click inserts all of the shown type.
     * @return The result, or null to fall back to opening the chest.
     */
    @Nullable
    public static InteractionResult useWithoutItem(Player player, BlockEntityChestWall wall) {
        if (getStorage(wall).isEmpty()) {
            return null;
        }
        if (player.isSecondaryUseActive()) {
            if (!player.level().isClientSide) {
                wall.setDisplayed(ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        // Only the server knows about the previous click, the client lets it decide.
        if (!player.level().isClientSide && !wall.getDisplayed().isEmpty() && wall.recordInsertClick(player, player.level().getGameTime())) {
            insertAll(player, getStorage(wall).get(), wall.getDisplayed());
            wall.updateDisplayStats(false);
            return InteractionResult.CONSUME;
        }
        return null;
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
