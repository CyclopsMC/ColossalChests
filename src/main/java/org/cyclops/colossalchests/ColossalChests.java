package org.cyclops.colossalchests;

import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ChestWallConfig;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.InterfaceConfig;
import org.cyclops.colossalchests.block.UncolossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChestConfig;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.modcompat.CommonCapabilitiesModCompat;
import org.cyclops.colossalchests.modcompat.IronChestModCompat;
import org.cyclops.colossalchests.proxy.ClientProxy;
import org.cyclops.colossalchests.proxy.CommonProxy;
import org.cyclops.colossalchests.recipe.condition.RecipeConditionMetalVariantsSettingConfig;
import org.cyclops.colossalchests.tileentity.TileColossalChestConfig;
import org.cyclops.colossalchests.tileentity.TileInterfaceConfig;
import org.cyclops.colossalchests.tileentity.TileUncolossalChestConfig;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemGroupMod;
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

    public ColossalChests() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        modCompatLoader.addModCompat(new IronChestModCompat());
        modCompatLoader.addModCompat(new CommonCapabilitiesModCompat());
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        super.setup(event);
        Advancements.load();
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
    public ItemGroup constructDefaultItemGroup() {
        return new ItemGroupMod(this, () -> RegistryEntries.ITEM_CHEST);
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

        configHandler.addConfigurable(new TileColossalChestConfig());
        configHandler.addConfigurable(new TileInterfaceConfig());
        configHandler.addConfigurable(new TileUncolossalChestConfig());

        configHandler.addConfigurable(new ContainerColossalChestConfig());
        configHandler.addConfigurable(new ContainerUncolossalChestConfig());

        configHandler.addConfigurable(new RecipeConditionMetalVariantsSettingConfig());
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
