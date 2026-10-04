package org.cyclops.colossalchests2.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The order in which the chest GUI shows slots. It never moves items between slots.
 * @author rubensworks
 */
public enum ChestSortMode implements StringRepresentable {
    /**
     * Slot order.
     */
    NONE("none"),
    NAME("name"),
    /**
     * Largest counts first.
     */
    COUNT("count"),
    /**
     * By mod, then by name.
     */
    MOD("mod");

    public static final Codec<ChestSortMode> CODEC = StringRepresentable.fromEnum(ChestSortMode::values);

    private final String name;

    ChestSortMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
