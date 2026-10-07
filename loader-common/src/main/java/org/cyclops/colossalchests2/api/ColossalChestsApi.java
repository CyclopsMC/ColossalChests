package org.cyclops.colossalchests2.api;

/**
 * Entry point of the Colossal Chests API. Addons only depend on the api package.
 * @author rubensworks
 */
public final class ColossalChestsApi {

    public static final String MOD_ID = "colossalchests2";
    private static final String IMPLEMENTATION = "org.cyclops.colossalchests2.ColossalChestsApiImpl";

    private static volatile IColossalChestsApi instance;

    private ColossalChestsApi() {
    }

    /**
     * @return The API, loaded when first used, so it works however mods are ordered.
     */
    public static IColossalChestsApi get() {
        IColossalChestsApi api = instance;
        if (api == null) {
            synchronized (ColossalChestsApi.class) {
                if (instance == null) {
                    try {
                        instance = (IColossalChestsApi) Class.forName(IMPLEMENTATION).getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Colossal Chests is not loaded", e);
                    }
                }
                api = instance;
            }
        }
        return api;
    }

}
