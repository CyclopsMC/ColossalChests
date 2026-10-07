package org.cyclops.colossalchests2;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.advancement.ChestFormedTriggerConfig;
import org.cyclops.colossalchests2.advancement.MaterialChangedTriggerConfig;
import org.cyclops.colossalchests2.block.BlockChestCoreConfig;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWallConfig;
import org.cyclops.colossalchests2.block.BlockChestWallConfig;
import org.cyclops.colossalchests2.block.BlockUncolossalChestConfigFabric;
import org.cyclops.colossalchests2.block.BuiltInMaterial;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCoreConfigFabric;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWallConfigFabric;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChestConfigFabric;
import org.cyclops.colossalchests2.command.CommandBuildChest;
import org.cyclops.colossalchests2.component.DataComponentChestContentsConfig;
import org.cyclops.colossalchests2.component.DataComponentChestUpgradesConfig;
import org.cyclops.colossalchests2.component.DataComponentMaterialTargetConfig;
import org.cyclops.colossalchests2.config.ChestTablesReloadListenerFabric;
import org.cyclops.colossalchests2.gametest.GameTestAddon;
import org.cyclops.colossalchests2.inventory.ContainerChestConfig;
import org.cyclops.colossalchests2.inventory.ContainerDisplayConfig;
import org.cyclops.colossalchests2.inventory.ContainerInterfaceConfig;
import org.cyclops.colossalchests2.inventory.ContainerMagnetConfig;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeToolConfig;
import org.cyclops.colossalchests2.inventory.ContainerRedstoneConfig;
import org.cyclops.colossalchests2.material.ItemMaterialUpgradeToolConfig;
import org.cyclops.colossalchests2.network.ChestNetwork;
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
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> ChestNetwork.sendTables(player));
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
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand(Commands.CommandSelection selection, CommandBuildContext context) {
        return super.constructBaseCommand(selection, context)
                .then(CommandBuildChest.make());
    }

    @Override
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));
        configHandler.addConfigurable(new ChestFormedTriggerConfig<>(this));
        configHandler.addConfigurable(new MaterialChangedTriggerConfig<>(this));

        configHandler.addConfigurable(new DataComponentChestContentsConfig<>(this));
        configHandler.addConfigurable(new DataComponentChestUpgradesConfig<>(this));
        configHandler.addConfigurable(new DataComponentMaterialTargetConfig<>(this));
        configHandler.addConfigurable(new ContainerChestConfig<>(this));
        for (ChestUpgrade upgrade : ChestUpgrades.VALUES) {
            configHandler.addConfigurable(new ItemChestUpgradeConfig<>(this, upgrade));
        }
        for (BuiltInMaterial material : BuiltInMaterial.values()) {
            configHandler.addConfigurable(new BlockChestWallConfig<>(this, material.getMaterial(), material::createProperties));
            configHandler.addConfigurable(new BlockChestCoreConfig<>(this, material.getMaterial(), material::createProperties));
        }
        if (GameTestAddon.isEnabled()) {
            configHandler.addConfigurable(new BlockChestWallConfig<>(this, GameTestAddon.MATERIAL, GameTestAddon::createProperties));
            configHandler.addConfigurable(new BlockChestCoreConfig<>(this, GameTestAddon.MATERIAL, GameTestAddon::createProperties));
        }
        configHandler.addConfigurable(new ItemMaterialUpgradeToolConfig<>(this));
        for (WallType type : WallType.VALUES) {
            configHandler.addConfigurable(new BlockChestFunctionalWallConfig<>(this, type));
        }
        configHandler.addConfigurable(new BlockEntityChestCoreConfigFabric<>(this));
        configHandler.addConfigurable(new BlockEntityChestWallConfigFabric<>(this));
        configHandler.addConfigurable(new BlockUncolossalChestConfigFabric<>(this));
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfigFabric<>(this));
        configHandler.addConfigurable(new ContainerInterfaceConfig<>(this));
        configHandler.addConfigurable(new ContainerDisplayConfig<>(this));
        configHandler.addConfigurable(new ContainerRedstoneConfig<>(this));
        configHandler.addConfigurable(new ContainerMagnetConfig<>(this));
        configHandler.addConfigurable(new ContainerMaterialUpgradeToolConfig<>(this));
    }
}
