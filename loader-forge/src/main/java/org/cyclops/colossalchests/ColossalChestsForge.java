package org.cyclops.colossalchests;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.colossalchests.advancement.criterion.ChestFormedTriggerConfig;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWallConfigForge;
import org.cyclops.colossalchests.block.ColossalChestConfigForge;
import org.cyclops.colossalchests.block.InterfaceConfigForge;
import org.cyclops.colossalchests.block.UncolossalChestConfigForge;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChestConfigForge;
import org.cyclops.colossalchests.blockentity.BlockEntityInterfaceConfigForge;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChestConfigForge;
import org.cyclops.colossalchests.condition.ConditionMetalVariantsSettingConfigForge;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.proxy.ClientProxyForge;
import org.cyclops.colossalchests.proxy.CommonProxyForge;
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
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));

        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new ChestWallConfigForge<>(this, material));
            configHandler.addConfigurable(new ColossalChestConfigForge<>(this, material));
            configHandler.addConfigurable(new InterfaceConfigForge<>(this, material));
        }
        configHandler.addConfigurable(new UncolossalChestConfigForge<>(this));

        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, true));
        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, false));

        configHandler.addConfigurable(new BlockEntityColossalChestConfigForge<>(this));
        configHandler.addConfigurable(new BlockEntityInterfaceConfigForge<>(this));
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfigForge<>(this));

        configHandler.addConfigurable(new ContainerColossalChestConfig<>(this));
        configHandler.addConfigurable(new ContainerUncolossalChestConfig<>(this));

        configHandler.addConfigurable(new ConditionMetalVariantsSettingConfigForge());

        configHandler.addConfigurable(new ChestFormedTriggerConfig<>(this));
    }
}
