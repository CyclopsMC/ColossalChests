package org.cyclops.colossalchests2;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.colossalchests2.advancement.ChestFormedTriggerConfig;
import org.cyclops.colossalchests2.advancement.MaterialChangedTriggerConfig;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.block.BlockChestCoreConfig;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWallConfig;
import org.cyclops.colossalchests2.block.BlockChestWallConfig;
import org.cyclops.colossalchests2.block.BlockUncolossalChestConfigForge;
import org.cyclops.colossalchests2.block.BuiltInMaterial;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCoreConfigForge;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWallConfigForge;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChestConfig;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChestForge;
import org.cyclops.colossalchests2.command.CommandBuildChest;
import org.cyclops.colossalchests2.component.DataComponentChestContentsConfig;
import org.cyclops.colossalchests2.component.DataComponentChestUpgradesConfig;
import org.cyclops.colossalchests2.component.DataComponentMaterialTargetConfig;
import org.cyclops.colossalchests2.config.ChestTablesReloadListener;
import org.cyclops.colossalchests2.gametest.GameTestAddon;
import org.cyclops.colossalchests2.inventory.ContainerChestConfig;
import org.cyclops.colossalchests2.inventory.ContainerDisplayConfig;
import org.cyclops.colossalchests2.inventory.ContainerInterfaceConfig;
import org.cyclops.colossalchests2.inventory.ContainerMagnetConfig;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeToolConfig;
import org.cyclops.colossalchests2.inventory.ContainerRedstoneConfig;
import org.cyclops.colossalchests2.material.ItemMaterialUpgradeToolConfig;
import org.cyclops.colossalchests2.network.ChestNetwork;
import org.cyclops.colossalchests2.proxy.ClientProxyForge;
import org.cyclops.colossalchests2.proxy.CommonProxyForge;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgradeConfig;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;

/**
 * The main mod class of ColossalChests.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class ColossalChestsForge extends ModBaseForge<ColossalChestsForge> {

    /**
     * The unique instance of this mod.
     */
    public static ColossalChestsForge _instance;

    public ColossalChestsForge() {
        super(Reference.MOD_ID, (instance) -> {
            _instance = instance;
            ColossalChestsInstance.MOD = instance;
        });
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new ChestTablesReloadListener()));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> event.getPlayers().forEach(ChestNetwork::sendTables));
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if (DisplayWallInteractions.onAttack(event.getEntity(), event.getLevel(), event.getPos(), event.getFace())) {
                event.setCanceled(true);
            }
        });
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyForge();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyForge();
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
        for (ChestUpgrade upgrade : ChestUpgrades.BUILT_IN) {
            configHandler.addConfigurable(new ItemChestUpgradeConfig<>(this, upgrade));
        }
        for (BuiltInMaterial material : BuiltInMaterial.values()) {
            configHandler.addConfigurable(new BlockChestWallConfig<>(this, material.getMaterial(), material::createProperties));
            configHandler.addConfigurable(new BlockChestCoreConfig<>(this, material.getMaterial(), material::createProperties));
        }
        if (GameTestAddon.isEnabled()) {
            GameTestAddon.register();
            configHandler.addConfigurable(new GameTestAddon.MaterialWallConfig<>(this));
            configHandler.addConfigurable(new GameTestAddon.MaterialCoreConfig<>(this));
            configHandler.addConfigurable(new GameTestAddon.UpgradeConfig<>(this));
            configHandler.addConfigurable(new GameTestAddon.WallConfig<>(this));
        }
        configHandler.addConfigurable(new ItemMaterialUpgradeToolConfig<>(this));
        for (WallType type : WallType.VALUES) {
            configHandler.addConfigurable(new BlockChestFunctionalWallConfig<>(this, type));
        }
        configHandler.addConfigurable(new BlockEntityChestCoreConfigForge<>(this));
        configHandler.addConfigurable(new BlockEntityChestWallConfigForge<>(this));
        configHandler.addConfigurable(new BlockUncolossalChestConfigForge<>(this));
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfig<>(this, BlockEntityUncolossalChestForge::new));
        configHandler.addConfigurable(new ContainerInterfaceConfig<>(this));
        configHandler.addConfigurable(new ContainerDisplayConfig<>(this));
        configHandler.addConfigurable(new ContainerRedstoneConfig<>(this));
        configHandler.addConfigurable(new ContainerMagnetConfig<>(this));
        configHandler.addConfigurable(new ContainerMaterialUpgradeToolConfig<>(this));
    }
}
