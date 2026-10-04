package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ChestClickLogic;
import org.cyclops.colossalchests2.inventory.ChestLayout;
import org.cyclops.colossalchests2.inventory.ChestSearch;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestSettingsPacket;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Screen for {@link ContainerChest}. It draws the chest slots itself and sends clicks on them to the server.
 * Slots always stay in place: searching only dims the slots that do not match.
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
    private static final int COLOR_SEARCH_MISS = 0xC0303030;
    private static final int COLOR_SEARCH_HIT = 0xFFFFD83D;
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_DRAG_PREVIEW = 0x80FFFFFF;
    private static final int COLOR_COUNT = 0xFFFFFF;
    private static final int COLOR_COUNT_CAPPED = 0xFFFF55;
    private static final int COLOR_WARNING = 0xFFAA0000;
    private static final int SETTINGS_WIDTH = 16;
    private static final int SETTINGS_HEIGHT = 15;
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_HEIGHT = 12;

    private final ChestLayout layout;
    private final List<Button> settingsButtons = Lists.newArrayList();
    private EditBox searchField;
    private final Tooltip searchHelp = Tooltip.create(Component.translatable("gui.colossalchests2.search.info"));
    private boolean settingsOpen;

    // Dragging the cursor stack over chest slots spreads it, like over vanilla slots.
    private int dragButton = -1;
    private int dragStartSlot = -1;
    private final Set<Integer> draggedSlots = Sets.newLinkedHashSet();
    @Nullable
    private DragPreview dragPreview;

    public ContainerScreenChest(ContainerChest menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.layout = menu.getLayout();
        this.imageWidth = layout.getWidth();
        this.imageHeight = layout.getHeight();
    }

    @Override
    protected void init() {
        super.init();
        settingsButtons.clear();

        // Borderless on a sunken field, like the creative search tab.
        String query = searchField != null ? searchField.getValue() : "";
        searchField = new EditBox(font, leftPos + getGridLeft() + 2, topPos + SEARCH_Y + 2, getGridWidth() - 4, font.lineHeight,
                Component.translatable("gui.colossalchests2.search"));
        searchField.setMaxLength(ContainerChest.MAX_QUERY_LENGTH);
        searchField.setBordered(false);
        searchField.setTextColor(0xFFFFFF);
        searchField.setValue(query);
        addRenderableWidget(searchField);

        addRenderableWidget(new SettingsButton(leftPos + getGridLeft() + getGridWidth() - SETTINGS_WIDTH, topPos + 3,
                b -> setSettingsOpen(!settingsOpen)));

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
        ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundChestSettingsPacket(menu.containerId, settings));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Help on hover, hidden while typing so it does not cover the slots.
        Tooltip searchTooltip = searchField.isFocused() ? null : searchHelp;
        if (searchField.getTooltip() != searchTooltip) {
            searchField.setTooltip(searchTooltip);
        }
        ChestSettings settings = menu.getSettings();
        settingsButtons.get(0).setMessage(toggleLabel("show_fill_levels", settings.showFillLevels()));
        settingsButtons.get(1).setMessage(toggleLabel("show_counts", settings.showCounts()));
        settingsButtons.get(2).setMessage(toggleLabel("show_upgrade_indicators", settings.showUpgradeIndicators()));
    }

    private static Component toggleLabel(String key, boolean value) {
        return Component.translatable("gui.colossalchests2." + key,
                Component.translatable(value ? "options.on" : "options.off"));
    }

    private record DragPreview(ItemStack cursor, ItemStack remainder, Map<Integer, Long> added, Set<Integer> capped) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Like vanilla, the cursor shows what a drag would leave on it.
        dragPreview = getDragPreview();
        if (dragPreview != null) {
            menu.setCarried(dragPreview.remainder());
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (dragPreview != null) {
            menu.setCarried(dragPreview.cursor());
        }
        renderChestTooltip(guiGraphics, mouseX, mouseY);
        renderInfoTooltip(guiGraphics, mouseX, mouseY);
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
        drawSearchField(guiGraphics, leftPos + getGridLeft(), topPos + SEARCH_Y, getGridWidth());
        String query = searchField.getValue();
        int hovered = getHoveredSlot(mouseX, mouseY);
        for (int slot = 0; slot < layout.slotCount(); slot++) {
            int x = leftPos + layout.getSlotX(slot);
            int y = topPos + layout.getSlotY(slot);
            drawSlot(guiGraphics, x, y);
            DeepSlot deepSlot = menu.getChestSlot(slot);
            if (dragPreview != null && dragPreview.added().containsKey(slot)) {
                // What the slot would hold when releasing the drag now, yellow when full.
                guiGraphics.fill(x, y, x + 16, y + 16, COLOR_DRAG_PREVIEW);
                guiGraphics.renderItem(dragPreview.cursor(), x, y);
                drawCount(guiGraphics, deepSlot.getCount() + dragPreview.added().get(slot), x, y,
                        dragPreview.capped().contains(slot) ? COLOR_COUNT_CAPPED : COLOR_COUNT);
            } else if (!deepSlot.isEmpty()) {
                guiGraphics.renderItem(deepSlot.getPrototype(), x, y);
                if (deepSlot.getCount() > 0) {
                    drawCount(guiGraphics, deepSlot.getCount(), x, y, COLOR_COUNT);
                }
            }
            if (menu.isChestSlotOverCapacity(slot)) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_OVER_CAPACITY);
            }
            if (!ChestSearch.matches(deepSlot, query, stack -> stack.getHoverName().getString(),
                    stack -> getTooltipFromItem(minecraft, stack).stream().map(Component::getString).toList())) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_SEARCH_MISS);
            } else if (!query.isBlank()) {
                drawSearchHit(guiGraphics, x, y);
            }
            if (slot == hovered) {
                renderSlotHighlight(guiGraphics, x, y, 0);
            }
        }
    }

    private void drawCount(GuiGraphics guiGraphics, long count, int x, int y, int color) {
        String text = IModHelpers.get().getGuiHelpers().quantityToScaledString(count);
        float scale = 0.5F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);
        IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, font, text,
                x + 16 - Math.round(font.width(text) * scale), y + 16 - Math.round(font.lineHeight * scale) + 1,
                scale, color, true, Font.DisplayMode.NORMAL);
        guiGraphics.pose().popPose();
    }

    /**
     * The left and width of the slot grid's background, which the search field lines up with.
     */
    private int getGridLeft() {
        return layout.getGridX() - 1;
    }

    private int getGridWidth() {
        return layout.columns() * ChestLayout.SLOT_SIZE;
    }

    private int getInfoY() {
        return layout.getPlayerInventoryY() - 11;
    }

    private Component getSlotsInfo() {
        return Component.translatable("gui.colossalchests2.slots_used", formatCount(getUsedSlots()), formatCount(menu.getChestSlotCount()));
    }

    private Component getCapacityInfo() {
        return Component.translatable("gui.colossalchests2.per_slot", formatCount(menu.getDepth() * 64));
    }

    /**
     * Slots in use, and capacity per slot, where vanilla shows the inventory label.
     */
    private void drawInfo(GuiGraphics guiGraphics) {
        int y = getInfoY();
        guiGraphics.drawString(font, getSlotsInfo(), getGridLeft() + 1, y, getOverCapacityCount() > 0 ? COLOR_WARNING : COLOR_TEXT, false);
        Component capacity = getCapacityInfo();
        guiGraphics.drawString(font, capacity, getGridLeft() + getGridWidth() - font.width(capacity), y, COLOR_TEXT, false);
    }

    private int getUsedSlots() {
        int count = 0;
        for (int slot = 0; slot < menu.getChestSlotCount(); slot++) {
            if (menu.getChestSlot(slot).getCount() > 0) {
                count++;
            }
        }
        return count;
    }

    private int getOverCapacityCount() {
        int count = 0;
        for (int slot = 0; slot < menu.getChestSlotCount(); slot++) {
            if (menu.isChestSlotOverCapacity(slot)) {
                count++;
            }
        }
        return count;
    }

    private void renderInfoTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = mouseX - leftPos;
        int y = mouseY - topPos - getInfoY();
        if (y < -1 || y > font.lineHeight) {
            return;
        }
        int left = getGridLeft() + 1;
        int right = getGridLeft() + getGridWidth();
        if (x >= left && x < left + font.width(getSlotsInfo())) {
            List<Component> lines = Lists.newArrayList(Component.translatable("gui.colossalchests2.slots_used.info",
                    formatCount(getUsedSlots()), formatCount(menu.getChestSlotCount())));
            int overCapacity = getOverCapacityCount();
            if (overCapacity > 0) {
                lines.add(Component.translatable("gui.colossalchests2.over_capacity", formatCount(overCapacity)).withStyle(ChatFormatting.RED));
            }
            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        } else if (x >= right - font.width(getCapacityInfo()) && x < right) {
            guiGraphics.renderTooltip(font, List.of(
                    Component.translatable("gui.colossalchests2.per_slot.info", formatCount(menu.getDepth())),
                    Component.translatable("gui.colossalchests2.capacity.stack_64", formatCount(menu.getDepth() * 64)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.colossalchests2.capacity.stack_16", formatCount(menu.getDepth() * 16)).withStyle(ChatFormatting.GRAY)),
                    Optional.empty(), mouseX, mouseY);
        }
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

    private boolean isOverSearchField(double mouseX, double mouseY) {
        int x = leftPos + getGridLeft();
        int y = topPos + SEARCH_Y;
        return mouseX >= x && mouseX < x + getGridWidth() && mouseY >= y && mouseY < y + SEARCH_HEIGHT;
    }

    /**
     * A border along the inside of a slot that matches the search.
     */
    private static void drawSearchHit(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 1, 400, COLOR_SEARCH_HIT);
        guiGraphics.fill(x, y + 15, x + 16, y + 16, 400, COLOR_SEARCH_HIT);
        guiGraphics.fill(x, y + 1, x + 1, y + 15, 400, COLOR_SEARCH_HIT);
        guiGraphics.fill(x + 15, y + 1, x + 16, y + 15, 400, COLOR_SEARCH_HIT);
    }

    /**
     * The sunken search field of the creative inventory.
     */
    private static void drawSearchField(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + SEARCH_HEIGHT, COLOR_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + width, y + SEARCH_HEIGHT, COLOR_LIGHT);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + SEARCH_HEIGHT - 1, COLOR_SLOT);
        guiGraphics.fill(x + width - 1, y, x + width, y + 1, COLOR_SLOT);
        guiGraphics.fill(x, y + SEARCH_HEIGHT - 1, x + 1, y + SEARCH_HEIGHT, COLOR_SLOT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawInfo(guiGraphics);
        guiGraphics.drawString(font, title, 8, 6, COLOR_TEXT, false);
        if (settingsOpen) {
            guiGraphics.drawString(font, Component.translatable("gui.colossalchests2.settings"), 8, SEARCH_Y + 2, COLOR_TEXT, false);
        }
    }

    private void renderChestTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int slot = getHoveredSlot(mouseX, mouseY);
        if (slot < 0 || !menu.getCarried().isEmpty()) {
            return;
        }
        DeepSlot deepSlot = menu.getChestSlot(slot);
        if (deepSlot.isEmpty()) {
            return;
        }
        List<Component> lines = Lists.newArrayList(getTooltipFromContainerItem(deepSlot.getPrototype()));
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

    /**
     * @return The chest slot under the mouse, or -1.
     */
    private int getHoveredSlot(double mouseX, double mouseY) {
        if (settingsOpen) {
            return -1;
        }
        int slot = layout.getPositionAt(mouseX - leftPos, mouseY - topPos);
        return slot < menu.getChestSlotCount() ? slot : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && searchField.visible && isOverSearchField(mouseX, mouseY)) {
            // Select all, so typing replaces the query.
            setFocused(searchField);
            searchField.setFocused(true);
            searchField.moveCursorToEnd(false);
            searchField.setHighlightPos(0);
            return true;
        }
        int slot = getHoveredSlot(mouseX, mouseY);
        if (slot < 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        boolean left = button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        boolean right = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        if ((left || right) && !menu.getCarried().isEmpty() && !hasShiftDown() && !hasControlDown()) {
            // With a stack on the cursor, the release decides between a click and a drag.
            dragButton = button;
            dragStartSlot = slot;
            draggedSlots.clear();
            if (menu.canChestSlotAccept(slot, menu.getCarried())) {
                draggedSlots.add(slot);
            }
        } else if (left || right) {
            ChestClickAction action;
            if (right) {
                action = ChestClickAction.TAKE_HALF;
            } else if (hasShiftDown()) {
                action = ChestClickAction.MOVE_STACK;
            } else if (hasControlDown()) {
                action = ChestClickAction.MOVE_ALL;
            } else {
                action = ChestClickAction.TAKE_STACK;
            }
            sendClick(slot, action);
        }
        // Clicks on the chest grid never reach vanilla slot handling.
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragButton >= 0 && button == dragButton) {
            int slot = getHoveredSlot(mouseX, mouseY);
            // Like vanilla, never drag over more slots than there are items.
            if (slot >= 0 && draggedSlots.size() < menu.getCarried().getCount()
                    && menu.canChestSlotAccept(slot, menu.getCarried())) {
                draggedSlots.add(slot);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * @return What releasing the drag now would do, or null if not dragging over several slots.
     */
    @Nullable
    private DragPreview getDragPreview() {
        ItemStack cursor = menu.getCarried();
        if (dragButton < 0 || draggedSlots.size() < 2 || cursor.isEmpty()) {
            return null;
        }
        Map<Integer, Long> added = Maps.newHashMap();
        Set<Integer> capped = Sets.newHashSet();
        int[] slots = draggedSlots.stream().mapToInt(Integer::intValue).toArray();
        ItemStack remainder = ChestClickLogic.drag(slots, dragButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT, cursor, (slot, amount) -> {
            long inserted = Math.min(amount, menu.getChestSlotSpace(slot, cursor));
            added.put(slot, inserted);
            if (inserted < amount) {
                capped.add(slot);
            }
            return inserted;
        });
        return new DragPreview(cursor, remainder, added, capped);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragButton >= 0 && button == dragButton) {
            boolean oneEach = dragButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            if (draggedSlots.size() < 2) {
                int slot = draggedSlots.isEmpty() ? dragStartSlot : draggedSlots.iterator().next();
                sendClick(slot, oneEach ? ChestClickAction.TAKE_HALF : ChestClickAction.TAKE_STACK);
            } else {
                ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundChestDragPacket(menu.containerId,
                        draggedSlots.stream().mapToInt(Integer::intValue).toArray(), oneEach));
            }
            dragButton = -1;
            draggedSlots.clear();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendClick(int slot, ChestClickAction action) {
        ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundChestClickPacket(menu.containerId, slot, action));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Typing in the search field must not close the screen.
        if (searchField.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return searchField.keyPressed(keyCode, scanCode, modifiers) || searchField.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * A vanilla button with the settings icon of Cyclops mods.
     */
    private static class SettingsButton extends Button {

        // The visible part of the 18x18 config board icon.
        private static final int ICON_U = 38;
        private static final int ICON_V = 21;
        private static final int ICON_WIDTH = 14;
        private static final int ICON_HEIGHT = 13;

        SettingsButton(int x, int y, OnPress onPress) {
            super(x, y, SETTINGS_WIDTH, SETTINGS_HEIGHT, Component.translatable("gui.colossalchests2.settings"), onPress, DEFAULT_NARRATION);
            setTooltip(Tooltip.create(Component.translatable("gui.colossalchests2.settings")));
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Component message = getMessage();
            // Draw the vanilla button without its label, then the icon on it.
            setMessage(Component.empty());
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            setMessage(message);
            guiGraphics.blit(Images.ICONS, getX() + 1, getY() + 1, ICON_U, ICON_V, ICON_WIDTH, ICON_HEIGHT);
        }
    }

}
