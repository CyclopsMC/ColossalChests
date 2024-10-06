package org.cyclops.colossalchests;

import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig<M extends IModBase> extends DummyConfigCommon<M> {

    @ConfigurablePropertyCommon(category = "general", comment = "If items should be ejected from the chests if one of the structure blocks are removed.", configLocation = ModConfigLocation.SERVER)
    public static boolean ejectItemsOnDestroy = false;

    @ConfigurablePropertyCommon(category = "general", comment = "If the higher tier metal variants (including diamond and obsidian) can be crafted.", configLocation = ModConfigLocation.SERVER)
    public static boolean metalVariants = true;

    @ConfigurablePropertyCommon(category = "core", comment = "Maximum buffer byte size for adaptive inventory slots fragmentation.")
    public static int maxPacketBufferSize = 20000;

    @ConfigurablePropertyCommon(category = "general", comment = "If the interface input overlay should always be rendered on chests.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean alwaysShowInterfaceOverlay = true;

    @ConfigurablePropertyCommon(category = "general", comment = "Always create full creative-mode chests when formed. Should not be used in survival worlds!", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean creativeChests = false;

    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorWood = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorCopper = 1.666;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorIron = 2;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorSilver = 2.666;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorGold = 3;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorDiamond = 4;
    @ConfigurablePropertyCommon(category = "general", comment = "Multiplier for the number of inventory slots for this chest material.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static double chestInventoryMaterialFactorObsidian = 4;

    public GeneralConfig(M mod) {
        super(mod, "general");
    }

}
