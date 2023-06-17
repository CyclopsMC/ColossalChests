package org.cyclops.colossalchests.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Triggers when a colossal chest is formed.
 * @author rubensworks
 */
public class ChestFormedTrigger extends SimpleCriterionTrigger<ChestFormedTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "chest_formed");

    public ChestFormedTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext conditionsParser) {
        ChestMaterial material = null;
        JsonElement element = json.get("material");
        if (element != null && !element.isJsonNull()) {
            String materialString = element.getAsString();
            try {
                material = Objects.requireNonNull(ChestMaterial.valueOf(materialString), "Could not find a chest material by name " + materialString);
            } catch (IllegalArgumentException e) {
                throw new JsonSyntaxException("Could not find a colossal chest material by name " + materialString
                        + ". Allowed values: "
                        + ChestMaterial.VALUES.stream().map(ChestMaterial::getName).collect(Collectors.toList()));
            }
        }

        Integer minimumSize = null;
        JsonElement elementSize = json.get("minimumSize");
        if (elementSize != null && !elementSize.isJsonNull()) {
            minimumSize = GsonHelper.convertToInt(elementSize, "minimumSize");
        }
        return new Instance(getId(), entityPredicate, material, minimumSize);
    }

    public void test(ServerPlayer player, ChestMaterial material, int size) {
        this.trigger(player, (instance) -> {
            return instance.test(player, Pair.of(material, size));
        });
    }

    public static class Instance extends AbstractCriterionTriggerInstance implements ICriterionInstanceTestable<Pair<ChestMaterial, Integer>> {
        private final ChestMaterial material;
        private final Integer minimumSize;

        public Instance(ResourceLocation criterionIn, ContextAwarePredicate player, @Nullable ChestMaterial material, @Nullable Integer minimumSize) {
            super(criterionIn, player);
            this.material = material;
            this.minimumSize = minimumSize;
        }

        public boolean test(ServerPlayer player, Pair<ChestMaterial, Integer> data) {
            return (this.material == null || this.material == data.getLeft())
                    && (this.minimumSize == null || this.minimumSize <= data.getRight());
        }
    }

}
