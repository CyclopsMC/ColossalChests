package org.cyclops.colossalchests2.config;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

/**
 * {@link ChestTablesReloadListener} with the id Fabric needs.
 * @author rubensworks
 */
public class ChestTablesReloadListenerFabric extends ChestTablesReloadListener implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }
}
