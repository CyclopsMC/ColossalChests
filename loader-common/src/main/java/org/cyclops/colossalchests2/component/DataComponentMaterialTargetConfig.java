package org.cyclops.colossalchests2.component;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the material a Material Upgrade Tool changes chests to.
 * @author rubensworks
 */
public class DataComponentMaterialTargetConfig<M extends IModBase> extends DataComponentConfigCommon<ResourceLocation, M> {

    public DataComponentMaterialTargetConfig(M mod) {
        super(mod, "material_target", builder -> builder
                .persistent(ResourceLocation.CODEC)
                .networkSynchronized(ResourceLocation.STREAM_CODEC));
    }

}
