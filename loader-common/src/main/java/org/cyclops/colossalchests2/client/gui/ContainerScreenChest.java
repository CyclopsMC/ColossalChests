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
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ChestClickLogic;
import org.cyclops.colossalchests2.inventory.ChestLayout;
import org.cyclops.colossalchests2.inventory.ChestSearch;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestFormPacket;
import org.cyclops.colossalchests2.storage.CompressionFamiliesCache;
import org.cyclops.colossalchests2.storage.CompressionFamily;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
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
    private static final int COLOR_OVER_CAPACITY = 0x80FF2020;
    private static final int COLOR_SEARCH_MISS = 0xC0303030;
    private static final int COLOR_SEARCH_HIT = 0xFFFFD83D;
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_DRAG_PREVIEW = 0x80FFFFFF;
    private static final int COLOR_GHOST = 0x808B8B8B;
    private static final int COLOR_UPGRADE_REFUSED = 0x80FF3030;
    private static final int COLOR_PADLOCK = 0xFFF0C030;
    private static final int COLOR_PADLOCK_SHACKLE = 0xFFC8C8D0;
    private static final int COLOR_VOID = 0xFF46145F;
    private static final int COLOR_FORM_CHOSEN = 0xFFFFFFFF;
    private static final int COLOR_FORM_CHOSEN_INSIDE = 0xFF3C2A55;
    private static final int FORM_CELL_WIDTH = 24;
    private static final int FORM_ROW_HEIGHT = 31;
    private static final int COLOR_VOID_RING = 0xFF9650C8;
    private static final int COLOR_COUNT = 0xFFFFFF;
    private static final int COLOR_COUNT_CAPPED = 0xFFFF55;
    private static final int COLOR_WARNING = 0xFFAA0000;
    private static final int SETTINGS_WIDTH = 16;
    private static final int SETTINGS_HEIGHT = 15;
    private static final int SEARCH_Y = 18;
    private static final int INFO_GAP = 6;
    private static final int SEARCH_HEIGHT = 12;
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

        // Two columns of two rows, which fits the grid area of the smallest chest.
        int settingsX = leftPos + layout.getGridX() - 1;
        // From the search row.
        int settingsY = topPos + SEARCH_Y - 1;
        int width = (layout.columns() * ChestLayout.SLOT_SIZE) / 2 - 1;
        int height = 16;
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.lock_all"),
                        b -> sendClick(0, ChestClickAction.LOCK_ALL))
                .bounds(settingsX, settingsY, width, height).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.clear_locks"),
                        b -> sendClick(0, ChestClickAction.CLEAR_LOCKS))
                .bounds(settingsX + width + 2, settingsY, width, height).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.void_all"),
                        b -> sendClick(0, ChestClickAction.VOID_ALL))
                .bounds(settingsX, settingsY + height + 2, width, height).build()));
        settingsButtons.add(addRenderableWidget(Button.builder(Component.translatable("gui.colossalchests2.clear_voids"),
                        b -> sendClick(0, ChestClickAction.CLEAR_VOIDS))
                .bounds(settingsX + width + 2, settingsY + height + 2, width, height).build()));
        setSettingsOpen(settingsOpen);
    }

    private void setSettingsOpen(boolean open) {
        settingsOpen = open;
        for (Button button : settingsButtons) {
            button.visible = open;
        }
        searchField.visible = !open;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Locks and void marks come with their upgrades.
        updateUpgradeButton(settingsButtons.get(0), hasLockUpgrade(), "lock_all", "requires_lock_upgrade");
        updateUpgradeButton(settingsButtons.get(1), hasLockUpgrade(), "clear_locks", "requires_lock_upgrade");
        updateUpgradeButton(settingsButtons.get(2), hasVoidUpgrade(), "void_all", "requires_void_upgrade");
        updateUpgradeButton(settingsButtons.get(3), hasVoidUpgrade(), "clear_voids", "requires_void_upgrade");
    }

    private static void updateUpgradeButton(Button button, boolean enabled, String key, String disabledKey) {
        if (button.active != enabled || button.getTooltip() == null) {
            button.active = enabled;
            button.setTooltip(Tooltip.create(Component.translatable("gui.colossalchests2." + (enabled ? key + ".info" : disabledKey))));
        }
    }

    private boolean hasVoidUpgrade() {
        return menu.getUpgradeSet().has(ChestUpgrades.VOID);
    }

    private boolean hasLockUpgrade() {
        return menu.getUpgradeSet().has(ChestUpgrades.LOCK);
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
            GuiPanels.drawPanel(guiGraphics, leftPos + UPGRADE_PANEL_X, topPos + UPGRADE_PANEL_Y, -UPGRADE_PANEL_X + 4, getUpgradePanelHeight());
        }
        GuiPanels.drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            GuiPanels.drawSlot(guiGraphics, leftPos + slot.x, topPos + slot.y);
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
            GuiPanels.drawSlot(guiGraphics, x, y);
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
                drawCount(guiGraphics, menu.getChestSlotAmount(slot, dragPreview.cursor()) + dragPreview.added().get(slot), x, y,
                        dragPreview.capped().contains(slot) ? COLOR_COUNT_CAPPED : COLOR_COUNT);
            } else if (!deepSlot.isEmpty()) {
                guiGraphics.renderItem(deepSlot.getPrototype(), x, y);
                getCompressedFamily(deepSlot).ifPresent(family -> drawFormBadge(guiGraphics, family.get(getChosenForm(deepSlot, family)).item(), x, y));
                if (deepSlot.getCount() > 0) {
                    // A "+" when smaller forms are left over.
                    String count = IModHelpers.get().getGuiHelpers().quantityToScaledString(deepSlot.getCount());
                    drawCount(guiGraphics, deepSlot.getRemainder() > 0 ? count + "+" : count, x, y, COLOR_COUNT);
                } else if (deepSlot.getRemainder() > 0) {
                    // Less than one item of the largest form.
                    drawCount(guiGraphics, "<1", x, y, COLOR_COUNT);
                } else {
                    // A slot reserved by a lock shows a ghost of its item.
                    guiGraphics.fill(x, y, x + 16, y + 16, 300, COLOR_GHOST);
                }
            }
            if (deepSlot.isLocked()) {
                drawPadlock(guiGraphics, x, y);
            }
            if (deepSlot.isVoiding()) {
                drawVoidMark(guiGraphics, x, y);
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

    /**
     * A small dark swirl in the top right corner of a slot.
     */
    private static void drawVoidMark(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 350);
        int left = x + 16 - 7;
        guiGraphics.fill(left + 1, y, left + 6, y + 7, COLOR_OUTLINE);
        guiGraphics.fill(left, y + 1, left + 7, y + 6, COLOR_OUTLINE);
        guiGraphics.fill(left + 1, y + 1, left + 6, y + 6, COLOR_VOID);
        guiGraphics.fill(left + 2, y + 2, left + 5, y + 5, COLOR_VOID_RING);
        guiGraphics.fill(left + 3, y + 3, left + 4, y + 4, COLOR_OUTLINE);
        guiGraphics.pose().popPose();
    }

    /**
     * The form that clicks take, small in the bottom left corner of a compressed slot, on the picker's purple.
     */
    private static void drawFormBadge(GuiGraphics guiGraphics, Item form, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        guiGraphics.fill(x, y + 6, x + 10, y + 16, COLOR_OUTLINE);
        guiGraphics.fill(x + 1, y + 7, x + 9, y + 15, COLOR_FORM_CHOSEN_INSIDE);
        guiGraphics.pose().popPose();
        guiGraphics.pose().pushPose();
        // Above the slot's item, below the count.
        guiGraphics.pose().translate(x + 1, y + 7, 100);
        guiGraphics.pose().scale(0.5F, 0.5F, 1F);
        guiGraphics.renderItem(new ItemStack(form), 0, 0);
        guiGraphics.pose().popPose();
    }

    private void drawCount(GuiGraphics guiGraphics, long count, int x, int y, int color) {
        drawCount(guiGraphics, IModHelpers.get().getGuiHelpers().quantityToScaledString(count), x, y, color);
    }

    private void drawCount(GuiGraphics guiGraphics, String text, int x, int y, int color) {
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

    private Component getFullCapacityInfo() {
        long depth = menu.getProfile().depth();
        return depth == 1 ? Component.translatable("gui.colossalchests2.stacks_per_slot.one")
                : Component.translatable("gui.colossalchests2.stacks_per_slot", formatCount(depth));
    }

    /**
     * @return The capacity per slot, shortened until it fits next to the slots info.
     */
    private Component getCapacityInfo() {
        long depth = menu.getProfile().depth();
        int available = getGridWidth() - 1 - font.width(getSlotsInfo()) - INFO_GAP;
        List<Component> candidates = List.of(getFullCapacityInfo(),
                Component.translatable("gui.colossalchests2.stacks_per_slot", CountFormat.compact(depth)),
                Component.translatable("gui.colossalchests2.stacks_per_slot.short", CountFormat.compact(depth)));
        return candidates.stream().filter(candidate -> font.width(candidate) <= available).findFirst().orElse(candidates.getLast());
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
            List<Component> lines = Lists.newArrayList(Component.translatable("gui.colossalchests2.stacks_per_slot.info"));
            Component full = getFullCapacityInfo();
            if (!getCapacityInfo().equals(full)) {
                lines.add(full);
            }
            lines.addAll(List.of(getCapacityLine("stack_64", 64), getCapacityLine("stack_16", 16), getCapacityLine("stack_1", 1)));
            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
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
        for (ChestUpgrade upgrade : ChestUpgrades.getAll()) {
            Component name = upgrade.getDisplayName();
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
        guiGraphics.drawString(font, settingsOpen ? Component.translatable("gui.colossalchests2.settings_title", title) : title,
                8, 6, COLOR_TEXT, false);
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
        Optional<CompressionFamily> family = getCompressedFamily(deepSlot);
        List<Component> lines = Lists.newArrayList(getTooltipFromContainerItem(deepSlot.getPrototype()));
        lines.add(Component.translatable("gui.colossalchests2.count", formatCount(deepSlot.getCount()),
                formatCount(menu.getChestSlotCapacity(slot))).withStyle(ChatFormatting.GRAY));
        if (menu.isChestSlotOverCapacity(slot)) {
            lines.add(Component.translatable("gui.colossalchests2.slot_over_capacity").withStyle(ChatFormatting.RED));
        }
        if (deepSlot.isLocked()) {
            lines.add(Component.translatable("gui.colossalchests2.slot_locked").withStyle(ChatFormatting.GOLD));
        }
        if (deepSlot.isVoiding()) {
            lines.add(Component.translatable("gui.colossalchests2.slot_voiding").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (family.isPresent()) {
            renderCompressedTooltip(guiGraphics, mouseX, mouseY, deepSlot, family.get(), lines);
        } else {
            guiGraphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    /**
     * @return The family of a compressed slot, while the chest compresses.
     */
    private Optional<CompressionFamily> getCompressedFamily(DeepSlot deepSlot) {
        if (minecraft == null || minecraft.level == null || deepSlot.isEmpty() || !menu.getUpgradeSet().has(ChestUpgrades.COMPRESSION)) {
            return Optional.empty();
        }
        return CompressionFamiliesCache.get(minecraft.level).find(deepSlot.getPrototype())
                .filter(family -> family.indexOf(deepSlot.getPrototype()) == 0);
    }

    private static int getChosenForm(DeepSlot deepSlot, CompressionFamily family) {
        return Math.max(0, deepSlot.getCompressionForm().map(family::indexOf).orElse(0));
    }

    /**
     * A tooltip like the bundle's: the text lines, then a row with each form and how many of it the slot makes, with
     * the form that clicks take framed.
     */
    private void renderCompressedTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, DeepSlot deepSlot, CompressionFamily family,
                                         List<Component> lines) {
        Component hint = Component.translatable("gui.colossalchests2.compression.scroll").withStyle(ChatFormatting.GRAY);
        long baseUnits = family.toBaseUnits(0, deepSlot.getCount()) + deepSlot.getRemainder();
        int chosen = getChosenForm(deepSlot, family);
        String[] amounts = new String[family.size()];
        int[] cellWidths = new int[family.size()];
        int rowWidth = 0;
        for (int form = 0; form < family.size(); form++) {
            amounts[form] = IModHelpers.get().getGuiHelpers().quantityToScaledString(family.fromBaseUnits(form, baseUnits));
            cellWidths[form] = Math.max(FORM_CELL_WIDTH, font.width(amounts[form]) + 4);
            rowWidth += cellWidths[form];
        }
        int width = Math.max(rowWidth, font.width(hint));
        for (Component line : lines) {
            width = Math.max(width, font.width(line));
        }
        int rowY = lines.size() * 10 + 2;
        int height = rowY + FORM_ROW_HEIGHT + 10;
        Vector2ic position = DefaultTooltipPositioner.INSTANCE.positionTooltip(this.width, this.height, mouseX, mouseY, width, height);
        int x = position.x();
        int y = position.y();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 400);
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, width, height, 0);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), x, y + i * 10 + (i > 0 ? 2 : 0), 0xFFFFFF, true);
        }
        int cellX = x;
        for (int form = 0; form < family.size(); form++) {
            int iconX = cellX + (cellWidths[form] - 16) / 2;
            if (form == chosen) {
                guiGraphics.fill(iconX - 2, y + rowY - 2, iconX + 18, y + rowY + 18, COLOR_FORM_CHOSEN);
                guiGraphics.fill(iconX - 1, y + rowY - 1, iconX + 17, y + rowY + 17, COLOR_FORM_CHOSEN_INSIDE);
            }
            guiGraphics.renderItem(new ItemStack(family.get(form).item()), iconX, y + rowY);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.drawString(font, amounts[form], cellX + (cellWidths[form] - font.width(amounts[form])) / 2, y + rowY + 19,
                    form == chosen ? 0xFFFFFF : 0xAAAAAA, true);
            guiGraphics.pose().popPose();
            cellX += cellWidths[form];
        }
        guiGraphics.drawString(font, hint, x, y + rowY + FORM_ROW_HEIGHT, 0xFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int slot = getHoveredSlot(mouseX, mouseY);
        if (slot >= 0 && scrollY != 0) {
            DeepSlot deepSlot = menu.getChestSlot(slot);
            Optional<CompressionFamily> family = getCompressedFamily(deepSlot);
            if (family.isPresent()) {
                // Scrolling down picks the next smaller form, like the bundle.
                int size = family.get().size();
                int form = Math.floorMod(getChosenForm(deepSlot, family.get()) + (scrollY < 0 ? 1 : -1), size);
                ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToServer(new ServerboundChestFormPacket(menu.containerId, slot,
                        new ItemStack(family.get().get(form).item())));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static String formatCount(long count) {
        return CountFormat.full(count);
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
        if (right && hasAltDown() && hasVoidUpgrade()) {
            sendClick(slot, ChestClickAction.TOGGLE_VOID);
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
