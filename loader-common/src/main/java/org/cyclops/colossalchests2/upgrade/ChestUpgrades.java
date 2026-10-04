package org.cyclops.colossalchests2.upgrade;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The core upgrades.
 * @author rubensworks
 */
public final class ChestUpgrades {

    /**
     * Multiplies depth per upgrade, and adds one to the non-stackable factor.
     */
    public static final ChestUpgrade DEPTH = new ChestUpgrade(id("depth")) {
        @Override
        public void applyProfile(CapacityProfile.Builder builder, int count) {
            long multiplier = getProperties().value();
            for (int i = 0; i < count; i++) {
                builder.multiplyDepth(multiplier);
            }
            builder.addNonStackableFactor(count);
        }

        @Override
        public Component getEffect() {
            return Component.translatable("item.colossalchests2.upgrade_depth.effect", getProperties().value());
        }
    };

    /**
     * Adds slots per upgrade.
     */
    public static final ChestUpgrade SLOT_EXPANSION = new ChestUpgrade(id("slot_expansion")) {
        @Override
        public int getExtraSlots(int count) {
            return (int) Math.min(Integer.MAX_VALUE, count * getProperties().value());
        }

        @Override
        public Component getEffect() {
            return Component.translatable("item.colossalchests2.upgrade_slot_expansion.effect", getProperties().value(), GeneralConfig.getMaxSlots());
        }
    };

    /**
     * Allows locking slots to a type.
     */
    public static final ChestUpgrade LOCK = new ChestUpgrade(id("lock"));

    public static final List<ChestUpgrade> VALUES = ImmutableList.of(DEPTH, SLOT_EXPANSION, LOCK);

    private ChestUpgrades() {
    }

    @Nullable
    public static ChestUpgrade byId(ResourceLocation id) {
        return VALUES.stream().filter(upgrade -> upgrade.getId().equals(id)).findFirst().orElse(null);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

}
