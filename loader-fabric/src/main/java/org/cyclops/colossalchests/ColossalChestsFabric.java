package org.cyclops.colossalchests;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests.advancement.criterion.ChestFormedTriggerConfig;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWallConfigFabric;
import org.cyclops.colossalchests.block.ColossalChestConfigFabric;
import org.cyclops.colossalchests.block.InterfaceConfigFabric;
import org.cyclops.colossalchests.block.UncolossalChestConfigFabric;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChestConfigFabric;
import org.cyclops.colossalchests.blockentity.BlockEntityInterfaceConfigFabric;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChestConfigFabric;
import org.cyclops.colossalchests.condition.ConditionMetalVariantsSettingConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.proxy.ClientProxyFabric;
import org.cyclops.colossalchests.proxy.CommonProxyFabric;
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

        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new ChestWallConfigFabric<>(this, material));
            configHandler.addConfigurable(new ColossalChestConfigFabric<>(this, material));
            configHandler.addConfigurable(new InterfaceConfigFabric<>(this, material));
        }
        configHandler.addConfigurable(new UncolossalChestConfigFabric<>(this));

        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, true));
        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, false));

        configHandler.addConfigurable(new BlockEntityColossalChestConfigFabric<>(this));
        configHandler.addConfigurable(new BlockEntityInterfaceConfigFabric<>(this));
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfigFabric<>(this));

        configHandler.addConfigurable(new ContainerColossalChestConfig<>(this));
        configHandler.addConfigurable(new ContainerUncolossalChestConfig<>(this));

        configHandler.addConfigurable(new ConditionMetalVariantsSettingConfig());

        configHandler.addConfigurable(new ChestFormedTriggerConfig<>(this));
    }
}
