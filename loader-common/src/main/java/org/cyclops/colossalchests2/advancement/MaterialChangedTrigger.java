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
 * Triggers when a player changes a chest's material with the Chest Material Upgrade Tool.
 * @author rubensworks
 */
public class MaterialChangedTrigger extends SimpleCriterionTrigger<MaterialChangedTrigger.Instance> {

    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ChestMaterial.CODEC.optionalFieldOf("from").forGetter(Instance::from),
            ChestMaterial.CODEC.optionalFieldOf("to").forGetter(Instance::to)
    ).apply(i, Instance::new));

    public void trigger(ServerPlayer player, ChestMaterial from, ChestMaterial to) {
        this.trigger(player, instance -> instance.matches(from, to));
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    /**
     * @param player The player conditions.
     * @param from The old material, any if empty.
     * @param to The new material, any if empty.
     */
    public record Instance(Optional<ContextAwarePredicate> player, Optional<ChestMaterial> from,
                           Optional<ChestMaterial> to) implements SimpleCriterionTrigger.SimpleInstance {
        public boolean matches(ChestMaterial from, ChestMaterial to) {
            return this.from.map(m -> m.equals(from)).orElse(true) && this.to.map(m -> m.equals(to)).orElse(true);
        }
    }

}
