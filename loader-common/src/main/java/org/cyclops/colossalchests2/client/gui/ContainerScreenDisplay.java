package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.blockentity.DisplayOption;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;

import java.util.List;

/**
 * Settings of a Display wall, a column per face.
 * @author rubensworks
 */
public class ContainerScreenDisplay extends AbstractContainerScreen<ContainerDisplay> {

    private static final int COLOR_LABEL = 0x404040;

    private static final List<DisplayOption> ICON_OPTIONS = List.of(DisplayOption.COUNT, DisplayOption.FILL_LEVEL,
            DisplayOption.UPGRADE_INDICATORS);
    private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"),
            ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
    private static final int COLOR_ICON_OFF = 0xA0000000;
    private static final int COLOR_ICON_STRIKE = 0xFFD04030;
    private static final int COLOR_BAR_BACKGROUND = 0xFF101010;
    private static final int COLOR_BAR = 0xFF40C040;

    private final List<Button> shownButtons = Lists.newArrayList();
    private final List<OptionButton> optionButtons = Lists.newArrayList();
    private final ItemStack upgradeIcon = new ItemStack(BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "upgrade_" + ChestUpgrades.LOCK.getId().getPath())));

    public ContainerScreenDisplay(ContainerDisplay menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ContainerDisplay.WIDTH;
        this.imageHeight = ContainerDisplay.HEIGHT;
        this.inventoryLabelY = ContainerDisplay.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        shownButtons.clear();
        optionButtons.clear();
        List<Direction> faces = menu.getFaces();
        for (int i = 0; i < faces.size(); i++) {
            Direction face = faces.get(i);
            int left = leftPos + menu.getColumnX(i) - ContainerDisplay.BUTTON_WIDTH / 2;
            shownButtons.add(addRenderableWidget(Button.builder(getShownLabel(face), button -> toggle(face, DisplayOption.SHOWN))
                    .bounds(left, topPos + ContainerDisplay.BUTTON_Y, ContainerDisplay.BUTTON_WIDTH, 20)
                    .build()));
            // Spread over the width of the button above.
            int gap = (ContainerDisplay.BUTTON_WIDTH - ICON_OPTIONS.size() * ContainerDisplay.OPTION_SIZE) / (ICON_OPTIONS.size() - 1);
            for (int j = 0; j < ICON_OPTIONS.size(); j++) {
                optionButtons.add(addRenderableWidget(new OptionButton(left + j * (ContainerDisplay.OPTION_SIZE + gap),
                        topPos + ContainerDisplay.OPTION_Y, face, ICON_OPTIONS.get(j))));
            }
        }
    }

    private void toggle(Direction face, DisplayOption option) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ContainerDisplay.getButton(face, option));
    }

    private Component getShownLabel(Direction face) {
        return Component.translatable(menu.isEnabled(face, DisplayOption.SHOWN) ? "gui.colossalchests2.display.shown" : "gui.colossalchests2.display.hidden");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Options are synced after the click, so the labels follow.
        for (int i = 0; i < shownButtons.size(); i++) {
            shownButtons.get(i).setMessage(getShownLabel(menu.getFaces().get(i)));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiPanels.drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            GuiPanels.drawSlot(guiGraphics, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        List<Direction> faces = menu.getFaces();
        for (int i = 0; i < faces.size(); i++) {
            Component label = Component.translatable(menu.getFaceTranslationKey(faces.get(i)));
            guiGraphics.drawString(font, label, menu.getColumnX(i) - font.width(label) / 2, ContainerDisplay.LABEL_Y, COLOR_LABEL, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && menu.isGhostSlot(hoveredSlot.index) && !hoveredSlot.hasItem()) {
            String key = "gui.colossalchests2.wall.displayed";
            guiGraphics.renderComponentTooltip(font, List.of(Component.translatable(key),
                    Component.translatable(key + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        for (Button button : shownButtons) {
            if (button.isHovered()) {
                guiGraphics.renderComponentTooltip(font, List.of(Component.translatable("gui.colossalchests2.display.shown_option"),
                        Component.translatable("gui.colossalchests2.display.shown_option.info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
        for (OptionButton button : optionButtons) {
            if (button.isHovered()) {
                String key = button.option.getTranslationKey();
                guiGraphics.renderComponentTooltip(font, List.of(button.getMessage(),
                        Component.translatable(key + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Toggles an option of a face, shown as an icon that is struck through while off.
     */
    private class OptionButton extends Button {

        private final Direction face;
        private final DisplayOption option;

        OptionButton(int x, int y, Direction face, DisplayOption option) {
            super(x, y, ContainerDisplay.OPTION_SIZE, ContainerDisplay.OPTION_SIZE, Component.empty(),
                    button -> toggle(face, option), DEFAULT_NARRATION);
            this.face = face;
            this.option = option;
        }

        boolean isOn() {
            return menu.isEnabled(face, option);
        }

        @Override
        public Component getMessage() {
            return Component.translatable(option.getTranslationKey(), Component.translatable(isOn() ? "options.on" : "options.off"));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // The background only, the icon replaces the message.
            guiGraphics.blitSprite(BUTTON_SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), width, height);
            int x = getX();
            int y = getY();
            switch (option) {
                case COUNT -> guiGraphics.drawString(font, "64", x + (width - font.width("64")) / 2 + 1, y + 4, 0xFFFFFF, true);
                case FILL_LEVEL -> {
                    guiGraphics.fill(x + 3, y + 6, x + 13, y + 10, COLOR_BAR_BACKGROUND);
                    guiGraphics.fill(x + 3, y + 6, x + 10, y + 10, COLOR_BAR);
                }
                default -> {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(x + 2, y + 2, 0);
                    guiGraphics.pose().scale(0.75F, 0.75F, 1);
                    guiGraphics.renderItem(upgradeIcon, 0, 0);
                    guiGraphics.pose().popPose();
                }
            }
            if (!isOn()) {
                // Dim the icon and strike it through, above the item.
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_ICON_OFF);
                for (int i = 2; i < width - 2; i++) {
                    guiGraphics.fill(x + i, y + height - 1 - i, x + i + 1, y + height - i, COLOR_ICON_STRIKE);
                }
                guiGraphics.pose().popPose();
            }
        }
    }
}
