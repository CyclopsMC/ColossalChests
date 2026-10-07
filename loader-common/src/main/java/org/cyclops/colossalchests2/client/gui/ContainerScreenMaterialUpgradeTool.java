package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeTool;

import java.util.List;

/**
 * Picks the material of a Material Upgrade Tool, with a button per material showing its wall.
 * @author rubensworks
 */
public class ContainerScreenMaterialUpgradeTool extends AbstractContainerScreen<ContainerMaterialUpgradeTool> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 74;
    private static final int BUTTON_Y = 20;
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_SPACING = 22;
    private static final int COLUMNS = 7;
    private static final int COLOR_LABEL = 0x404040;
    private static final int COLOR_HINT = 0x707070;

    private final List<Button> buttons = Lists.newArrayList();

    public ContainerScreenMaterialUpgradeTool(ContainerMaterialUpgradeTool menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT + (getRows() - 1) * BUTTON_SPACING;
    }

    private static int getRows() {
        return Math.max(1, (ChestMaterial.getAll().size() + COLUMNS - 1) / COLUMNS);
    }

    /**
     * Buttons wrap into rows, each centered.
     */
    private int getButtonX(int index) {
        int row = index / COLUMNS;
        int inRow = Math.min(COLUMNS, ChestMaterial.getAll().size() - row * COLUMNS);
        return leftPos + (WIDTH - inRow * BUTTON_SPACING + BUTTON_SPACING - BUTTON_SIZE) / 2 + (index % COLUMNS) * BUTTON_SPACING;
    }

    private int getButtonY(int index) {
        return topPos + BUTTON_Y + (index / COLUMNS) * BUTTON_SPACING;
    }

    private int getLabelY() {
        return BUTTON_Y + (getRows() - 1) * BUTTON_SPACING + BUTTON_SIZE + 6;
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        for (int i = 0; i < ChestMaterial.getAll().size(); i++) {
            int index = i;
            buttons.add(addRenderableWidget(Button.builder(Component.empty(),
                            b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index))
                    .bounds(getButtonX(i), getButtonY(i), BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.create(ChestMaterial.getAll().get(i).getDisplayName()))
                    .build()));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The chosen material can not be chosen again.
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).active = i != menu.getTarget();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiPanels.drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, COLOR_LABEL, false);
        int target = menu.getTarget();
        Component label = target < 0
                ? Component.translatable("gui.colossalchests2.material_upgrade_tool.no_target")
                : Component.translatable("gui.colossalchests2.material_upgrade_tool.target", ChestMaterial.getAll().get(target).getDisplayName());
        guiGraphics.drawString(font, label, (WIDTH - font.width(label)) / 2, getLabelY(), COLOR_LABEL, false);
        Component hint = Component.translatable("gui.colossalchests2.material_upgrade_tool.hint");
        guiGraphics.drawString(font, hint, (WIDTH - font.width(hint)) / 2, getLabelY() + 14, COLOR_HINT, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < ChestMaterial.getAll().size(); i++) {
            guiGraphics.renderItem(new ItemStack(ChestMaterial.getAll().get(i).getWallBlock()), getButtonX(i) + 2, getButtonY(i) + 2);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
