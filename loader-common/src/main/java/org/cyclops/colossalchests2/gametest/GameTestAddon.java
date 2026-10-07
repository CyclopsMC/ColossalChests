package org.cyclops.colossalchests2.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.ColossalChestsApi;
import org.cyclops.colossalchests2.api.IChest;
import org.cyclops.colossalchests2.api.block.ChestMemberBlock;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Content registered like an addon would, only on game test servers, to test the extension points.
 * It only uses the api package.
 * @author rubensworks
 */
public final class GameTestAddon {

    /**
     * Defined by data/colossalchests2test/colossalchests2/material/test_addon.json: after gold, with its depth limit set
     * by the material instead of the upgrade. That file does nothing outside game tests, as the blocks are missing.
     * Its namespace differs from its blocks', like a material of another mod.
     */
    public static final ChestMaterial MATERIAL = new ChestMaterial(ResourceLocation.fromNamespaceAndPath(ColossalChestsApi.MOD_ID + "test", "test_addon"));

    /**
     * Refuses dirt, and adds a cobblestone every second. Defined by data/colossalchests2/colossalchests2/upgrade/test_addon.json,
     * which does nothing outside game tests, as the upgrade is missing.
     */
    public static final ChestUpgrade UPGRADE = new ChestUpgrade(ResourceLocation.fromNamespaceAndPath(ColossalChestsApi.MOD_ID, "test_addon")) {
        @Override
        public boolean canInsert(IChest chest, ItemStack type, int count) {
            return !type.is(Items.DIRT);
        }

        @Override
        public void tick(IChest chest, int count) {
            if (chest.getLevel().getGameTime() % 20 == 0) {
                chest.insert(new ItemStack(Items.COBBLESTONE), count, false);
            }
        }
    };

    /**
     * The id of the wall, registered by {@link WallConfig}.
     */
    public static final ResourceLocation WALL = ResourceLocation.fromNamespaceAndPath(ColossalChestsApi.MOD_ID, "test_addon_wall");

    private GameTestAddon() {
    }

    /**
     * @return If this is a game test server of any loader.
     */
    public static boolean isEnabled() {
        return Boolean.getBoolean("neoforge.gameTestServer") || Boolean.getBoolean("forge.gameTestServer")
                || System.getProperty("fabric-api.gametest") != null;
    }

    /**
     * Register the addon upgrade, after which the caller registers the configs.
     */
    public static void register() {
        ColossalChestsApi.get().registerUpgrade(UPGRADE);
    }

    /**
     * @return New properties for a wall or core of the test material.
     */
    public static Block.Properties createProperties() {
        return Block.Properties.of().strength(2.5F, 6.0F).sound(SoundType.AMETHYST);
    }

    /**
     * Config for the plain wall of the {@link #MATERIAL}.
     */
    public static class MaterialWallConfig<M extends IModBase> extends BlockConfigCommon<M> {
        public MaterialWallConfig(M mod) {
            super(mod, "chest_wall_" + MATERIAL.getName(), eConfig -> ColossalChestsApi.get().createWall(createProperties(), MATERIAL),
                    getDefaultItemConstructor(mod));
        }
    }

    /**
     * Config for the core of the {@link #MATERIAL}.
     */
    public static class MaterialCoreConfig<M extends IModBase> extends BlockConfigCommon<M> {
        public MaterialCoreConfig(M mod) {
            super(mod, "chest_core_" + MATERIAL.getName(), eConfig -> ColossalChestsApi.get().createCore(createProperties(), MATERIAL),
                    getDefaultItemConstructor(mod));
        }
    }

    /**
     * Config for the item of the {@link #UPGRADE}.
     */
    public static class UpgradeConfig<M extends IModBase> extends ItemConfigCommon<M> {
        public UpgradeConfig(M mod) {
            super(mod, "upgrade_" + UPGRADE.getId().getPath(),
                    eConfig -> ColossalChestsApi.get().createUpgradeItem(new Item.Properties().stacksTo(16), UPGRADE));
        }
    }

    /**
     * A functional wall that counts how often the contents of its chest changed.
     */
    public static class Wall extends ChestMemberBlock {

        private final AtomicInteger contentsChanges = new AtomicInteger();

        public Wall(Properties properties) {
            super(properties);
        }

        public int getContentsChanges() {
            return contentsChanges.get();
        }

        @Override
        public void onChestContentsChanged(BlockState state, Level level, BlockPos pos, IChest chest) {
            contentsChanges.incrementAndGet();
        }
    }

    /**
     * Config for the {@link Wall}.
     */
    public static class WallConfig<M extends IModBase> extends BlockConfigCommon<M> {
        public WallConfig(M mod) {
            super(mod, WALL.getPath(), eConfig -> new Wall(Block.Properties.of().strength(2.5F)), getDefaultItemConstructor(mod));
        }
    }

}
