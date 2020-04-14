package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;
import org.cyclops.cyclopscore.client.render.tileentity.ItemStackTileEntityRendererBase;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityUncolossalChestRender extends ItemStackTileEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRender() {
        super(TileUncolossalChest::new);
    }

}
