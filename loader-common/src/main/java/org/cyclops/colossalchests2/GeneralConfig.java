package org.cyclops.colossalchests2;

import org.cyclops.colossalchests2.api.MaterialProperties;
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

    public static final int MIN_SIZE = MaterialProperties.MIN_SIZE;
    public static final int HARD_MAX_SIZE = MaterialProperties.MAX_SIZE;
    public static final int HARD_MAX_SLOTS = 108;
    public static final int HARD_MAX_MAGNET_RADIUS = 128;

    @ConfigurablePropertyCommon(category = "chest", comment = "The number of slots of a chest without upgrades.", minimalValue = 1, maximalValue = HARD_MAX_SLOTS, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int baseSlots = 27;
    @ConfigurablePropertyCommon(category = "chest", comment = "The maximum number of slots of a chest, including upgrades.", minimalValue = 1, maximalValue = HARD_MAX_SLOTS, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int maxSlots = HARD_MAX_SLOTS;

    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 2.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize2 = 4;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 3.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize3 = 16;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 4.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize4 = 64;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 5.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize5 = 256;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 6.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize6 = 1024;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 7.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize7 = 4096;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 8.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize8 = 16384;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 9.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize9 = 65536;
    @ConfigurablePropertyCommon(category = "chest", comment = "Stacks per slot for a chest with an outer size of 10.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int depthSize10 = 262144;

    @ConfigurablePropertyCommon(category = "chest", comment = "The maximum number of items in a single slot, for any item type.", minimalValue = 1, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int maxItemsPerSlot = Integer.MAX_VALUE;
    @ConfigurablePropertyCommon(category = "chest", comment = "If items that do not stack, such as tools, can be stored.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean acceptNonStackables = true;

    @ConfigurablePropertyCommon(category = "magnet", comment = "If Magnet walls can pull dropped items.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean magnetEnabled = true;
    @ConfigurablePropertyCommon(category = "magnet", comment = "Default range value for Magnet walls. Players can change it per wall, up to magnetMaxRadius.", minimalValue = 1, maximalValue = HARD_MAX_MAGNET_RADIUS, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int magnetRadius = 8;
    @ConfigurablePropertyCommon(category = "magnet", comment = "The largest radius players can set on a Magnet wall, in blocks.", minimalValue = 1, maximalValue = HARD_MAX_MAGNET_RADIUS, isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int magnetMaxRadius = 32;

    @ConfigurablePropertyCommon(category = "display", comment = "If Display walls show blocks and other 3D items like an item frame, front-on, instead of like an inventory icon.", configLocation = ModConfigLocation.CLIENT)
    public static boolean displayItemFrameStyle = false;

    public GeneralConfig(M mod) {
        super(mod, "general");
    }

    // Config values are not range-checked when loaded, so getters clamp them.

    /**
     * @return The number of slots without upgrades, within [1, {@link #getMaxSlots()}].
     */
    public static int getBaseSlots() {
        return Math.clamp(baseSlots, 1, getMaxSlots());
    }

    /**
     * @return The maximum number of slots, within [1, {@link #HARD_MAX_SLOTS}].
     */
    public static int getMaxSlots() {
        return Math.clamp(maxSlots, 1, HARD_MAX_SLOTS);
    }

    /**
     * @return The largest radius of Magnet walls, within [1, {@link #HARD_MAX_MAGNET_RADIUS}].
     */
    public static int getMagnetMaxRadius() {
        return Math.clamp(magnetMaxRadius, 1, HARD_MAX_MAGNET_RADIUS);
    }

    /**
     * @return The radius of Magnet walls that have not been changed, within [1, {@link #getMagnetMaxRadius()}].
     */
    public static int getMagnetDefaultRadius() {
        return Math.clamp(magnetRadius, 1, getMagnetMaxRadius());
    }

    /**
     * @return The maximum number of items in a single slot, at least 1.
     */
    public static long getMaxItemsPerSlot() {
        return Math.max(1, maxItemsPerSlot);
    }

    /**
     * @param size The outer edge length of a chest structure.
     * @return Stacks per slot for that size before upgrades, at least 1.
     */
    public static long getDepthForSize(int size) {
        return Math.max(1, getConfiguredDepthForSize(size));
    }

    private static int getConfiguredDepthForSize(int size) {
        return switch (size) {
            case 2 -> depthSize2;
            case 3 -> depthSize3;
            case 4 -> depthSize4;
            case 5 -> depthSize5;
            case 6 -> depthSize6;
            case 7 -> depthSize7;
            case 8 -> depthSize8;
            case 9 -> depthSize9;
            case 10 -> depthSize10;
            default -> throw new IllegalArgumentException("Size out of range [" + MIN_SIZE + ", " + HARD_MAX_SIZE + "]: " + size);
        };
    }

}
