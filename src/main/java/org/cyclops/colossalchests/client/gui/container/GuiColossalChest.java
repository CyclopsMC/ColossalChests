package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * GUI for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class GuiColossalChest extends ScrollingGuiContainer {

    private static final int TEXTUREWIDTH = 195;
    private static final int TEXTUREHEIGHT = 194;

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
    protected boolean isSearchEnabled() {
        return false;
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "colossalChest.png";
    }

    @Override
    protected int getBaseXSize() {
        return TEXTUREWIDTH;
    }

    @Override
    protected int getBaseYSize() {
        return TEXTUREHEIGHT;
    }

    protected void drawForgegroundString() {
        fontRendererObj.drawString(
                L10NHelpers.localize("general.colossalchests.colossalchest.name", tile.getSizeSingular()),
                8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawForgegroundString();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
    
}
