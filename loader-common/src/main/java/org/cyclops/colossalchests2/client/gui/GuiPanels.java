package org.cyclops.colossalchests2.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Drawing of vanilla style panels and slots at any size.
 * @author rubensworks
 */
public final class GuiPanels {

    private static final ResourceLocation PANEL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int PANEL_TEXTURE_WIDTH = 176;
    private static final int PANEL_TEXTURE_HEIGHT = 222;
    private static final int PANEL_CORNER = 4;
    private static final int COLOR_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SLOT = 0xFF8B8B8B;
    private static final int COLOR_SLOT_SHADOW = 0xFF373737;

    private GuiPanels() {
    }

    /**
     * A panel with the borders of the vanilla chest texture.
     */
    public static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
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

    /**
     * A vanilla style slot background around an item position.
     */
    public static void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, COLOR_SLOT_SHADOW);
        guiGraphics.fill(x, y, x + 17, y + 17, COLOR_LIGHT);
        guiGraphics.fill(x, y, x + 16, y + 16, COLOR_SLOT);
    }
}
