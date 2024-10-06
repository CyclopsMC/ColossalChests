package org.cyclops.colossalchests;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;

/**
 * Class that can hold basic static things that are better not hard-coded
 * like mod details, texture paths, ID's...
 * @author rubensworks
 *
 */
@SuppressWarnings("javadoc")
public class Reference {

    // Mod info
    public static final String MOD_ID = "colossalchests";
    public static final String GA_TRACKING_ID = "UA-65307010-5";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/" + MinecraftHelpers.getMinecraftVersionMajorMinor() + "/ColossalChests.txt";

    // Paths
    public static final String TEXTURE_PATH_GUI = "textures/gui/";
    public static final String TEXTURE_PATH_SKINS = "textures/skins/";
    public static final String TEXTURE_PATH_MODELS = "textures/models/";
    public static final String TEXTURE_PATH_ENTITIES = "textures/entities/";
    public static final String TEXTURE_PATH_GUIBACKGROUNDS = "textures/gui/title/background/";
    public static final String TEXTURE_PATH_ITEMS = "textures/items/";
    public static final String TEXTURE_PATH_PARTICLES = "textures/particles/";
    public static final String MODEL_PATH = "models/";

    // MOD ID's
    public static final String MOD_IRONCHEST = "ironchest";
    public static final String MOD_COMMONCAPABILITIES = "commoncapabilities";
}
