package org.cyclops.colossalchests2;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.cyclops.colossalchests2.block.BlockChestCoreConfig;
import org.cyclops.colossalchests2.block.BlockChestWallConfig;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCoreConfigNeoForge;
import org.cyclops.colossalchests2.component.DataComponentChestContentsConfig;
import org.cyclops.colossalchests2.component.DataComponentChestSettingsConfig;
import org.cyclops.colossalchests2.inventory.ContainerChestConfig;
import org.cyclops.colossalchests2.config.ChestTablesReloadListener;
import org.cyclops.colossalchests2.modcompat.CommonCapabilitiesModCompat;
import org.cyclops.colossalchests2.proxy.ClientProxy;
import org.cyclops.colossalchests2.proxy.CommonProxy;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.modcompat.ModCompatLoader;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;

/**
 * The main mod class of this mod.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class ColossalChests extends ModBaseVersionable<ColossalChests> {

    /**
     * The unique instance of this mod.
     */
    public static ColossalChests _instance;

    public ColossalChests(IEventBus modEventBus) {
        super(Reference.MOD_ID, (instance) -> {
            ColossalChestsInstance.MOD = instance;
            _instance = instance;
        }, modEventBus);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new ChestTablesReloadListener()));
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        modCompatLoader.addModCompat(new CommonCapabilitiesModCompat());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
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
        configHandler.addConfigurable(new ContainerChestConfig<>(this));
        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new BlockChestWallConfig<>(this, material));
            configHandler.addConfigurable(new BlockChestCoreConfig<>(this, material));
        }
        configHandler.addConfigurable(new BlockEntityChestCoreConfigNeoForge<>(this));
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        clog(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        ColossalChests._instance.getLoggerHelper().log(level, message);
    }

}
