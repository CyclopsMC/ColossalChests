package org.cyclops.colossalchests.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;

import java.util.Optional;

/**
 * Triggers when a colossal chest is formed.
 * @author rubensworks
 */
public class ChestFormedTrigger extends SimpleCriterionTrigger<ChestFormedTrigger.Instance> {

    public static final Codec<ChestFormedTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ChestFormedTrigger.Instance::player),
                            ChestMaterial.CODEC.optionalFieldOf("material").forGetter(ChestFormedTrigger.Instance::material),
                            Codec.INT.optionalFieldOf("minimumSize").forGetter(ChestFormedTrigger.Instance::minimumSize)
                    )
                    .apply(p_311401_, ChestFormedTrigger.Instance::new)
    );

    public void test(ServerPlayer player, ChestMaterial material, int size) {
        this.trigger(player, (instance) -> instance.test(player, Pair.of(material, size)));
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<ChestMaterial> material,
            Optional<Integer> minimumSize
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<Pair<ChestMaterial, Integer>> {
        @Override
        public boolean test(ServerPlayer player, Pair<ChestMaterial, Integer> data) {
            return this.material.map(mat -> mat == data.getLeft()).orElse(true)
                    && this.minimumSize.map(size -> size <= data.getRight()).orElse(true);
        }
    }

}
