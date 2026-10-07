package org.cyclops.colossalchests2.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Chest sounds that depend on the chest's size and material.
 * Larger chests sound deeper, carry further and close slower, ending in a heavy thud.
 * @author rubensworks
 */
public final class ChestSounds {

    /**
     * The smallest size whose lid lands with a thud.
     */
    public static final int THUD_MIN_SIZE = 6;
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
     * A sound played together with others.
     * @param sound The sound.
     * @param volume Its volume relative to the chest's volume.
     */
    public record Layer(SoundEvent sound, float volume) {
    }

    /**
     * Wood sounds like a vanilla chest, metals add a trapdoor and stone-like materials sound like an ender chest.
     * @param material The chest material.
     * @param open If opening, otherwise closing.
     * @return The sounds to play together.
     */
    public static List<Layer> getLayers(ChestMaterial material, boolean open) {
        Layer chest = new Layer(open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, 1.0F);
        if (material == ChestMaterial.COPPER) {
            return List.of(chest, new Layer(open ? SoundEvents.COPPER_TRAPDOOR_OPEN : SoundEvents.COPPER_TRAPDOOR_CLOSE, 0.4F));
        }
        if (material == ChestMaterial.IRON || material == ChestMaterial.GOLD || material == ChestMaterial.DIAMOND) {
            return List.of(chest, new Layer(open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE, 0.4F));
        }
        if (material == ChestMaterial.OBSIDIAN || material == ChestMaterial.NETHERITE) {
            return List.of(new Layer(open ? SoundEvents.ENDER_CHEST_OPEN : SoundEvents.ENDER_CHEST_CLOSE, 1.0F));
        }
        return List.of(chest);
    }

    /**
     * @param material The chest material.
     * @return The sound of a large lid landing.
     */
    public static SoundEvent getThud(ChestMaterial material) {
        return material == ChestMaterial.WOOD ? SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR : SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;
    }

    /**
     * @param random A random source.
     * @return A pitch factor, shared by layers so they stay in tune.
     */
    public static float getPitchVariation(RandomSource random) {
        return 1.0F - PITCH_VARIATION / 2 + random.nextFloat() * PITCH_VARIATION;
    }

    /**
     * Play the opening or closing sound for everyone nearby.
     */
    public static void play(Level level, Vec3 center, int size, ChestMaterial material, boolean open) {
        float pitch = getPitch(size) * getPitchVariation(level.random);
        float volume = getVolume(size);
        for (Layer layer : getLayers(material, open)) {
            level.playSound(null, center.x, center.y, center.z, layer.sound(), SoundSource.BLOCKS, volume * layer.volume(), pitch);
        }
    }

    /**
     * Play the thud of a large lid landing, only for this client, as the lid moves on the client.
     */
    public static void playThud(Level level, Vec3 center, int size, ChestMaterial material) {
        level.playLocalSound(center.x, center.y, center.z, getThud(material), SoundSource.BLOCKS,
                getVolume(size) * 0.35F, getPitch(size) * 0.8F * getPitchVariation(level.random), false);
    }

}
