package org.cyclops.colossalchests.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChestNeoForge;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestConfigNeoForge<M extends ModBase> extends BlockEntityColossalChestConfig<M> {
    public BlockEntityColossalChestConfigNeoForge(M mod) {
        super(mod);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    @Override
    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityColossalChest> getBlockEntitySupplier() {
        return BlockEntityColossalChestNeoForge::new;
    }

    @Override
    protected BlockEntityRendererProvider<BlockEntityColossalChest> getBlockEntityRendererProvider() {
        return RenderTileEntityColossalChestNeoForge::new;
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getInventory())
        );
    }
}
