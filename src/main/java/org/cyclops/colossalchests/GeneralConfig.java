package org.cyclops.colossalchests;

import net.neoforged.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(category = "core", comment = "If the recipe loader should crash when finding invalid recipes.", requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static boolean crashOnInvalidRecipe = false;

    @ConfigurableProperty(category = "core", comment = "If mod compatibility loader should crash hard if errors occur in that process.", requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static boolean crashOnModCompatCrash = false;

    @ConfigurableProperty(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurableProperty(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    @ConfigurableProperty(category = "general", comment = "If items should be ejected from the chests if one of the structure blocks are removed.", configLocation = ModConfig.Type.SERVER)
    public static boolean ejectItemsOnDestroy = false;

    @ConfigurableProperty(category = "general", comment = "If the higher tier metal variants (including diamond and obsidian) can be crafted.", configLocation = ModConfig.Type.SERVER)
    public static boolean metalVariants = true;

    @ConfigurableProperty(category = "core", comment = "Maximum buffer byte size for adaptive inventory slots fragmentation.")
    public static int maxPacketBufferSize = 20000;

    @ConfigurableProperty(category = "general", comment = "If the interface input overlay should always be rendered on chests.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean alwaysShowInterfaceOverlay = true;

    @ConfigurableProperty(category = "general", comment = "Always create full creative-mode chests when formed. Should not be used in survival worlds!", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean creativeChests = false;

    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorWood = 1;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorCopper = 1.666;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorIron = 2;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorSilver = 2.666;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorGold = 3;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorDiamond = 4;
    @ConfigurableProperty(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static double chestInventoryMaterialFactorObsidian = 4;

    public GeneralConfig() {
        super(ColossalChests._instance, "general");
    }

    @Override
    public void onRegistered() {
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_INVALID_RECIPE, GeneralConfig.crashOnInvalidRecipe);
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_MODCOMPAT_CRASH, GeneralConfig.crashOnModCompatCrash);

        if(analytics) {
            Analytics.registerMod(getMod(), Reference.GA_TRACKING_ID);
        }
        if(versionChecker) {
            Versions.registerMod(getMod(), ColossalChests._instance, Reference.VERSION_URL);
        }
    }

}
