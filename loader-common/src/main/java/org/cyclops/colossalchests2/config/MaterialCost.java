package org.cyclops.colossalchests2.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * An amount of an item, by id so files may name items of mods that are not installed.
 * @param item The item id.
 * @param count The amount.
 * @author rubensworks
 */
public record MaterialCost(ResourceLocation item, int count) {

    public static final Codec<MaterialCost> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(MaterialCost::item),
            Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(MaterialCost::count)
    ).apply(i, MaterialCost::new));

}
