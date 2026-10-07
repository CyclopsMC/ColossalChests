package org.cyclops.colossalchests2.material;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.multiblock.ChestStructure;

import java.util.List;

/**
 * Changes the material of a formed chest in place: the core and all plain walls, functional walls stay.
 * Contents, upgrades and settings move to the new core.
 * @author rubensworks
 */
public final class MaterialChanges {

    private MaterialChanges() {
    }

    /**
     * Change a chest to another material, like breaking and replacing its core and plain walls by hand:
     * one wall of the new material is taken from the player per block, the core included, and as many walls of
     * the old material are given back, dropping what does not fit. Players with infinite materials, such as in
     * creative mode, neither pay nor get walls back.
     * @param player The player.
     * @param core A formed core.
     * @param target The new material.
     * @return The message for the player, and if the change happened.
     */
    public static Result change(Player player, BlockEntityChestCore core, ChestMaterial target) {
        ChestStructure structure = core.getStructure();
        if (structure == null || !(core.getBlockState().getBlock() instanceof BlockChestCore coreBlock)) {
            return Result.fail(message("not_formed"));
        }
        ChestMaterial current = coreBlock.getMaterial();
        if (current.equals(target)) {
            return Result.fail(message("same", target.getDisplayName()));
        }
        MaterialChangeRules.Problem problem = MaterialChangeRules.check(structure.size(), core.getUpgradeSet(), target);
        switch (problem) {
            case TOO_LARGE -> {
                return Result.fail(message("too_large", target.getDisplayName(), target.getProperties().maxSize()));
            }
            case UPGRADE_SLOTS -> {
                return Result.fail(message("upgrade_slots", target.getDisplayName(), target.getProperties().upgradeSlots()));
            }
            case UPGRADE_LIMIT -> {
                return Result.fail(message("upgrade_limit", target.getDisplayName()));
            }
            case NONE -> {
            }
        }

        Level level = core.getLevel();
        List<BlockPos> walls = Lists.newArrayList();
        for (BlockPos pos : structure.shell()) {
            if (level.getBlockState(pos).getBlock() instanceof BlockChestWall wall && wall.isPlain()) {
                walls.add(pos);
            }
        }
        int blocks = walls.size() + 1;
        Item newWall = target.getWallBlock().asItem();
        if (!player.hasInfiniteMaterials()) {
            int available = count(player.getInventory(), newWall);
            if (available < blocks) {
                return Result.fail(message("missing", blocks, newWall.getDescription(), blocks - available));
            }
            consume(player.getInventory(), newWall, blocks);
            give(player, current.getWallBlock().asItem(), blocks);
        }
        swap(level, core, walls, target);
        if (player instanceof ServerPlayer serverPlayer) {
            RegistryEntries.TRIGGER_MATERIAL_CHANGED.value().trigger(serverPlayer, current, target);
            RegistryEntries.TRIGGER_CHEST_FORMED.value().trigger(serverPlayer, target, structure.size());
        }
        return Result.success(message("changed", target.getDisplayName()));
    }

    private static Component message(String key, Object... args) {
        return Component.translatable("chest.colossalchests2.material_change." + key, args);
    }

    /**
     * Only plain stacks count, so named or enchanted items are never taken.
     */
    private static boolean matches(ItemStack stack, Item item) {
        return stack.is(item) && stack.getComponentsPatch().isEmpty();
    }

    /**
     * @param inventory A player inventory.
     * @param item An item.
     * @return How many plain stacks of the item the inventory holds.
     */
    public static int count(Inventory inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (matches(stack, item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consume(Inventory inventory, Item item, int count) {
        int remaining = count;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (matches(stack, item)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
        inventory.setChanged();
    }

    /**
     * Give items to the player, dropping what does not fit at their feet.
     */
    private static void give(Player player, Item item, int count) {
        for (int remaining = count; remaining > 0; remaining -= item.getDefaultMaxStackSize()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(item, Math.min(remaining, item.getDefaultMaxStackSize())));
        }
    }

    /**
     * Replace the core and walls, and move the core's data to the new core.
     */
    private static void swap(Level level, BlockEntityChestCore core, List<BlockPos> walls, ChestMaterial target) {
        List<ServerPlayer> viewers = Lists.newArrayList();
        for (ServerPlayer viewer : List.copyOf(core.getViewers())) {
            if (viewer.containerMenu instanceof ContainerChest menu && menu.isFor(core)) {
                viewers.add(viewer);
                viewer.closeContainer();
            }
        }
        core.compactUpgrades();
        CompoundTag data = core.saveCustomOnly(level.registryAccess());
        BlockPos corePos = core.getBlockPos();
        for (BlockPos pos : walls) {
            level.setBlock(pos, withFormed(target.getWallBlock(), level.getBlockState(pos)), Block.UPDATE_ALL);
        }
        level.setBlock(corePos, withFormed(target.getCoreBlock(), level.getBlockState(corePos)), Block.UPDATE_ALL);
        if (level.getBlockEntity(corePos) instanceof BlockEntityChestCore newCore) {
            newCore.loadCustomOnly(data, level.registryAccess());
            newCore.setChanged();
            newCore.validateNow();
            for (ServerPlayer viewer : viewers) {
                newCore.openMenu(viewer);
            }
        }
    }

    private static BlockState withFormed(Block block, BlockState old) {
        return block.defaultBlockState().setValue(BlockChestCore.FORMED, old.getValue(BlockChestCore.FORMED));
    }

    /**
     * @param message The message for the player.
     * @param success If the material changed.
     */
    public record Result(Component message, boolean success) {
        public static Result success(Component message) {
            return new Result(message, true);
        }

        public static Result fail(Component message) {
            return new Result(message, false);
        }
    }

}
