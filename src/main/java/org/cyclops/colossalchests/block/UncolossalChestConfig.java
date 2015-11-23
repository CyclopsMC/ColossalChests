package org.cyclops.colossalchests.block;

import net.minecraft.client.model.ModelChest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.tileentity.RenderTileEntityUncolossalChest;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static UncolossalChestConfig _instance;

    /**
     * Make a new instance.
     */
    public UncolossalChestConfig() {
        super(
                ColossalChests._instance,
        	true,
            "uncolossalChest",
            null,
            UncolossalChest.class
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ModelChest model = new ModelChest();
        ColossalChests._instance.getProxy().registerRenderer(TileUncolossalChest.class, new RenderTileEntityUncolossalChest(model));
    }
    
}
