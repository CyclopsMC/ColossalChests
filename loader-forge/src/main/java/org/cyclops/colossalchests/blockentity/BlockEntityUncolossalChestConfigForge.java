package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfigForge<M extends ModBaseForge> extends BlockEntityUncolossalChestConfig<M> {
    public BlockEntityUncolossalChestConfigForge(M mod) {
        super(mod);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerCapabilities);
    }

    public void registerCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject().getType() == getInstance()) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, getNamedId()), new ICapabilityProvider() {
                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                    if (capability == ForgeCapabilities.ITEM_HANDLER) {
                        return LazyOptional.of(() -> new InvWrapper(((BlockEntityUncolossalChest) event.getObject()).getInventory())).cast();
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }
}
