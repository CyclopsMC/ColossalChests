package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerMaterialUpgradeTool}.
 * @author rubensworks
 */
public class ContainerMaterialUpgradeToolConfig<M extends IModBase> extends GuiConfigCommon<ContainerMaterialUpgradeTool, M> {

    public ContainerMaterialUpgradeToolConfig(M mod) {
        super(mod, "material_upgrade_tool", eConfig -> new ContainerTypeDataCommon<>(ContainerMaterialUpgradeTool::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerMaterialUpgradeTool> getScreenFactoryProvider() {
        return new ContainerMaterialUpgradeToolConfigScreenFactoryProvider();
    }
}
