package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;

/**
 * GUI for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class GuiColossalChest extends GuiContainerExtended {

    /**
     * Texture width.
     */
    public static final int TEXTUREWIDTH = 236;
    /**
     * Texture height.
     */
    public static final int TEXTUREHEIGHT = 189;

    /**
     * Tank width.
     */
    public static final int TANKWIDTH = 16;
    /**
     * Tank height.
     */
    public static final int TANKHEIGHT = 58;
    /**
     * Tank X.
     */
    public static final int TANKX = TEXTUREWIDTH;
    /**
     * Tank Y.
     */
    public static final int TANKY = 0;
    /**
     * Tank target X.
     */
    public static final int TANKTARGETX = 28;
    /**
     * Tank target Y.
     */
    public static final int TANKTARGETY = 82;

    /**
     * Tank width.
     */
    public static final int EFFICIENCYBARWIDTH = 2;
    /**
     * Tank height.
     */
    public static final int EFFICIENCYBARHEIGHT = 58;
    /**
     * Tank X.
     */
    public static final int EFFICIENCYBARX = TEXTUREWIDTH;
    /**
     * Tank Y.
     */
    public static final int EFFICIENCYBARY = 58;
    /**
     * Tank target X.
     */
    public static final int EFFICIENCYBARTARGETX = 46;
    /**
     * Tank target Y.
     */
    public static final int EFFICIENCYBARTARGETY = 82;

    private final TileColossalChest tile;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public GuiColossalChest(InventoryPlayer inventory, TileColossalChest tile) {
        super(new ContainerColossalChest(inventory, tile));
        this.tile = tile;
    }

    @Override
    protected int getBaseYSize() {
        return TEXTUREHEIGHT;
    }

    @Override
    public String getGuiTexture() {
        return Reference.TEXTURE_PATH_GUI + "colossalChest_gui.png";
    }

    @Override
    protected int getBaseXSize() {
        return TEXTUREWIDTH;
    }
    
}
