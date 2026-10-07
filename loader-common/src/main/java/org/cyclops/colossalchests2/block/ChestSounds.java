package org.cyclops.colossalchests2.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Chest sounds that depend on the chest's size: larger chests sound deeper, carry further and open slower.
 * @author rubensworks
 */
public final class ChestSounds {

    /**
     * Lid speed per tick of vanilla chests, kept up to this size.
     */
    public static final int VANILLA_LID_MAX_SIZE = 3;
    private static final float VANILLA_LID_SPEED = 0.1F;
    private static final float PITCH_VARIATION = 0.1F;

    private ChestSounds() {
    }

    /**
     * @param size The outer edge length, 1 for a single block chest.
     * @return The base pitch: above vanilla for single blocks, deeper as chests grow.
     */
    public static float getPitch(int size) {
        return Mth.clamp(1.25F * (float) Math.pow(Math.max(1, size), -0.4), 0.5F, 2.0F);
    }

    /**
     * Volumes above 1 do not sound louder, but are heard from further away (16 blocks per unit).
     * @param size The outer edge length.
     * @return The volume.
     */
    public static float getVolume(int size) {
        return 0.5F + 0.6F * (float) Math.log(Math.max(1, size));
    }

    /**
     * @param size The outer edge length.
     * @return How much the lid opens or closes per tick, 1 being fully open.
     */
    public static float getLidSpeed(int size) {
        return size <= VANILLA_LID_MAX_SIZE ? VANILLA_LID_SPEED : VANILLA_LID_SPEED * (float) Math.sqrt((double) VANILLA_LID_MAX_SIZE / size);
    }

    /**
     * @param random A random source.
     * @return A small random pitch factor, so repeated sounds vary.
     */
    public static float getPitchVariation(RandomSource random) {
        return 1.0F - PITCH_VARIATION / 2 + random.nextFloat() * PITCH_VARIATION;
    }

    /**
     * Play the opening or closing sound for everyone nearby.
     */
    public static void play(Level level, Vec3 center, int size, boolean open) {
        level.playSound(null, center.x, center.y, center.z, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
                getVolume(size), getPitch(size) * getPitchVariation(level.random));
    }

}
