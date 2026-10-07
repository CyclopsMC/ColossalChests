package org.cyclops.colossalchests2.advancement;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link MaterialChangedTrigger}.
 * @author rubensworks
 */
public class MaterialChangedTriggerConfig<M extends IModBase> extends CriterionTriggerConfigCommon<MaterialChangedTrigger.Instance, M> {

    public MaterialChangedTriggerConfig(M mod) {
        super(mod, "material_changed", new MaterialChangedTrigger());
    }

}
