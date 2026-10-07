package org.cyclops.colossalchests2.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.cyclops.colossalchests2.block.ChestMaterial;

import java.util.Optional;

/**
 * Triggers when a chest forms near a player, or a player changes a chest's material.
 * @author rubensworks
 */
public class ChestFormedTrigger extends SimpleCriterionTrigger<ChestFormedTrigger.Instance> {

    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ChestMaterial.CODEC.optionalFieldOf("material").forGetter(Instance::material),
            Codec.INT.optionalFieldOf("minimum_size").forGetter(Instance::minimumSize)
    ).apply(i, Instance::new));

    public void trigger(ServerPlayer player, ChestMaterial material, int size) {
        this.trigger(player, instance -> instance.matches(material, size));
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    /**
     * @param player The player conditions.
     * @param material The chest material, any if empty.
     * @param minimumSize The smallest chest size, any if empty.
     */
    public record Instance(Optional<ContextAwarePredicate> player, Optional<ChestMaterial> material,
                           Optional<Integer> minimumSize) implements SimpleCriterionTrigger.SimpleInstance {
        public boolean matches(ChestMaterial material, int size) {
            return this.material.map(m -> m == material).orElse(true) && this.minimumSize.map(s -> s <= size).orElse(true);
        }
    }

}
