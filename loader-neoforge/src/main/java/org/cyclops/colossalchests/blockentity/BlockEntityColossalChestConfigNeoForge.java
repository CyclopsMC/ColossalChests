package org.cyclops.colossalchests.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChest;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChestNeoForge;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.inventory.IInventoryIndexReference;
import org.cyclops.cyclopscore.inventory.IndexedItemResourceHandler;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestConfigNeoForge<M extends ModBaseNeoForge<?>> extends BlockEntityColossalChestConfig<M> {
    public BlockEntityColossalChestConfigNeoForge(M mod) {
        super(mod);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    @Override
    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityColossalChest> getBlockEntitySupplier() {
        return BlockEntityColossalChestNeoForge::new;
    }

    @Override
    protected BlockEntityRendererProvider<BlockEntityColossalChest, RenderTileEntityColossalChest.RenderState> getBlockEntityRendererProvider() {
        return RenderTileEntityColossalChestNeoForge::new;
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                getInstance(),
                (blockEntity, context) -> {
                    if (blockEntity.getInventory() instanceof IInventoryIndexReference inventoryIndexReference) {
                        return new IndexedItemResourceHandler(inventoryIndexReference, VanillaContainerWrapper.of(blockEntity.getInventory()));
                    } else {
                        return VanillaContainerWrapper.of(blockEntity.getInventory());
                    }
                }
        );
    }
}
