package org.cyclops.colossalchests.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.cyclopscore.advancement.criterion.BaseCriterionTrigger;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Triggers when a colossal chest is formed.
 * @author rubensworks
 */
public class ChestFormedTrigger extends BaseCriterionTrigger<Pair<PropertyMaterial.Type, Integer>, ChestFormedTrigger.Instance> {
    public ChestFormedTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "chest_formed"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        PropertyMaterial.Type material = null;
        JsonElement element = json.get("material");
        if (element != null && !element.isJsonNull()) {
            String materialString = element.getAsString();
            try {
                material = PropertyMaterial.Type.valueOf(materialString);
            } catch (IllegalArgumentException e) {
                throw new JsonSyntaxException("Could not find a colossal chest material by name " + materialString
                        + ". Allowed values: "
                        + Arrays.stream(PropertyMaterial.Type.values()).map(Enum::name).collect(Collectors.toList()));
            }
        }

        Integer minimumSize = null;
        JsonElement elementSize = json.get("minimumSize");
        if (elementSize != null && !elementSize.isJsonNull()) {
            minimumSize = JsonUtils.getInt(elementSize, "minimumSize");
        }
        return new Instance(getId(), material, minimumSize);
    }

    public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable<Pair<PropertyMaterial.Type, Integer>> {
        private final PropertyMaterial.Type material;
        private final Integer minimumSize;

        public Instance(ResourceLocation criterionIn, @Nullable PropertyMaterial.Type material, @Nullable Integer minimumSize) {
            super(criterionIn);
            this.material = material;
            this.minimumSize = minimumSize;
        }

        public boolean test(EntityPlayerMP player, Pair<PropertyMaterial.Type, Integer> data) {
            return (this.material == null || this.material == data.getLeft())
                    && (this.minimumSize == null || this.minimumSize <= data.getRight());
        }
    }

}
