package org.cyclops.colossalchests2;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.colossalchests2.proxy.ClientProxyForge;
import org.cyclops.colossalchests2.proxy.CommonProxyForge;
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
    }
}
