package org.cyclops.colossalchests.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
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
public class ChestFormedTrigger extends AbstractCriterionTrigger<ChestFormedTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "chest_formed");

    public ChestFormedTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
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
            minimumSize = JSONUtils.getInt(elementSize, "minimumSize");
        }
        return new Instance(getId(), entityPredicate, material, minimumSize);
    }

    public void test(ServerPlayerEntity player, ChestMaterial material, int size) {
        this.triggerListeners(player, (instance) -> {
            return instance.test(player, Pair.of(material, size));
        });
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<Pair<ChestMaterial, Integer>> {
        private final ChestMaterial material;
        private final Integer minimumSize;

        public Instance(ResourceLocation criterionIn, EntityPredicate.AndPredicate player, @Nullable ChestMaterial material, @Nullable Integer minimumSize) {
            super(criterionIn, player);
            this.material = material;
            this.minimumSize = minimumSize;
        }

        public boolean test(ServerPlayerEntity player, Pair<ChestMaterial, Integer> data) {
            return (this.material == null || this.material == data.getLeft())
                    && (this.minimumSize == null || this.minimumSize <= data.getRight());
        }
    }

}
