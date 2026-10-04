package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ChestLayout;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.inventory.ChestSortMode;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestSettingsPacket;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Screen for {@link ContainerChest}. It draws the chest slots itself and sends clicks on them to the server.
 * @author rubensworks
 */
public class ContainerScreenChest extends AbstractContainerScreen<ContainerChest> {

    private static final int COLOR_BACKGROUND = 0xFFC6C6C6;
    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SHADOW = 0xFF555555;
    private static final int COLOR_OUTLINE = 0xFF000000;
    private static final int COLOR_SLOT = 0xFF8B8B8B;
    private static final int COLOR_SLOT_SHADOW = 0xFF373737;
    private static final int COLOR_OVER_CAPACITY = 0x80FF2020;
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_WARNING = 0xFFAA0000;
    private static final int BUTTON_SIZE = 12;
    private static final ChestSortMode[] SORT_MODES = {ChestSortMode.NAME, ChestSortMode.COUNT, ChestSortMode.MOD};

    private final ChestLayout layout;
    private final List<Button> sortButtons = Lists.newArrayList();
    private final List<Button> settingsButtons = Lists.newArrayList();
    private EditBox searchField;
    private Button settingsTab;
    private boolean settingsOpen;

    public ContainerScreenChest(ContainerChest menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.layout = menu.getLayout();
        this.imageWidth = layout.getWidth();
        this.imageHeight = layout.getHeight();
        this.inventoryLabelX = layout.getPlayerInventoryX();
        this.inventoryLabelY = layout.getPlayerInventoryY() - 11;
    }

    @Override
    protected void init() {
        super.init();
        sortButtons.clear();
        settingsButtons.clear();

        String query = searchField != null ? searchField.getValue() : "";
        searchField = new EditBox(font, leftPos + 8, topPos + 19, imageWidth - 16, 11, Component.translatable("gui.colossalchests2.search"));
        searchField.setMaxLength(ContainerChest.MAX_QUERY_LENGTH);
        searchField.setHint(Component.translatable("gui.colossalchests2.search").withStyle(ChatFormatting.GRAY));
        searchField.setValue(query);
        searchField.setResponder(value -> sendSettings(menu.getSettings()));
        addRenderableWidget(searchField);

        int x = leftPos + imageWidth - 8 - 4 * (BUTTON_SIZE + 2) + 2;
        int y = topPos + 4;
        for (ChestSortMode mode : SORT_MODES) {
            Button button = Button.builder(Component.translatable("gui.colossalchests2.sort." + mode.getSerializedName() + ".short"),
                            b -> sendSettings(menu.getSettings().withSortMode(menu.getSettings().sortMode() == mode ? ChestSortMode.NONE : mode)))
                    .bounds(x, y, BUTTON_SIZE, BUTTON_SIZE)
                    .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.sort." + mode.getSerializedName())))
                    .build();
            sortButtons.add(addRenderableWidget(button));
            x += BUTTON_SIZE + 2;
        }
        settingsTab = addRenderableWidget(Button.builder(Component.literal("⚙"), b -> setSettingsOpen(!settingsOpen))
                .bounds(x, y, BUTTON_SIZE, BUTTON_SIZE)
                .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.settings")))
                .build());

        // Two columns of three rows, which fits the grid area of the smallest chest.
        int settingsX = leftPos + layout.getGridX() - 1;
        int settingsY = topPos + ChestLayout.GRID_Y - 1;
        int width = (layout.columns() * ChestLayout.SLOT_SIZE) / 2 - 1;
        int height = 16;
        settingsButtons.add(addRenderableWidget(Button.builder(Component.empty(),
                        b -> sendSettings(menu.getSettings().withShowFillLevels(!menu.getSettings().showFillLevels())))
                .bounds(settingsX, settingsY, width, height)
                .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.show_fill_levels.info"))).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.empty(),
                        b -> sendSettings(menu.getSettings().withShowCounts(!menu.getSettings().showCounts())))
                .bounds(settingsX + width + 2, settingsY, width, height)
                .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.show_counts.info"))).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.empty(),
                        b -> sendSettings(menu.getSettings().withShowUpgradeIndicators(!menu.getSettings().showUpgradeIndicators())))
                .bounds(settingsX, settingsY + height + 2, width, height)
                .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.show_upgrade_indicators.info"))).build()));
        for (String action : new String[]{"lock_all", "clear_locks"}) {
            Button button = Button.builder(Component.translatable("gui.colossalchests2." + action), b -> {})
                    .bounds(settingsX + (action.equals("lock_all") ? 0 : width + 2), settingsY + 2 * (height + 2), width, height)
                    .tooltip(Tooltip.create(Component.translatable("gui.colossalchests2.requires_lock_upgrade")))
                    .build();
            // Locks come with the Lock upgrade.
            button.active = false;
            settingsButtons.add(addRenderableWidget(button));
        }
        setSettingsOpen(settingsOpen);
    }

    private void setSettingsOpen(boolean open) {
        settingsOpen = open;
        for (Button button : settingsButtons) {
            button.visible = open;
        }
        searchField.visible = !open;
    }

    private void sendSettings(ChestSettings settings) {
        ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(
                new ServerboundChestSettingsPacket(menu.containerId, searchField.getValue(), settings));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ChestSettings settings = menu.getSettings();
        for (int i = 0; i < SORT_MODES.length; i++) {
            Component label = Component.translatable("gui.colossalchests2.sort." + SORT_MODES[i].getSerializedName() + ".short");
            sortButtons.get(i).setMessage(settings.sortMode() == SORT_MODES[i] ? label.copy().withStyle(ChatFormatting.YELLOW) : label);
        }
        settingsButtons.get(0).setMessage(toggleLabel("show_fill_levels", settings.showFillLevels()));
        settingsButtons.get(1).setMessage(toggleLabel("show_counts", settings.showCounts()));
        settingsButtons.get(2).setMessage(toggleLabel("show_upgrade_indicators", settings.showUpgradeIndicators()));
    }

    private static Component toggleLabel(String key, boolean value) {
        return Component.translatable("gui.colossalchests2." + key,
                Component.translatable(value ? "options.on" : "options.off"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderChestTooltip(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (int i = 0; i < 36; i++) {
            drawSlot(guiGraphics, leftPos + menu.slots.get(i).x, topPos + menu.slots.get(i).y);
        }
        if (settingsOpen) {
            return;
        }
        int[] view = menu.getView();
        int hovered = getHoveredPosition(mouseX, mouseY);
        for (int position = 0; position < layout.slotCount(); position++) {
            int x = leftPos + layout.getSlotX(position);
            int y = topPos + layout.getSlotY(position);
            drawSlot(guiGraphics, x, y);
            if (position >= view.length) {
                continue;
            }
            int slot = view[position];
            DeepSlot deepSlot = menu.getChestSlot(slot);
            if (!deepSlot.isEmpty()) {
                guiGraphics.renderItem(deepSlot.getPrototype(), x, y);
                if (deepSlot.getCount() > 0) {
                    drawCount(guiGraphics, deepSlot.getCount(), x, y);
                }
            }
            if (menu.isChestSlotOverCapacity(slot)) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_OVER_CAPACITY);
            }
            if (position == hovered) {
                renderSlotHighlight(guiGraphics, x, y, 0);
            }
        }
    }

    private void drawCount(GuiGraphics guiGraphics, long count, int x, int y) {
        String text = IModHelpers.get().getGuiHelpers().quantityToScaledString(count);
        float scale = 0.5F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, font, text,
                x + 16 - Math.round(font.width(text) * scale), y + 16 - Math.round(font.lineHeight * scale) + 1,
                scale, 0xFFFFFF, true, net.minecraft.client.gui.Font.DisplayMode.NORMAL);
        guiGraphics.pose().popPose();
    }

    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 1, y, x + width - 1, y + height, COLOR_OUTLINE);
        guiGraphics.fill(x, y + 1, x + width, y + height - 1, COLOR_OUTLINE);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_LIGHT);
        guiGraphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, COLOR_SHADOW);
        guiGraphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, COLOR_BACKGROUND);
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, COLOR_SLOT_SHADOW);
        guiGraphics.fill(x, y, x + 17, y + 17, COLOR_LIGHT);
        guiGraphics.fill(x, y, x + 16, y + 16, COLOR_SLOT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 7, COLOR_TEXT, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_TEXT, false);
        int overCapacity = 0;
        for (int slot = 0; slot < menu.getChestSlotCount(); slot++) {
            if (menu.isChestSlotOverCapacity(slot)) {
                overCapacity++;
            }
        }
        Component info = overCapacity > 0
                ? Component.translatable("gui.colossalchests2.over_capacity", overCapacity)
                : Component.translatable("gui.colossalchests2.info", menu.getChestSlotCount(), formatCount(menu.getDepth()));
        guiGraphics.drawString(font, info, imageWidth - 8 - font.width(info), inventoryLabelY, overCapacity > 0 ? COLOR_WARNING : COLOR_TEXT, false);
        if (settingsOpen) {
            guiGraphics.drawString(font, Component.translatable("gui.colossalchests2.settings"), 8, 21, COLOR_TEXT, false);
        }
    }

    private void renderChestTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int position = getHoveredPosition(mouseX, mouseY);
        if (position < 0 || position >= menu.getView().length || !menu.getCarried().isEmpty()) {
            return;
        }
        int slot = menu.getView()[position];
        DeepSlot deepSlot = menu.getChestSlot(slot);
        if (deepSlot.isEmpty()) {
            return;
        }
        ItemStack prototype = deepSlot.getPrototype();
        List<Component> lines = Lists.newArrayList(getTooltipFromContainerItem(prototype));
        lines.add(Component.translatable("gui.colossalchests2.count", formatCount(deepSlot.getCount()),
                formatCount(menu.getChestSlotCapacity(slot))).withStyle(ChatFormatting.GRAY));
        if (menu.isChestSlotOverCapacity(slot)) {
            lines.add(Component.translatable("gui.colossalchests2.slot_over_capacity").withStyle(ChatFormatting.RED));
        }
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }

    private static String formatCount(long count) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(count);
    }

    private int getHoveredPosition(double mouseX, double mouseY) {
        if (settingsOpen) {
            return -1;
        }
        return layout.getPositionAt(mouseX - leftPos, mouseY - topPos);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int position = getHoveredPosition(mouseX, mouseY);
        if (position >= 0) {
            int[] view = menu.getView();
            if (position < view.length && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                ChestClickAction action;
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    action = ChestClickAction.TAKE_HALF;
                } else if (hasShiftDown()) {
                    action = ChestClickAction.MOVE_STACK;
                } else if (hasControlDown()) {
                    action = ChestClickAction.MOVE_ALL;
                } else {
                    action = ChestClickAction.TAKE_STACK;
                }
                ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(
                        new ServerboundChestClickPacket(menu.containerId, view[position], action));
            }
            // Clicks on the chest grid never reach vanilla slot handling.
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Typing in the search field must not close the screen.
        if (searchField.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return searchField.keyPressed(keyCode, scanCode, modifiers) || searchField.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
