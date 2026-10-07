package org.cyclops.colossalchests2.blockentity;

import net.minecraft.util.Mth;

/**
 * Like a vanilla chest lid, with a configurable speed so larger lids move slower.
 * @author rubensworks
 */
public class ChestLid {

    private boolean shouldBeOpen;
    private float openness;
    private float oOpenness;
    private float speed = 0.1F;

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void tick() {
        oOpenness = openness;
        if (!shouldBeOpen && openness > 0.0F) {
            openness = Math.max(openness - speed, 0.0F);
        } else if (shouldBeOpen && openness < 1.0F) {
            openness = Math.min(openness + speed, 1.0F);
        }
    }

    public float getOpenness(float partialTick) {
        return Mth.lerp(partialTick, oOpenness, openness);
    }

    public void shouldBeOpen(boolean shouldBeOpen) {
        this.shouldBeOpen = shouldBeOpen;
    }

}
