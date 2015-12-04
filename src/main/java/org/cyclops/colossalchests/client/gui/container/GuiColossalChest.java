package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.network.packet.ClickWindowPacketOverride;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonArrow;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.init.ModBase;

import java.io.IOException;

/**
 * GUI for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class GuiColossalChest extends ScrollingGuiContainer {

    private static final int TEXTUREWIDTH = 195;
    private static final int TEXTUREHEIGHT = 194;

    private final TileColossalChest tile;

    private GuiButtonArrow buttonUp;
    private GuiButtonArrow buttonDown;

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
    public void initGui() {
        super.initGui();
        buttonList.add(buttonUp = new GuiButtonArrow(0, this.guiLeft + 173, this.guiTop + 7, GuiButtonArrow.Direction.NORTH));
        buttonList.add(buttonDown = new GuiButtonArrow(1, this.guiLeft + 173, this.guiTop + 129, GuiButtonArrow.Direction.SOUTH));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        int i = (button == buttonUp) ? 1 : ((button == buttonDown ? -1 : 0));
        this.currentScroll = (float)((double)this.currentScroll - (double)i / (double)getScrollStep());
        this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
        getScrollingInventoryContainer().scrollTo(this.currentScroll);
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
        fontRendererObj.drawString(tile.getCommandSenderName(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawForgegroundString();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        if (slotIn != null) {
            slotId = slotIn.slotNumber;
        }
        // Send our own packet, to avoid C0EPacketClickWindow to be sent to the server what would trigger an overflowable S30PacketWindowItems
        windowClick(this.inventorySlots.windowId, slotId, clickedButton, clickType, this.mc.thePlayer);
    }

    // Adapted from PlayerControllerMP#windowClick
    protected ItemStack windowClick(int windowId, int slotId, int mouseButtonClicked, int p_78753_4_, EntityPlayer playerIn) {
        short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
        ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, p_78753_4_, playerIn);
        // Original: this.netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, p_78753_4_, itemstack, short1));
        ColossalChests._instance.getPacketHandler().sendToServer(
                new ClickWindowPacketOverride(windowId, slotId, mouseButtonClicked, p_78753_4_, itemstack, short1));
        return itemstack;
    }
}
