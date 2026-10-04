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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
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
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
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
    private static final int COLOR_GHOST = 0x808B8B8B;
    private static final int COLOR_UPGRADE_REFUSED = 0x80FF3030;
    private static final int COLOR_PADLOCK = 0xFFF0C030;
    private static final int COLOR_PADLOCK_SHACKLE = 0xFFC8C8D0;
    private static final int COLOR_COUNT = 0xFFFFFF;
    private static final int COLOR_COUNT_CAPPED = 0xFFFF55;
    private static final int COLOR_WARNING = 0xFFAA0000;
    private static final int SETTINGS_WIDTH = 16;
    private static final int SETTINGS_HEIGHT = 15;
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_HEIGHT = 12;
    private static final ResourceLocation PANEL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int PANEL_TEXTURE_WIDTH = 176;
    private static final int PANEL_TEXTURE_HEIGHT = 222;
    private static final int PANEL_CORNER = 4;
    private static final int UPGRADE_PANEL_X = ContainerChest.UPGRADE_SLOT_X - 7;
    private static final int UPGRADE_PANEL_Y = ContainerChest.UPGRADE_SLOT_Y - 4;

    private final ChestLayout layout;
    private final List<Button> settingsButtons = Lists.newArrayList();
    private EditBox searchField;
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
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.lock_all"),
                        b -> sendClick(0, ChestClickAction.LOCK_ALL))
                .bounds(settingsX, settingsY + 2 * (height + 2), width, height).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.clear_locks"),
                        b -> sendClick(0, ChestClickAction.CLEAR_LOCKS))
                .bounds(settingsX + width + 2, settingsY + 2 * (height + 2), width, height).build()));
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
        // Locks come with the Lock upgrade.
        boolean locks = hasLockUpgrade();
        for (int i = 3; i < 5; i++) {
            Button button = settingsButtons.get(i);
            if (button.active != locks || button.getTooltip() == null) {
                button.active = locks;
                button.setTooltip(Tooltip.create(Component.translatable(locks
                        ? "gui.colossalchests2." + (i == 3 ? "lock_all" : "clear_locks") + ".info"
                        : "gui.colossalchests2.requires_lock_upgrade")));
            }
        }
    }

    private boolean hasLockUpgrade() {
        return menu.getUpgradeSet().has(ChestUpgrades.LOCK);
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
        renderUpgradeSlotTooltip(guiGraphics, mouseX, mouseY);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // The upgrade column sticks out on the left, behind the main panel.
        if (menu.getUpgradeSlotCount() > 0) {
            drawPanel(guiGraphics, leftPos + UPGRADE_PANEL_X, topPos + UPGRADE_PANEL_Y, -UPGRADE_PANEL_X + 4, getUpgradePanelHeight());
        }
        drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            drawSlot(guiGraphics, leftPos + slot.x, topPos + slot.y);
        }
        // Holding an upgrade that does not fit marks the free upgrade slots.
        ItemStack carried = menu.getCarried();
        if (ItemChestUpgrade.getUpgrade(carried) != null && menu.getUpgradeInsertProblem(carried) != null) {
            for (int i = 0; i < menu.getUpgradeSlotCount(); i++) {
                Slot slot = menu.slots.get(menu.getUpgradeSlotsStart() + i);
                if (!slot.hasItem()) {
                    guiGraphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, COLOR_UPGRADE_REFUSED);
                }
            }
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
            boolean match = ChestSearch.matches(deepSlot, query, stack -> stack.getHoverName().getString(),
                    stack -> getTooltipFromItem(minecraft, stack).stream().map(Component::getString).toList());
            if (match && !query.isBlank()) {
                drawSearchHit(guiGraphics, x, y);
            }
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
                } else {
                    // A slot reserved by a lock shows a ghost of its item.
                    guiGraphics.fill(x, y, x + 16, y + 16, 300, COLOR_GHOST);
                }
            }
            if (deepSlot.isLocked()) {
                drawPadlock(guiGraphics, x, y);
            }
            if (menu.isChestSlotOverCapacity(slot)) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_OVER_CAPACITY);
            }
            if (!match) {
                guiGraphics.fill(x, y, x + 16, y + 16, 400, COLOR_SEARCH_MISS);
            }
            if (slot == hovered) {
                renderSlotHighlight(guiGraphics, x, y, 0);
            }
        }
    }

    /**
     * A small padlock in the top left corner of a slot.
     */
    private static void drawPadlock(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 350);
        guiGraphics.fill(x, y + 3, x + 7, y + 9, COLOR_OUTLINE);
        guiGraphics.fill(x + 1, y, x + 6, y + 4, COLOR_OUTLINE);
        guiGraphics.fill(x + 2, y + 1, x + 5, y + 4, COLOR_PADLOCK_SHACKLE);
        guiGraphics.fill(x + 3, y + 2, x + 4, y + 4, COLOR_OUTLINE);
        guiGraphics.fill(x + 1, y + 4, x + 6, y + 8, COLOR_PADLOCK);
        guiGraphics.fill(x + 3, y + 5, x + 4, y + 7, COLOR_OUTLINE);
        guiGraphics.pose().popPose();
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

    private Component getCapacityLine(String key, int maxStackSize) {
        long capacity = menu.getProfile().capacityFor(maxStackSize);
        return Component.translatable("gui.colossalchests2.capacity." + key, capacity > 0 ? Component.literal(formatCount(capacity))
                : Component.translatable("gui.colossalchests2.capacity.none")).withStyle(ChatFormatting.GRAY);
    }

    private Component getCapacityInfo() {
        long depth = menu.getProfile().depth();
        return depth == 1 ? Component.translatable("gui.colossalchests2.stacks_per_slot.one")
                : Component.translatable("gui.colossalchests2.stacks_per_slot", formatCount(depth));
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

    private int getFullSlots() {
        int count = 0;
        for (int slot = 0; slot < menu.getChestSlotCount(); slot++) {
            DeepSlot deepSlot = menu.getChestSlot(slot);
            if (deepSlot.getCount() > 0 && deepSlot.getCount() >= menu.getChestSlotCapacity(slot)) {
                count++;
            }
        }
        return count;
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
            lines.add(Component.translatable("gui.colossalchests2.slots_full", formatCount(getFullSlots())).withStyle(ChatFormatting.GRAY));
            int overCapacity = getOverCapacityCount();
            if (overCapacity > 0) {
                lines.add(Component.translatable("gui.colossalchests2.over_capacity", formatCount(overCapacity)).withStyle(ChatFormatting.RED));
            }
            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        } else if (x >= right - font.width(getCapacityInfo()) && x < right) {
            guiGraphics.renderTooltip(font, List.of(
                    Component.translatable("gui.colossalchests2.stacks_per_slot.info"),
                    getCapacityLine("stack_64", 64), getCapacityLine("stack_16", 16), getCapacityLine("stack_1", 1)),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    private int getUpgradePanelHeight() {
        return ContainerChest.UPGRADE_SLOT_Y - UPGRADE_PANEL_Y + menu.getUpgradeSlotCount() * 18 + 3;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        boolean inUpgradePanel = mouseX >= left + UPGRADE_PANEL_X && mouseX < left && mouseY >= top + UPGRADE_PANEL_Y
                && mouseY < top + UPGRADE_PANEL_Y + getUpgradePanelHeight();
        return super.hasClickedOutside(mouseX, mouseY, left, top, button) && !inUpgradePanel;
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
        List<Component> lines = super.getTooltipFromContainerItem(stack);
        ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
        if (upgrade != null && !(hoveredSlot instanceof ContainerChest.UpgradeSlot)) {
            // How this chest relates to an upgrade in the inventory.
            lines = Lists.newArrayList(lines);
            Component problem = menu.getUpgradeInsertProblem(stack);
            if (problem != null) {
                addUpgradeInsertProblem(lines, stack, problem);
            } else if (!menu.hasFreeUpgradeSlot()) {
                lines.add(Component.translatable("gui.colossalchests2.upgrade.no_free_slot").withStyle(ChatFormatting.GOLD));
            } else {
                lines.add(Component.translatable("gui.colossalchests2.upgrade.takes_more",
                        menu.getMaxUpgradeCount(upgrade) - menu.getUpgradeSet().count(upgrade)).withStyle(ChatFormatting.GREEN));
            }
        }
        if (hoveredSlot instanceof ContainerChest.UpgradeSlot slot) {
            int problems = menu.getUpgradeRemovalProblems(slot.getContainerSlot());
            if (problems > 0) {
                lines = Lists.newArrayList(lines);
                lines.add((problems == 1 ? Component.translatable("gui.colossalchests2.upgrade.removal_refused.one")
                        : Component.translatable("gui.colossalchests2.upgrade.removal_refused", formatCount(problems))).withStyle(ChatFormatting.RED));
            }
        }
        return lines;
    }

    private void addUpgradeInsertProblem(List<Component> lines, ItemStack stack, Component problem) {
        lines.add(problem.copy().withStyle(ChatFormatting.RED));
        if (menu.doesBetterMaterialTakeMore(stack)) {
            lines.add(Component.translatable("gui.colossalchests2.upgrade.better_material").withStyle(ChatFormatting.GRAY));
        }
    }

    private void renderUpgradeSlotTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(hoveredSlot instanceof ContainerChest.UpgradeSlot slot) || slot.hasItem()) {
            return;
        }
        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty()) {
            Component problem = menu.getUpgradeInsertProblem(carried);
            if (problem != null) {
                List<Component> lines = Lists.newArrayList();
                addUpgradeInsertProblem(lines, carried, problem);
                guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
            }
            return;
        }
        // An empty slot lists what this chest takes.
        List<Component> lines = Lists.newArrayList(Component.translatable("gui.colossalchests2.upgrade.slot"),
                Component.translatable("gui.colossalchests2.upgrade.slot.takes").withStyle(ChatFormatting.GRAY));
        UpgradeSet installed = menu.getUpgradeSet();
        for (ChestUpgrade upgrade : ChestUpgrades.VALUES) {
            Component name = Component.translatable("item.colossalchests2.upgrade_" + upgrade.getId().getPath());
            int max = menu.getMaxUpgradeCount(upgrade);
            lines.add(max == 0
                    ? Component.translatable("gui.colossalchests2.upgrade.slot.none", name).withStyle(ChatFormatting.DARK_GRAY)
                    : Component.translatable("gui.colossalchests2.upgrade.slot.count", name, installed.count(upgrade), max).withStyle(ChatFormatting.GRAY));
        }
        guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }

    /**
     * A panel cut from the vanilla chest texture, so corners and colors match vanilla and resource packs.
     */
    private static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int c = PANEL_CORNER;
        int right = PANEL_TEXTURE_WIDTH - c;
        int bottom = PANEL_TEXTURE_HEIGHT - c;
        // Corners.
        blitPanel(guiGraphics, x, y, c, c, 0, 0, c, c);
        blitPanel(guiGraphics, x + width - c, y, c, c, right, 0, c, c);
        blitPanel(guiGraphics, x, y + height - c, c, c, 0, bottom, c, c);
        blitPanel(guiGraphics, x + width - c, y + height - c, c, c, right, bottom, c, c);
        // Edges, stretched from a plain strip.
        blitPanel(guiGraphics, x + c, y, width - 2 * c, c, c, 0, 1, c);
        blitPanel(guiGraphics, x + c, y + height - c, width - 2 * c, c, c, bottom, 1, c);
        blitPanel(guiGraphics, x, y + c, c, height - 2 * c, 0, c, c, 1);
        blitPanel(guiGraphics, x + width - c, y + c, c, height - 2 * c, right, c, c, 1);
        // Background, from a plain pixel next to the title.
        blitPanel(guiGraphics, x + c, y + c, width - 2 * c, height - 2 * c, c + 1, c + 1, 1, 1);
    }

    private static void blitPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight) {
        if (width > 0 && height > 0) {
            guiGraphics.blit(PANEL_TEXTURE, x, y, width, height, u, v, uWidth, vHeight, 256, 256);
        }
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
     * A border along the inside of a slot that matches the search, behind its item.
     */
    private static void drawSearchHit(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 1, COLOR_SEARCH_HIT);
        guiGraphics.fill(x, y + 15, x + 16, y + 16, COLOR_SEARCH_HIT);
        guiGraphics.fill(x, y + 1, x + 1, y + 15, COLOR_SEARCH_HIT);
        guiGraphics.fill(x + 15, y + 1, x + 16, y + 15, COLOR_SEARCH_HIT);
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
        if (deepSlot.isLocked()) {
            lines.add(Component.translatable("gui.colossalchests2.slot_locked").withStyle(ChatFormatting.GOLD));
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
        if (left && hasAltDown() && hasLockUpgrade()) {
            sendClick(slot, ChestClickAction.TOGGLE_LOCK);
            return true;
        }
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
                if (oneEach && hasLockUpgrade() && menu.getChestSlot(slot).isEmpty()) {
                    // With the Lock upgrade, a right click on an empty slot reserves it for the cursor item.
                    sendClick(slot, ChestClickAction.LOCK_TO_CURSOR);
                } else {
                    sendClick(slot, oneEach ? ChestClickAction.TAKE_HALF : ChestClickAction.TAKE_STACK);
                }
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
