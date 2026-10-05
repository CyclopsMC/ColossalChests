package org.cyclops.colossalchests2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.block.BlockChestCoreConfig;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWallConfig;
import org.cyclops.colossalchests2.block.BlockChestWallConfig;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCoreConfigFabric;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWallConfigFabric;
import org.cyclops.colossalchests2.component.DataComponentChestContentsConfig;
import org.cyclops.colossalchests2.component.DataComponentChestSettingsConfig;
import org.cyclops.colossalchests2.component.DataComponentChestUpgradesConfig;
import org.cyclops.colossalchests2.config.ChestTablesReloadListenerFabric;
import org.cyclops.colossalchests2.inventory.ContainerChestConfig;
import org.cyclops.colossalchests2.inventory.ContainerDisplayConfig;
import org.cyclops.colossalchests2.inventory.ContainerInterfaceConfig;
import org.cyclops.colossalchests2.proxy.ClientProxyFabric;
import org.cyclops.colossalchests2.proxy.CommonProxyFabric;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgradeConfig;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;

/**
 * The main mod class of ColossalChests.
 * @author rubensworks
 */
public class ColossalChestsFabric extends ModBaseFabric<ColossalChestsFabric> implements ModInitializer {

    /**
     * The unique instance of this mod.
     */
    public static ColossalChestsFabric _instance;

    public ColossalChestsFabric() {
        super(Reference.MOD_ID, (instance) -> {
            ColossalChestsInstance.MOD = instance;
            _instance = instance;
        });
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ChestTablesReloadListenerFabric());
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                DisplayWallInteractions.onAttack(player, level, pos, direction) ? InteractionResult.FAIL : InteractionResult.PASS);
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyFabric();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyFabric();
    }

    @Override
    protected boolean hasDefaultCreativeModeTab() {
        return true;
    }

    @Override
    protected CreativeModeTab.Builder constructDefaultCreativeModeTab(CreativeModeTab.Builder builder) {
        return super.constructDefaultCreativeModeTab(builder)
                .icon(() -> new ItemStack(RegistryEntries.ITEM_CHEST));
    }

    @Override
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));

        configHandler.addConfigurable(new DataComponentChestContentsConfig<>(this));
        configHandler.addConfigurable(new DataComponentChestSettingsConfig<>(this));
        configHandler.addConfigurable(new DataComponentChestUpgradesConfig<>(this));
        configHandler.addConfigurable(new ContainerChestConfig<>(this));
        for (ChestUpgrade upgrade : ChestUpgrades.VALUES) {
            configHandler.addConfigurable(new ItemChestUpgradeConfig<>(this, upgrade));
        }
        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new BlockChestWallConfig<>(this, material));
            configHandler.addConfigurable(new BlockChestCoreConfig<>(this, material));
        }
        for (WallType type : WallType.VALUES) {
            configHandler.addConfigurable(new BlockChestFunctionalWallConfig<>(this, type));
        }
        configHandler.addConfigurable(new BlockEntityChestCoreConfigFabric<>(this));
        configHandler.addConfigurable(new BlockEntityChestWallConfigFabric<>(this));
        configHandler.addConfigurable(new ContainerInterfaceConfig<>(this));
        configHandler.addConfigurable(new ContainerDisplayConfig<>(this));
    }
}
