package org.cyclops.colossalchests2.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * What Colossal Chests offers to addons, from {@link ColossalChestsApi#get()}.
 * @author rubensworks
 */
public interface IColossalChestsApi {

    /**
     * @return A new plain wall of the material, for the caller to register.
     */
    Block createWall(Block.Properties properties, ChestMaterial material);

    /**
     * @return A new core of the material, for the caller to register. Registering it makes the material exist.
     */
    Block createCore(Block.Properties properties, ChestMaterial material);

    /**
     * Register an upgrade, which must happen while mods are constructed.
     */
    void registerUpgrade(ChestUpgrade upgrade);

    /**
     * @return A new item that installs the upgrade, for the caller to register as [namespace]:upgrade_[name].
     */
    Item createUpgradeItem(Item.Properties properties, ChestUpgrade upgrade);

    /**
     * @return All upgrades, this mod's first.
     */
    List<ChestUpgrade> getUpgrades();

    @Nullable
    ChestUpgrade getUpgrade(ResourceLocation id);

    /**
     * @return All materials that have a core block, in the order of their data files.
     */
    List<ChestMaterial> getMaterials();

    /**
     * @return The plain wall of the material, or air.
     */
    Block getWallBlock(ChestMaterial material);

    /**
     * @return The core of the material, or air.
     */
    Block getCoreBlock(ChestMaterial material);

    /**
     * @return The loaded properties of a material, or the defaults.
     */
    MaterialProperties getMaterialProperties(ResourceLocation material);

    /**
     * @return The loaded properties of an upgrade, or disabled.
     */
    UpgradeProperties getUpgradeProperties(ResourceLocation upgrade);

    /**
     * @return How many of the upgrade a chest of the material takes.
     */
    int getMaxUpgradeCount(ResourceLocation upgrade, ResourceLocation material);

    /**
     * @return The formed chest whose shell contains the position.
     */
    Optional<IChest> getChest(Level level, BlockPos pos);

    /**
     * Make cores near a position check their structure again, after a member changed there.
     */
    void requestValidationNear(Level level, BlockPos pos);

    /**
     * Right-click on a member: opens the formed chest, or explains why it does not form.
     */
    InteractionResult useMember(BlockState state, Level level, BlockPos pos, Player player);

    /**
     * Right-click on a member with an item: lets the item be used while not formed, and opens the chest otherwise.
     */
    ItemInteractionResult useItemOnMember(ItemStack stack, BlockState state);

}
