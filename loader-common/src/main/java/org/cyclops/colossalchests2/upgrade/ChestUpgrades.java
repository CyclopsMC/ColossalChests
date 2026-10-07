package org.cyclops.colossalchests2.upgrade;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.config.UpgradeProperties;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The core upgrades.
 * @author rubensworks
 */
public final class ChestUpgrades {

    /**
     * Multiplies depth per upgrade, and adds one to the non-stackable factor.
     */
    public static final ChestUpgrade DEPTH = new ChestUpgrade(id("depth"), new UpgradeProperties(0, ImmutableMap.<ResourceLocation, Integer>builder()
            .put(ChestMaterial.WOOD.id(), 0)
            .put(ChestMaterial.COPPER.id(), 1)
            .put(ChestMaterial.IRON.id(), 2)
            .put(ChestMaterial.GOLD.id(), 3)
            .put(ChestMaterial.DIAMOND.id(), 4)
            .put(ChestMaterial.OBSIDIAN.id(), 5)
            .put(ChestMaterial.NETHERITE.id(), 6)
            .build(), 2)) {
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
    public static final ChestUpgrade SLOT_EXPANSION = new ChestUpgrade(id("slot_expansion"), new UpgradeProperties(3, Map.of(), 27)) {
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
    public static final ChestUpgrade LOCK = new ChestUpgrade(id("lock"), new UpgradeProperties(1, Map.of(), 1));

    /**
     * Multiplies the capacity for unstackable items per upgrade.
     */
    public static final ChestUpgrade BUNDLING = new ChestUpgrade(id("bundling"), new UpgradeProperties(4, Map.of(), 2)) {
        @Override
        public void applyProfile(CapacityProfile.Builder builder, int count) {
            long multiplier = getProperties().value();
            for (int i = 0; i < count; i++) {
                builder.multiplyNonStackable(multiplier);
            }
        }

        @Override
        public Component getEffect() {
            return Component.translatable("item.colossalchests2.upgrade_bundling.effect", getProperties().value());
        }
    };

    /**
     * Allows marking slots as voiding, so automation overflow of their type is destroyed.
     */
    public static final ChestUpgrade VOID = new ChestUpgrade(id("void"), new UpgradeProperties(1, Map.of(), 1));

    /**
     * Stores the forms of a compression family, such as nuggets, ingots and blocks, as its largest form in one slot.
     * Removal is refused while a slot holds a part that does not make a whole item of the largest form.
     */
    public static final ChestUpgrade COMPRESSION = new ChestUpgrade(id("compression"), new UpgradeProperties(1, Map.of(ChestMaterial.WOOD.id(), 0), 1)) {
        @Override
        public List<Integer> getRemovalProblems(ChestStorage storage) {
            List<Integer> slots = Lists.newArrayList();
            for (int slot = 0; slot < storage.getSlotCount(); slot++) {
                if (storage.getSlot(slot).getRemainder() > 0) {
                    slots.add(slot);
                }
            }
            return slots;
        }
    };

    /**
     * The upgrades of this mod, which it registers items for.
     */
    public static final List<ChestUpgrade> BUILT_IN = ImmutableList.of(DEPTH, SLOT_EXPANSION, LOCK, BUNDLING, VOID, COMPRESSION);

    private static volatile List<ChestUpgrade> all = BUILT_IN;

    private ChestUpgrades() {
    }

    /**
     * Register an upgrade, which must happen while mods are constructed, before any world is loaded.
     * @param upgrade The upgrade.
     */
    public static synchronized void register(ChestUpgrade upgrade) {
        if (byId(upgrade.getId()) != null) {
            throw new IllegalArgumentException("Chest upgrade " + upgrade.getId() + " is already registered");
        }
        all = ImmutableList.<ChestUpgrade>builder().addAll(all).add(upgrade).build();
    }

    /**
     * @return All registered upgrades, this mod's first.
     */
    public static List<ChestUpgrade> getAll() {
        return all;
    }

    @Nullable
    public static ChestUpgrade byId(ResourceLocation id) {
        return all.stream().filter(upgrade -> upgrade.getId().equals(id)).findFirst().orElse(null);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

}
