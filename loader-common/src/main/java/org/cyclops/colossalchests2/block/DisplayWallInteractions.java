package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.network.packet.ServerboundDisplayTakePacket;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Drawer style clicks on Display walls: left-click takes, right-click inserts, a double right-click inserts all.
 * An empty right-click opens the settings. Each face shows its own type, or acts like a plain wall when hidden.
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
     * @param level The level.
     * @param pos A position.
     * @param face A face of it, null if unknown.
     * @return The Display wall at the position, if the face is not hidden.
     */
    public static Optional<BlockEntityChestWall> getShownWall(Level level, BlockPos pos, @Nullable Direction face) {
        return isDisplayWall(level.getBlockState(pos)) && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall
                && (face == null || !wall.isFaceHidden(face)) ? Optional.of(wall) : Optional.empty();
    }

    /**
     * A Display wall is only mined with a tool that can harvest it, so taking items never breaks the chest.
     * Hidden faces act like a plain wall.
     * @param level The level.
     * @param pos A position.
     * @param face The clicked face, null if unknown.
     * @param player A player.
     * @return If a left-click by the player must not mine the block.
     */
    public static boolean isProtectedFromMining(Level level, BlockPos pos, @Nullable Direction face, Player player) {
        return getShownWall(level, pos, face).isPresent() && !player.getMainHandItem().isCorrectToolForDrops(level.getBlockState(pos));
    }

    /**
     * Handle a left-click on a block, called on both sides by the loaders before mining starts.
     * The client asks the server to take items, once per click.
     * @param player The player.
     * @param level The level.
     * @param pos The clicked position.
     * @param face The clicked face, null if unknown.
     * @return If mining must be cancelled.
     */
    public static boolean onAttack(Player player, Level level, BlockPos pos, @Nullable Direction face) {
        if (!isProtectedFromMining(level, pos, face, player)) {
            return false;
        }
        // Holding the button repeats this, only a new click takes again.
        if (level.isClientSide && attackReleased && face != null) {
            attackReleased = false;
            ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundDisplayTakePacket(pos, face, player.isShiftKeyDown()));
        }
        return true;
    }

    private static Optional<ChestStorage> getStorage(BlockEntityChestWall wall) {
        return wall.getCore().map(BlockEntityChestCore::getStorage);
    }

    /**
     * Take the type shown on a face into the player's held slot, then the rest of the inventory, dropping what does
     * not fit.
     * @param player The player.
     * @param wall A Display wall.
     * @param face The clicked face.
     * @param single If one item is taken, otherwise a stack.
     * @return How many were taken.
     */
    public static long take(Player player, BlockEntityChestWall wall, Direction face, boolean single) {
        ItemStack type = wall.getDisplayed(face);
        Optional<ChestStorage> storage = getStorage(wall);
        if (type.isEmpty() || storage.isEmpty()) {
            return 0;
        }
        long taken = storage.get().extract(type, single ? 1 : type.getMaxStackSize(), false);
        if (taken > 0) {
            ItemStack stack = type.copyWithCount((int) taken);
            // Fill the held slot first.
            Inventory inventory = player.getInventory();
            ItemStack held = inventory.getSelected();
            if (held.isEmpty()) {
                inventory.setItem(inventory.selected, stack.split(stack.getMaxStackSize()));
            } else if (ItemStack.isSameItemSameComponents(held, stack)) {
                held.grow(stack.split(Math.max(0, held.getMaxStackSize() - held.getCount())).getCount());
            }
            if (!stack.isEmpty() && !inventory.add(stack)) {
                player.drop(stack, false);
            }
            wall.updateDisplayStats(false);
        }
        return taken;
    }

    /**
     * A right-click with an item on a face: an empty face starts showing it, and the type it shows is inserted.
     * @return The result, or null to fall back to the default wall behaviour.
     */
    @Nullable
    public static ItemInteractionResult useItemOn(ItemStack stack, Player player, BlockEntityChestWall wall, Direction face) {
        Optional<ChestStorage> storage = getStorage(wall);
        // An empty hand is handled by useWithoutItem.
        if (stack.isEmpty() || storage.isEmpty() || wall.isFaceHidden(face)) {
            return null;
        }
        if (!player.level().isClientSide) {
            if (wall.getDisplayed(face).isEmpty()) {
                wall.setDisplayed(face, stack);
            }
            if (ItemStack.isSameItemSameComponents(stack, wall.getDisplayed(face))) {
                if (wall.recordInsertClick(player, face, player.level().getGameTime())) {
                    insertAll(player, storage.get(), wall.getDisplayed(face));
                } else {
                    stack.shrink((int) storage.get().insert(stack, stack.getCount(), false));
                }
                wall.updateDisplayStats(false);
            }
        }
        return ItemInteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * A right-click with an empty hand on a face: right after an insert there it inserts all of the type it shows,
     * otherwise it opens the settings. Sneaking does nothing, except on a hidden face, where it is the only way to the
     * settings.
     * @return The result, or null to fall back to the default wall behaviour.
     */
    @Nullable
    public static InteractionResult useWithoutItem(Player player, BlockEntityChestWall wall, Direction face) {
        if (wall.isFaceHidden(face)) {
            if (!player.isSecondaryUseActive()) {
                return null;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                openSettings(serverPlayer, wall);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        // Only the server knows about the previous click, the client lets it decide.
        if (player instanceof ServerPlayer serverPlayer) {
            Optional<ChestStorage> storage = getStorage(wall);
            if (storage.isPresent() && !wall.getDisplayed(face).isEmpty() && wall.recordInsertClick(player, face, player.level().getGameTime())) {
                insertAll(player, storage.get(), wall.getDisplayed(face));
                wall.updateDisplayStats(false);
            } else {
                openSettings(serverPlayer, wall);
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static void openSettings(ServerPlayer player, BlockEntityChestWall wall) {
        IModHelpers.get().getMinecraftHelpers().openMenu(player, wall, buf -> ContainerDisplay.writeOpenData(buf, wall));
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
