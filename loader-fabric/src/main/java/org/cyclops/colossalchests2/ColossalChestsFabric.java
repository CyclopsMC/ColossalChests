package org.cyclops.colossalchests2;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.proxy.ClientProxyFabric;
import org.cyclops.colossalchests2.proxy.CommonProxyFabric;
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
    }
}
