package org.cyclops.colossalchests;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.cyclops.colossalchests.advancement.criterion.ChestFormedTriggerConfig;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWallConfig;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.InterfaceConfig;
import org.cyclops.colossalchests.block.UncolossalChestConfig;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChestConfig;
import org.cyclops.colossalchests.blockentity.BlockEntityInterfaceConfig;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChestConfig;
import org.cyclops.colossalchests.condition.ConditionMetalVariantsSettingConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.modcompat.CommonCapabilitiesModCompat;
import org.cyclops.colossalchests.modcompat.IronChestModCompat;
import org.cyclops.colossalchests.proxy.ClientProxy;
import org.cyclops.colossalchests.proxy.CommonProxy;
import org.cyclops.cyclopscore.config.ConfigHandler;
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
        super(Reference.MOD_ID, (instance) -> _instance = instance, modEventBus);
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        modCompatLoader.addModCompat(new IronChestModCompat());
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
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());

        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new ChestWallConfig(material));
            configHandler.addConfigurable(new ColossalChestConfig(material));
            configHandler.addConfigurable(new InterfaceConfig(material));
        }

        configHandler.addConfigurable(new UncolossalChestConfig());
        configHandler.addConfigurable(new ItemUpgradeToolConfig(true));
        configHandler.addConfigurable(new ItemUpgradeToolConfig(false));

        configHandler.addConfigurable(new BlockEntityColossalChestConfig());
        configHandler.addConfigurable(new BlockEntityInterfaceConfig());
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfig());

        configHandler.addConfigurable(new ContainerColossalChestConfig());
        configHandler.addConfigurable(new ContainerUncolossalChestConfig());

        configHandler.addConfigurable(new ConditionMetalVariantsSettingConfig());

        configHandler.addConfigurable(new ChestFormedTriggerConfig());
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
