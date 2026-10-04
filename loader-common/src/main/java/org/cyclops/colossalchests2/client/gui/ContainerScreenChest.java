package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ChestLayout;
import org.cyclops.colossalchests2.inventory.ChestSearch;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestSettingsPacket;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.List;
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
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_WARNING = 0xFFAA0000;
    private static final int COLOR_TAB_UNSELECTED = 0xFFA8A8A8;
    private static final int TAB_WIDTH = 25;
    private static final int TAB_HEIGHT = 24;
    private static final int TAB_Y = 4;
    private static final ItemStack SETTINGS_ICON = new ItemStack(Items.COMPARATOR);
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_HEIGHT = 12;
    private static final ItemStack CAPACITY_ICON = new ItemStack(Items.COBBLESTONE);

    private final ChestLayout layout;
    private final List<Button> settingsButtons = Lists.newArrayList();
    private EditBox searchField;
    private boolean settingsOpen;

    // Dragging the cursor stack over chest slots spreads it, like over vanilla slots.
    private int dragButton = -1;
    private final Set<Integer> draggedSlots = Sets.newLinkedHashSet();

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
        searchField = new EditBox(font, leftPos + 10, topPos + SEARCH_Y + 2, imageWidth - 20, font.lineHeight,
                Component.translatable("gui.colossalchests2.search"));
        searchField.setMaxLength(ContainerChest.MAX_QUERY_LENGTH);
        searchField.setBordered(false);
        searchField.setTextColor(0xFFFFFF);
        searchField.setValue(query);
        addRenderableWidget(searchField);

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
        ChestSettings settings = menu.getSettings();
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
        renderInfoTooltip(guiGraphics, mouseX, mouseY);
        if (isOverSettingsTab(mouseX, mouseY)) {
            guiGraphics.renderTooltip(font, List.of(Component.translatable("gui.colossalchests2.settings")), Optional.empty(), mouseX, mouseY);
        }
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Like creative tabs: a closed tab sits behind the panel, an open one joins it.
        if (!settingsOpen) {
            drawSettingsTab(guiGraphics, false);
        }
        drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        if (settingsOpen) {
            drawSettingsTab(guiGraphics, true);
        }
        for (int i = 0; i < 36; i++) {
            drawSlot(guiGraphics, leftPos + menu.slots.get(i).x, topPos + menu.slots.get(i).y);
        }
        drawInfo(guiGraphics);
        if (settingsOpen) {
            return;
        }
        drawSearchField(guiGraphics, leftPos + 8, topPos + SEARCH_Y, imageWidth - 16);
        String query = searchField.getValue();
        int hovered = getHoveredSlot(mouseX, mouseY);
        for (int slot = 0; slot < layout.slotCount(); slot++) {
            int x = leftPos + layout.getSlotX(slot);
            int y = topPos + layout.getSlotY(slot);
            drawSlot(guiGraphics, x, y);
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
            if (!ChestSearch.matches(deepSlot, query, stack -> stack.getHoverName().getString())) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_SEARCH_MISS);
            }
            if (slot == hovered || draggedSlots.contains(slot)) {
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
                scale, 0xFFFFFF, true, Font.DisplayMode.NORMAL);
        guiGraphics.pose().popPose();
    }

    /**
     * Slot count and capacity per slot, as icons with numbers where the inventory label would be.
     */
    private void drawInfo(GuiGraphics guiGraphics) {
        int x = leftPos + 8;
        int y = topPos + getInfoY();
        drawMiniSlot(guiGraphics, x, y);
        String slots = formatCount(menu.getChestSlotCount());
        guiGraphics.drawString(font, slots, x + 11, y + 1, COLOR_TEXT, false);
        int capacityX = getCapacityIconX();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos + capacityX, y, 0);
        guiGraphics.pose().scale(0.5625F, 0.5625F, 1);
        guiGraphics.renderItem(CAPACITY_ICON, 0, 0);
        guiGraphics.pose().popPose();
        guiGraphics.drawString(font, formatCount(getItemsPerSlot()), leftPos + capacityX + 11, y + 1, COLOR_TEXT, false);
        int overCapacity = getOverCapacityCount();
        if (overCapacity > 0) {
            Component warning = Component.translatable("gui.colossalchests2.over_capacity", overCapacity);
            guiGraphics.drawString(font, warning, leftPos + imageWidth - 8 - font.width(warning), y + 1, COLOR_WARNING, false);
        }
    }

    private int getInfoY() {
        return layout.getPlayerInventoryY() - 12;
    }

    private int getCapacityIconX() {
        return 8 + 11 + font.width(formatCount(menu.getChestSlotCount())) + 8;
    }

    /**
     * @return How many items that stack to 64 fit in a slot.
     */
    private long getItemsPerSlot() {
        return menu.getDepth() * 64;
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
        if (y < 0 || y > 9) {
            return;
        }
        int capacityX = getCapacityIconX();
        if (x >= 8 && x < capacityX - 4) {
            guiGraphics.renderTooltip(font, List.of(Component.translatable("gui.colossalchests2.slots", menu.getChestSlotCount())),
                    Optional.empty(), mouseX, mouseY);
        } else if (x >= capacityX && x < capacityX + 11 + font.width(formatCount(getItemsPerSlot()))) {
            guiGraphics.renderTooltip(font, List.of(
                    Component.translatable("gui.colossalchests2.capacity", formatCount(getItemsPerSlot())),
                    Component.translatable("gui.colossalchests2.capacity.stacks", formatCount(menu.getDepth())).withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.colossalchests2.capacity.small_stacks", formatCount(menu.getDepth() * 16)).withStyle(ChatFormatting.GRAY)),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    private int getTabX() {
        return leftPos + imageWidth - 4;
    }

    private boolean isOverSettingsTab(double mouseX, double mouseY) {
        return mouseX >= getTabX() + 4 && mouseX < getTabX() + TAB_WIDTH && mouseY >= topPos + TAB_Y && mouseY < topPos + TAB_Y + TAB_HEIGHT;
    }

    private void drawSettingsTab(GuiGraphics guiGraphics, boolean open) {
        int x = getTabX();
        int y = topPos + TAB_Y;
        guiGraphics.fill(x, y, x + TAB_WIDTH - 1, y + TAB_HEIGHT, COLOR_OUTLINE);
        guiGraphics.fill(x, y + 1, x + TAB_WIDTH, y + TAB_HEIGHT - 1, COLOR_OUTLINE);
        guiGraphics.fill(x, y + 1, x + TAB_WIDTH - 1, y + TAB_HEIGHT - 1, COLOR_LIGHT);
        guiGraphics.fill(x, y + 3, x + TAB_WIDTH - 1, y + TAB_HEIGHT - 1, COLOR_SHADOW);
        guiGraphics.fill(x, y + 3, x + TAB_WIDTH - 3, y + TAB_HEIGHT - 3, open ? COLOR_BACKGROUND : COLOR_TAB_UNSELECTED);
        if (open) {
            // Join the tab with the panel by covering the panel's border.
            guiGraphics.fill(x - 1, y + 3, x + 1, y + TAB_HEIGHT - 3, COLOR_BACKGROUND);
        }
        guiGraphics.renderItem(SETTINGS_ICON, x + (TAB_WIDTH - 16) / 2, y + (TAB_HEIGHT - 16) / 2 + 1);
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

    private static void drawMiniSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 9, y + 9, COLOR_SLOT_SHADOW);
        guiGraphics.fill(x + 1, y + 1, x + 9, y + 9, COLOR_LIGHT);
        guiGraphics.fill(x + 1, y + 1, x + 8, y + 8, COLOR_SLOT);
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isOverSettingsTab(mouseX, mouseY)) {
            setSettingsOpen(!settingsOpen);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
            draggedSlots.clear();
            draggedSlots.add(slot);
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
            if (slot >= 0 && draggedSlots.size() < menu.getCarried().getCount()) {
                draggedSlots.add(slot);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragButton >= 0 && button == dragButton) {
            boolean oneEach = dragButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            if (draggedSlots.size() == 1) {
                sendClick(draggedSlots.iterator().next(), oneEach ? ChestClickAction.TAKE_HALF : ChestClickAction.TAKE_STACK);
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

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        // The tab sticks out of the panel, so clicking it must not drop the cursor stack.
        return super.hasClickedOutside(mouseX, mouseY, left, top, button) && !isOverSettingsTab(mouseX, mouseY);
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

}
