package org.cyclops.colossalchests2;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.IChest;
import org.cyclops.colossalchests2.api.IColossalChestsApi;
import org.cyclops.colossalchests2.api.MaterialProperties;
import org.cyclops.colossalchests2.api.UpgradeProperties;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.ChestInteractions;
import org.cyclops.colossalchests2.block.ChestMaterials;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the API, loaded by {@link org.cyclops.colossalchests2.api.ColossalChestsApi}.
 * @author rubensworks
 */
public class ColossalChestsApiImpl implements IColossalChestsApi {

    @Override
    public Block createWall(Block.Properties properties, ChestMaterial material) {
        return new BlockChestWall(properties, material);
    }

    @Override
    public Block createCore(Block.Properties properties, ChestMaterial material) {
        return new BlockChestCore(properties, material);
    }

    @Override
    public void registerUpgrade(ChestUpgrade upgrade) {
        ChestUpgrades.register(upgrade);
    }

    @Override
    public Item createUpgradeItem(Item.Properties properties, ChestUpgrade upgrade) {
        return new ItemChestUpgrade(properties, upgrade);
    }

    @Override
    public List<ChestUpgrade> getUpgrades() {
        return ChestUpgrades.getAll();
    }

    @Nullable
    @Override
    public ChestUpgrade getUpgrade(ResourceLocation id) {
        return ChestUpgrades.byId(id);
    }

    @Override
    public List<ChestMaterial> getMaterials() {
        return ChestMaterials.getAll();
    }

    @Override
    public Block getWallBlock(ChestMaterial material) {
        return BlockChestWall.getInstances().stream().filter(wall -> material.equals(wall.getMaterial())).findFirst()
                .map(Block.class::cast).orElse(Blocks.AIR);
    }

    @Override
    public Block getCoreBlock(ChestMaterial material) {
        return BlockChestCore.getInstances().stream().filter(core -> material.equals(core.getMaterial())).findFirst()
                .map(Block.class::cast).orElse(Blocks.AIR);
    }

    @Override
    public MaterialProperties getMaterialProperties(ResourceLocation material) {
        return ChestTablesLoader.get().getMaterial(material);
    }

    @Override
    public UpgradeProperties getUpgradeProperties(ResourceLocation upgrade) {
        return ChestTablesLoader.get().getUpgrade(upgrade);
    }

    @Override
    public int getMaxUpgradeCount(ResourceLocation upgrade, ResourceLocation material) {
        return ChestTablesLoader.get().getMaxUpgradeCount(upgrade, material);
    }

    @Override
    public Optional<IChest> getChest(Level level, BlockPos pos) {
        return ChestCoreIndex.findFormedCore(level, pos).map(IChest.class::cast);
    }

    @Override
    public void requestValidationNear(Level level, BlockPos pos) {
        ChestCoreIndex.requestValidationNear(level, pos);
    }

    @Override
    public InteractionResult useMember(BlockState state, Level level, BlockPos pos, Player player) {
        return ChestInteractions.use(state, level, pos, player);
    }

    @Override
    public ItemInteractionResult useItemOnMember(ItemStack stack, BlockState state) {
        return ChestInteractions.useItemOn(stack, state);
    }

}
