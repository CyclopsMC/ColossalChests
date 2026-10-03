package org.cyclops.colossalchests2.capability;

/**
 * What the current loader supports, for code shared between loaders.
 * @author rubensworks
 */
public final class LoaderCapabilities {

    /**
     * If blocks without a block entity, such as walls, can expose item storage on this loader.
     */
    public static boolean blockCapabilitiesSupported = true;

    private LoaderCapabilities() {
    }

}
