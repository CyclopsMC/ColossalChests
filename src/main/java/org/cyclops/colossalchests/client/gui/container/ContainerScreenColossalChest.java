package org.cyclops.colossalchests.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.network.packet.ClickWindowPacketOverride;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonArrow;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;

/**
 * GUI for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class ContainerScreenColossalChest extends ContainerScreenScrolling<ContainerColossalChest> {

    private static final int TEXTUREWIDTH = 195;
    private static final int TEXTUREHEIGHT = 194;

    public ContainerScreenColossalChest(ContainerColossalChest container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new ButtonArrow(this.leftPos + 173, this.topPos + 7, new TranslatableComponent("gui.cyclopscore.up"), (button) -> scrollRelative(1), ButtonArrow.Direction.NORTH));
        addRenderableWidget(new ButtonArrow(this.leftPos + 173, this.topPos + 129, new TranslatableComponent("gui.cyclopscore.down"), (button) -> scrollRelative(-1), ButtonArrow.Direction.SOUTH));
    }

    protected void scrollRelative(int direction) {
        int multiplier = Minecraft.getInstance().player.isCrouching() ? 9 : 1;
        this.getScrollbar().scrollRelative(direction * multiplier);
    }

    @Override
    protected boolean isSearchEnabled() {
        return false;
    }

    @Override
    protected boolean isSubsetRenderSlots() {
        return true;
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/colossal_chest.png");
    }

    @Override
    protected int getBaseXSize() {
        return TEXTUREWIDTH;
    }

    @Override
    protected int getBaseYSize() {
        return TEXTUREHEIGHT;
    }

    protected void drawForgegroundString(PoseStack matrixStack) {
        font.draw(matrixStack, getTitle().getString(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        drawForgegroundString(matrixStack);
        //super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
        if (slotIn != null) {
            slotId = slotIn.index;
        }
        // Send our own packet, to avoid C0EPacketClickWindow to be sent to the server what would trigger an overflowable S30PacketWindowItems
        handleInventoryMouseClick(this.container.containerId, slotId, clickedButton, clickType, this.getMinecraft().player);
    }

    // Adapted from MultiPlayerGameMode#handleInventoryMouseClick
    protected ItemStack handleInventoryMouseClick(int windowId, int slotId, int mouseButtonClicked, ClickType p_78753_4_, Player playerIn) {
        //short short1 = playerIn.containerMenu.backup(playerIn.getInventory());
        ItemStack itemstack = playerIn.containerMenu.getCarried().copy();
        // Original: this.connection.send(new ServerboundContainerClickPacket(p_171800_, abstractcontainermenu.getStateId(), p_171801_, p_171802_, p_171803_, abstractcontainermenu.getCarried().copy(), int2objectmap));
        ColossalChests._instance.getPacketHandler().sendToServer(new ClickWindowPacketOverride(windowId, slotId, mouseButtonClicked, p_78753_4_, itemstack));
        return itemstack;
    }
}
