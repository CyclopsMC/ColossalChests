package org.cyclops.colossalchests.modcompat;

import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;

/**
 * Mod compat for the Iron Chest mod.
 * @author rubensworks
 *
 */
public class IronChestModCompat implements IModCompat {

    @Override
    public String getId() {
        return Reference.MOD_IRONCHEST;
    }

    @Override
    public boolean isEnabledDefault() {
        return true;
    }

    @Override
    public String getComment() {
        return "If the non-wood variants should use the textures of the Iron Chest mod.";
    }

    @Override
    public ICompatInitializer createInitializer() {
        return (mod) -> {
            if(mod.getModHelpers().getMinecraftHelpers().isClientSide()) {
                IronChestModCompatClient.overrideTextures();
            }
        };
    }

}
