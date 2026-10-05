package org.cyclops.colossalchests2.blockentity;



import java.util.Locale;

/**
 * What a side of a Display wall shows, each toggled per side.
 * @author rubensworks
 */
public enum DisplayOption {
    /**
     * The side itself. A hidden side looks and acts like a plain wall.
     */
    SHOWN,
    COUNT,
    FILL_LEVEL,
    UPGRADE_INDICATORS;

    public String getTranslationKey() {
        return "gui.colossalchests2.display." + name().toLowerCase(Locale.ROOT);
    }
}
