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
import org.cyclops.colossalchests.block.ChestWallConfigNeoForge;
import org.cyclops.colossalchests.block.ColossalChestConfigNeoForge;
import org.cyclops.colossalchests.block.InterfaceConfigNeoForge;
import org.cyclops.colossalchests.block.UncolossalChestConfigNeoForge;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChestConfigNeoForge;
import org.cyclops.colossalchests.blockentity.BlockEntityInterfaceConfigNeoForge;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChestConfigNeoForge;
import org.cyclops.colossalchests.condition.ConditionMetalVariantsSettingConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.modcompat.CommonCapabilitiesModCompat;
import org.cyclops.colossalchests.modcompat.IronChestModCompat;
import org.cyclops.colossalchests.proxy.ClientProxy;
import org.cyclops.colossalchests.proxy.CommonProxy;
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
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));

        for (ChestMaterial material : ChestMaterial.VALUES) {
            configHandler.addConfigurable(new ChestWallConfigNeoForge<>(this, material));
            configHandler.addConfigurable(new ColossalChestConfigNeoForge<>(this, material));
            configHandler.addConfigurable(new InterfaceConfigNeoForge<>(this, material));
        }
        configHandler.addConfigurable(new UncolossalChestConfigNeoForge<>(this));

        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, true));
        configHandler.addConfigurable(new ItemUpgradeToolConfig<>(this, false));

        configHandler.addConfigurable(new BlockEntityColossalChestConfigNeoForge<>(this));
        configHandler.addConfigurable(new BlockEntityInterfaceConfigNeoForge<>(this));
        configHandler.addConfigurable(new BlockEntityUncolossalChestConfigNeoForge<>(this));

        configHandler.addConfigurable(new ContainerColossalChestConfig<>(this));
        configHandler.addConfigurable(new ContainerUncolossalChestConfig<>(this));

        configHandler.addConfigurable(new ConditionMetalVariantsSettingConfig());

        configHandler.addConfigurable(new ChestFormedTriggerConfig<>(this));
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
