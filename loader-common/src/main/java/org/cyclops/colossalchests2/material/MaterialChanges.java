package org.cyclops.colossalchests2.material;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.config.MaterialCost;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.multiblock.ChestStructure;

import java.util.List;
import java.util.Optional;

/**
 * Changes the material of a formed chest in place: the core and all plain walls, functional walls stay.
 * Contents, upgrades and settings move to the new core.
 * @author rubensworks
 */
public final class MaterialChanges {

    private MaterialChanges() {
    }

    /**
     * Turn a chest into the next material, paid from the player's inventory, or back into the previous one,
     * which refunds that cost. Players with infinite materials, such as in creative mode, neither pay nor get refunds.
     * @param player The player.
     * @param core A formed core.
     * @param upgrade If upgrading, otherwise downgrading.
     * @return The message for the player, and if the change happened.
     */
    public static Result change(Player player, BlockEntityChestCore core, boolean upgrade) {
        ChestStructure structure = core.getStructure();
        if (structure == null || !(core.getBlockState().getBlock() instanceof BlockChestCore coreBlock)) {
            return Result.fail(message("not_formed"));
        }
        ChestMaterial current = coreBlock.getMaterial();
        Optional<ChestMaterial> targetOptional = upgrade ? current.next() : current.previous();
        if (targetOptional.isEmpty()) {
            return Result.fail(message(upgrade ? "highest" : "lowest", current.getDisplayName()));
        }
        ChestMaterial target = targetOptional.get();
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
        List<MaterialCost> cost = MaterialChangeRules.getTotalCost(
                (upgrade ? target : current).getProperties().upgradeCost(), walls.size() + 1);
        if (!player.hasInfiniteMaterials()) {
            if (upgrade) {
                List<MaterialCost> missing = getMissing(player.getInventory(), cost);
                if (!missing.isEmpty()) {
                    return Result.fail(message("missing", target.getDisplayName(), describe(missing)));
                }
                consume(player.getInventory(), cost);
            } else {
                for (MaterialCost entry : cost) {
                    give(player, entry);
                }
            }
        }
        swap(level, core, walls, target);
        return Result.success(message(upgrade || player.hasInfiniteMaterials() ? "upgraded" : "downgraded", target.getDisplayName()));
    }

    private static Component message(String key, Object... args) {
        return Component.translatable("chest.colossalchests2.material_change." + key, args);
    }

    public static Optional<Item> getItem(MaterialCost cost) {
        return BuiltInRegistries.ITEM.getOptional(cost.item());
    }

    /**
     * Only plain stacks count, so named or enchanted items are never taken.
     */
    private static boolean matches(ItemStack stack, Item item) {
        return stack.is(item) && stack.getComponentsPatch().isEmpty();
    }

    /**
     * @param inventory A player inventory.
     * @param cost A total cost.
     * @return What the inventory lacks of the cost. Items that do not exist are not required.
     */
    public static List<MaterialCost> getMissing(Inventory inventory, List<MaterialCost> cost) {
        List<MaterialCost> missing = Lists.newArrayList();
        for (MaterialCost entry : cost) {
            getItem(entry).ifPresent(item -> {
                int available = 0;
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    ItemStack stack = inventory.getItem(slot);
                    if (matches(stack, item)) {
                        available += stack.getCount();
                    }
                }
                if (available < entry.count()) {
                    missing.add(new MaterialCost(entry.item(), entry.count() - available));
                }
            });
        }
        return missing;
    }

    private static void consume(Inventory inventory, List<MaterialCost> cost) {
        for (MaterialCost entry : cost) {
            getItem(entry).ifPresent(item -> {
                int remaining = entry.count();
                for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
                    ItemStack stack = inventory.getItem(slot);
                    if (matches(stack, item)) {
                        int taken = Math.min(remaining, stack.getCount());
                        stack.shrink(taken);
                        remaining -= taken;
                    }
                }
            });
        }
        inventory.setChanged();
    }

    /**
     * Give items to the player, dropping what does not fit at their feet.
     */
    private static void give(Player player, MaterialCost cost) {
        getItem(cost).ifPresent(item -> {
            int remaining = cost.count();
            while (remaining > 0) {
                int count = Math.min(remaining, item.getDefaultMaxStackSize());
                player.getInventory().placeItemBackInInventory(new ItemStack(item, count));
                remaining -= count;
            }
        });
    }

    private static Component describe(List<MaterialCost> costs) {
        Component list = Component.empty();
        for (int i = 0; i < costs.size(); i++) {
            MaterialCost cost = costs.get(i);
            Component name = getItem(cost).map(Item::getDescription).orElse(Component.literal(cost.item().toString()));
            list = list.copy().append(i == 0 ? Component.empty() : Component.literal(", "))
                    .append(Component.translatable("chest.colossalchests2.material_change.cost", cost.count(), name));
        }
        return list;
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
